# Day 8 - HTTP, TCP, and what a connection costs

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

An HTTP server is a socket, a parser and a routing table. Every framework you have used is that
plus conveniences. Building one in twenty lines is worth doing once, because afterwards
"the framework is slow" becomes a question you can actually investigate.

The number that matters today is what a **connection** costs. Opening a TCP connection is a
three-way handshake: SYN, SYN-ACK, ACK. That is one full round trip before a single byte of your
request moves. Add TLS and it is one or two more.

Do the arithmetic with Day 1's numbers:

- Same datacenter: ~0.5 ms round trip. Handshake ~0.5 ms.
- Cross-country: ~40 ms. Handshake ~40 ms, plus ~80 ms for TLS.
- Cross-continent: ~150 ms. Handshake ~150 ms, plus ~300 ms for TLS.

For a 200 ms API call across a continent, a fresh connection can cost more than the work. That
arithmetic - and nothing more sophisticated - is the entire reason for connection pools, HTTP
keep-alive, HTTP/2 multiplexing, and `proxy_set_header Connection ""` in an nginx config. When
you configure a pool in Phase 5, this is the number you are spending.

**The trade-off:** pooled connections hold resources on both ends and go stale. A pooled
connection to a server that has silently gone away fails on first use, which is why pools need
validation queries and idle timeouts, and why "connection reset" is such a common production
error.

## Build (25 min)

Implement `TinyHttpServer` in `src/main/java/sd/p01/day08/` using the JDK's built-in
`com.sun.net.httpserver.HttpServer`. Three routes: `/health`, `/echo?msg=...`, and a 404 catch-all.

Two details that are real bugs, not pedantry:

- `sendResponseHeaders` takes the **byte** length, not the string length. They differ the moment
  a non-ASCII character appears, and the mismatch truncates the response.
- Close the response body stream, or you leak the connection.

`ConnectionCostBenchmark` is given and compares 300 requests over a fresh connection each time
against 300 over one reused connection.

## Reflect (10 min)

1. What was the per-request difference over loopback? Now multiply the gap by a 40 ms
   cross-country round trip. What does that do to a 300-request page load?
2. `createContext("/")` is a prefix match that catches everything unmatched. That is the entire
   routing algorithm. What does a real router add, and what does it cost?
3. Your connection pool has 10 connections and each request takes 50 ms. What is your maximum
   throughput through that pool? (You already know this one - it is Day 3.)

**Interview angle:** when you draw a service calling three others, say "I would use pooled,
keep-alive connections here - the handshake is a full round trip and at this QPS that is real
latency." It shows you are costing the arrows on your diagram, not just drawing them.

## Stretch

Add a `Connection: close` header to your responses and re-run the reuse benchmark. Watch the
advantage disappear. Then find where your favourite HTTP client configures its pool size, and
work out what its default implies about maximum throughput.

## Checkpoint

```powershell
.\day.cmd 8
```
