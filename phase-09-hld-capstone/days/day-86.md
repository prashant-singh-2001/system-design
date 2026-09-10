# Day 86 - Design: video streaming

**Phase 9 - HLD and capstone** | 45 minutes

## Concept (10 min)

Video is the design where **storage and bandwidth stop being footnotes and become the architecture**.

Three facts drive everything:

**A video is not stored once.** It is stored once per rendition. One upload becomes six files -
240p through 4K - and the ladder's total is what you pay for. Transcoding is minutes of CPU per
minute of video, which is why it is asynchronous and why nobody does it on the upload path. The
upload returns immediately; the video becomes playable later.

**Egress dominates.** For a video platform, bandwidth out is usually the largest cost - larger than
storage, larger than compute. Today's kernel puts a number on it: one file, stored once, served a
million times. That single fact is why **every serious video design is really a caching design**,
and why the CDN is not an optimisation you add at the end.

**Chunking is what makes adaptive bitrate possible.** The video is cut into a few seconds each, so
the player can pick a different rendition for the very next chunk. Without chunking, a network dip
is a stall; with it, the picture softens and playback continues. The player measures throughput and
under-selects deliberately - choosing a rendition that needs exactly your measured bandwidth
guarantees stalls, because the measurement is a noisy average.

The upload path is worth designing explicitly, because it is where the interesting failures are:
**chunked, resumable upload** to blob storage, then an event onto a queue, then a transcoding
worker pool, then a manifest published when the renditions are ready. Every arrow there is
something you built in Phase 7.

## Build (25 min)

**First (about 10 min)** implement `StreamingMath` in `src/main/java/sd/p09/day86/`: ladder storage,
monthly egress, adaptive rendition selection with a safety factor, and chunk counting.

**Then (about 15 min)** write `designs/video-streaming.md`. Assume 500 hours uploaded per minute,
2 billion views per day, and a global audience.

## Reflect (10 min)

1. Your CDN hit ratio drops from 95% to 85%. Compute what that does to origin egress, and decide
   whether it is an incident.
2. A video goes viral in a region with no nearby edge. What happens, and what would you do about it?
3. Transcoding a long video takes 40 minutes. What does the user see during that time, and what do
   you tell them?

**Interview angle:** "the dominant cost is egress, so the design is really about CDN hit ratio;
transcoding is async off a queue so upload latency is decoupled from processing" reframes the
question correctly in one sentence. Candidates who start with the database have not seen the bill.

## Stretch

Design the live-streaming variant. Almost every assumption changes: you cannot pre-transcode, the
latency budget is seconds rather than minutes, and the CDN must cache something that does not exist
yet. Work out which parts of your VOD design survive.

## Checkpoint

```powershell
.\day.cmd 86
```
