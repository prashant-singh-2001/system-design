# Day 15 - Dependency Inversion

**Phase 2 - SOLID** | 45 minutes

## Concept (10 min)

> High-level modules should not depend on low-level modules. Both should depend on abstractions.

Look at `LegacyReportService`. One word is the problem: `new`.

The grouping-and-summing logic is pure arithmetic with no need for a database. But because the
service constructs its own DAO, you cannot exercise that arithmetic without a live Postgres. The
dependency points the wrong way: a high-level policy (how we report on sales) depends on a
low-level detail (that sales live in a relational database, reached over JDBC).

The fix is to introduce a port - `SalesDataSource` - and inject it. But the part people miss is
**who owns the abstraction**. The port is declared beside the domain logic that needs it, not
beside the JDBC code that implements it. The domain writes the job description; infrastructure
applies for the job. That ownership direction is what makes it *inversion* rather than merely
*indirection* - and it is the difference between DIP and "we added an interface".

There is a corollary worth stating: nothing about persistence may leak into the port. No
`Connection`, no `SQLException`, no result set, no cursor. If it does, the inversion is
incomplete and you will discover it the first time you try to back the port with an HTTP API
instead of a database.

**The trade-off:** an extra interface per boundary, and the wiring has to happen somewhere -
usually a composition root, or a DI container. Worth it at real boundaries (storage, network,
clock, randomness). Not worth it for every collaborator; injecting an interface for a pure
function you will never replace is ceremony, not design.

## Build (25 min)

In `src/main/java/sd/p02/day15/`:

1. `SalesDataSource` - one method, `List<Sale> findSales()`. Nothing about JDBC in the signature.
2. `ReportService` - take the port as a constructor parameter, copy the grouping and sorting
   logic across unchanged. Totals descending, ties broken by region name ascending.

The whole test double in the test file is `SalesDataSource fake = () -> SALES;`. One line. That
is what dependency inversion bought.

## Reflect (10 min)

1. Testability is often listed as a *separate* benefit of DIP. Argue that it is the same benefit
   viewed from a different angle.
2. Your port returns `List<Sale>`. What if there are ten million sales? What would you change,
   and does the fix threaten the abstraction?
3. Where should the `SalesDataSource` interface physically live - the same package as
   `ReportService`, or with the JDBC implementation? Why does it matter?

**Interview angle:** "the domain declares the port and infrastructure implements it, so the
dependency arrow points inward" is the sentence. It sets up Day 17's hexagonal architecture and
signals that you understand the direction, not just the indirection.

## Stretch

Add a second implementation - a `CsvSalesDataSource` reading from a string. Note that
`ReportService` needed no change at all. Then ask what would have needed to change if the port
had exposed a `ResultSet`.

## Checkpoint

```powershell
.\day.cmd 15
```
