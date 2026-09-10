# Phase 1 - Foundations: the machine and the numbers

**Days 1-10**

## Why this phase comes first

Every scalability argument bottoms out in physics. "Add a cache" is a claim about the ratio
between a memory reference and a disk seek. "Shard it" is a claim about what a single machine
can hold. "Use a queue" is a claim about what happens to latency as utilisation approaches one.

If you do not have those numbers, system design becomes pattern-matching on vocabulary, and it
shows immediately under follow-up questions. So before any architecture, you spend ten days
building an accurate mental model of one machine: how fast its memory is, how expensive a thread
is, what a connection costs, and what queueing does to your p99.

You will **measure** most of these rather than read them. Numbers you produced yourself are the
ones you still trust six months later at a whiteboard.

## What you will be able to do afterwards

- Estimate QPS, storage and bandwidth for any system in under five minutes, out loud
- Explain why sequential access beats random by 10-100x, and name three designs that exploit it
- Say what a thread costs, and why virtual threads changed the answer
- Explain why servers run at 70% utilisation and not 95%, with the arithmetic
- Choose a wire format and defend the choice on size, speed and evolvability

## The through-line

Days 1-3 are about **cost models**: memory, time, and queues.
Days 4-6 are about **concurrency**: what goes wrong, how to fix it, what it costs.
Days 7-9 are about **I/O**: connections, servers, and bytes on the wire.
Day 10 puts all of it into one capacity estimate you have to defend.

## The one idea to carry forward

**Sequential beats random, and batching beats chatter, at every single layer.** You will meet
this idea again as the LSM tree in Phase 5, as write-behind caching in Phase 6, and as producer
batching in Phase 7. It is the same idea each time. Notice it when it recurs.
