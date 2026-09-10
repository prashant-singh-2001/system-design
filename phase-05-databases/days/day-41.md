# Day 41 - Schema design, and real constraints

**Phase 5 - Databases** | 45 minutes

## Docker starts here

Start Docker Desktop before you begin. The test spins up its own disposable Postgres via
Testcontainers, so there is nothing to install and nothing to clean up - but the daemon has to
be running. The first run pulls `postgres:16-alpine`, which takes a minute; after that it is
seconds.

## Concept (10 min)

Normalization has a bad reputation because it is taught as three rules to recite. The rules are
just consequences of one idea: **store each fact exactly once, so it can never disagree with
itself.**

- **1NF** - no repeating groups. No `item1_name`, `item2_name`, `item3_name`. Order items get
  their own table.
- **2NF** - every non-key column depends on the *whole* key. In `order_items`, keyed by
  `(order_id, product_id)`, a column depending on only `product_id` belongs in `products`.
- **3NF** - no transitive dependencies. A product's name lives in `products` and nowhere else,
  so renaming it is one write rather than a hunt.

Then the deliberate exception, which is the interesting part. `order_items.unit_price_cents`
duplicates `products.price_cents` - and it should. The price *at the time of purchase* is
genuinely a different fact from the price *now*. Prices change; invoices must not. Recognising
when a duplicate is actually a distinct fact is the skill; blindly de-duplicating is how you
build a system that silently rewrites history.

Second theme: **constraints are code the database runs for you, on every write, forever.** A
`CHECK (price_cents > 0)` cannot be forgotten by a new service, bypassed by a migration script,
or lost in a refactor. Application validation is a courtesy to the user; a constraint is the
actual guarantee.

**The trade-off:** normalized schemas need joins, and joins cost. Constraints cost write latency
and make some migrations harder. Both are usually worth it, and Day 46 is where you deliberately
pay the other side of the bargain.

## Build (25 min)

Fill in `src/main/resources/sd/p05/day41/schema.sql`. The exact tables, columns and constraints
are listed in the file's header comment, and `Day41SchemaTest` checks every one of them by
trying to insert data that should be refused.

Order matters: `customers` and `products` before `orders`, and `orders` before `order_items`,
or the foreign keys have nothing to point at.

## Reflect (10 min)

1. Every one of those constraints could also be enforced in application code. Name three things
   that can bypass application validation but cannot bypass a database constraint.
2. `order_items` uses a composite primary key `(order_id, product_id)`. What does that make
   impossible, and what would break if you added a surrogate `id` instead?
3. The `status` CHECK lists four values. What has to happen to ship a fifth one? Compare that
   with storing status as a foreign key to a `statuses` table.

**Interview angle:** when you sketch a data model, say the key out loud and say what it makes
impossible. "The composite key on (order_id, product_id) means the same product cannot appear
twice on one order" is a design statement; a list of column names is not.

## Stretch

Add `ON DELETE CASCADE` to `order_items.order_id` and `ON DELETE RESTRICT` to
`orders.customer_id`. Work out why those two want opposite behaviour.

## Checkpoint

```powershell
.\day.cmd 41
```
