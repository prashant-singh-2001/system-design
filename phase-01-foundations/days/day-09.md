# Day 9 - Wire formats

**Phase 1 - Foundations** | 45 minutes

## Concept (10 min)

Every message between two services is encoded by one and decoded by the other. That choice
sets your bandwidth bill, your CPU cost, and - most importantly and least obviously - how hard
it will be to change the message next year.

- **Java serialization.** Convenient, and you should almost never ship it. Every payload carries
  a class descriptor, so it is bloated. It is Java-only. It is brittle across versions. And
  deserializing untrusted bytes is a remote-code-execution vector behind a long list of real
  CVEs.
- **JSON.** Any language can read it, humans can debug it, and adding a field does not break old
  readers. The cost: every field name is repeated in every message, and numbers are stored as
  text. At a million events a second that repetition is most of your bandwidth.
- **Compact binary.** No field names on the wire; a `long` is 8 bytes rather than up to 20
  characters. Expect roughly a third of the JSON size. The cost is severe: the format is now
  *implicit in the code*. Reader and writer must agree exactly, forever. Add a field and old
  readers break. Reorder two fields and you get silent corruption rather than an error.

That last cost is why Protobuf, Avro and Thrift exist. They give you binary density *with* a way
to evolve - numbered field tags, or a schema registry - so that adding a field is safe and
removing one is a deliberate, checkable act.

**The trade-off, stated properly:** you are choosing between bytes on the wire and the ability to
change your mind later. Internal high-volume paths can afford a schema; public APIs usually
cannot, which is why the world's public APIs are JSON and its internal RPC is not.

## Build (25 min)

Implement `JsonCodec` and `BinaryCodec` in `src/main/java/sd/p01/day09/`. No libraries.

- **JSON:** build the string, `getBytes(UTF_8)`, and escape at least `"` and `\` in string
  values. (Ask yourself what an unescaped quote in a payload does to your output. That is an
  injection bug, not a formatting nit.) For decoding you only have to parse this one fixed shape.
- **Binary:** `DataOutputStream` with `writeLong` / `writeUTF` in a fixed order, and read them
  back in the same order.

The tests check round-trip fidelity including quotes, backslashes and multi-byte characters, and
assert `binary < json < java-serialization` on size.

## Reflect (10 min)

1. Record the three sizes. What is the binary-to-JSON ratio? Multiply by 1M events/second and
   put a number on the annual bandwidth difference.
2. **The key question:** how would you add a fifth field to `BinaryCodec` without breaking every
   existing reader? Work it out properly - your answer is essentially how Protobuf works.
3. Your public API and your internal event bus have different constraints. Which format for each,
   and what specifically drove each choice?

**Interview angle:** "JSON at the edge for compatibility and debuggability, Protobuf or Avro
internally for density and schema evolution" is the answer - but only when you can say *why*
each side needs what it needs. Otherwise it is a memorised pairing.

## Stretch

Add a version byte to your binary format and write a decoder that handles both v1 and v2
payloads. You have now built the minimum viable schema evolution, and you will understand field
tags the first time you read a `.proto` file.

## Checkpoint

```powershell
.\day.cmd 9
```
