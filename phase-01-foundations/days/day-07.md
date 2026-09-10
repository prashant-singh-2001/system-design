# Day 7 - Blocking vs non-blocking I/O

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

In the blocking model, one thread owns one connection and sits inside `read()` until bytes
arrive. It is beautifully simple - the call stack *is* the connection state, so control flow,
exceptions and debugging all work the way you expect. The cost is one thread per connection.
At 10,000 mostly-idle open connections that is 10,000 threads holding 10,000 stacks. This is
the C10K problem, and it is what drove the industry to async.

In the non-blocking model, one thread owns a `Selector`. Channels register their interest, the
thread asks the OS "which of these are ready?", and it services only those. One thread can drive
tens of thousands of connections, because idle connections cost nothing but a file descriptor.

The cost is that connection state must now live somewhere explicit - in objects you manage,
attached to selection keys - instead of on a call stack. You have hand-rolled what the thread
scheduler was doing for you. This is why Netty exists, and why Netty is not a small library.

Today you write both and feel the difference in your hands.

**The trade-off, and the twist:** after yesterday, ask the real question. Virtual threads give
blocking code the scalability of non-blocking code. So when is the Selector still worth it?
The honest remaining answers are: when you need fine-grained control over buffers and
backpressure, when you are writing a proxy that never touches payload bytes, and when you are on
a runtime without virtual threads. That list is much shorter than it was in 2015 - and knowing
that it got shorter is itself the point of today.

## Build (25 min)

`BlockingEchoServer` is given, fully working. Implement `NioEchoServer` in
`src/main/java/sd/p01/day07/`: bind to port 0, run a single-threaded `Selector` loop on a daemon
thread, echo each line back.

Three traps, all of which people hit:

- **Clear `selectedKeys()` every pass.** Forget it and you reprocess stale keys forever. This is
  the single most common NIO bug.
- **`read()` returning -1** means the peer closed. Cancel the key and close the channel, or the
  loop spins at 100% CPU.
- **Anything slow inside the loop blocks every connection.** One thread, no exceptions.

The class javadoc has the full skeleton. Take it - the learning today is in getting the details
right, not in inventing the shape.

## Reflect (10 min)

1. Compare the two files. How much longer is the Selector version, and where did the extra
   complexity go?
2. The blocking server holds connection state on the call stack. Where does the Selector version
   hold it? What would you attach to a `SelectionKey` for a protocol that needs a partial-message
   buffer?
3. Given virtual threads exist, would you choose NIO for a new service today? Give one concrete
   case where you would, and one where you would not.

**Interview angle:** if asked to handle 100,000 concurrent connections, the strong answer names
both paths and picks on evidence: "either an event loop, or virtual threads with blocking code -
I would start with the second because it is far easier to debug, and move to an event loop only
if profiling showed the scheduler was the bottleneck."

## Stretch

Add a `write()` that only partially completes. Handle it properly: buffer the remainder, register
`OP_WRITE`, and drain on the next readiness event. This is the part everyone skips and every
production event loop must get right.

## Checkpoint

```powershell
.\day.cmd 7
```
