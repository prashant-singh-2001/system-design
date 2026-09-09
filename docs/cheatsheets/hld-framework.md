# The 45-minute HLD framework

The single most common failure is spending 25 minutes on requirements and never drawing the
system. Manage the clock out loud: "I have used ten minutes, let me move to the architecture."

| Minutes | Phase | What good looks like |
|---|---|---|
| 0-5 | **Scope** | Ask 3-4 sharp questions, then *state the scope yourself* and get agreement |
| 5-10 | **Estimate** | QPS, storage, read:write ratio. Say which numbers will drive design |
| 10-15 | **API + data model** | Signatures and the partition key, with justification |
| 15-25 | **Architecture** | Draw it. Then trace one read and one write through the boxes |
| 25-35 | **Deep dive** | The hard part. This is most of your score |
| 35-45 | **Bottlenecks** | What breaks at 10x, what happens when each box dies |

## Questions worth asking at minute 2

- How many users, and how active?
- Read-heavy or write-heavy?
- How strong does consistency need to be, and *for which operation*?
- What is the latency budget?
- Global or single region?

## Phrases that signal seniority

- "I am going to assume X - tell me if that is wrong." (unblocks yourself)
- "That is out of scope for now; I will come back if there is time." (scoping)
- "This is a read-heavy system at 50:1, so I will optimise the read path first." (numbers to decisions)
- "The trade-off here is consistency against latency. Given the requirement, I choose ..." (naming the axis)
- "This will break first when ..." (knowing your own design's limits)

## Phrases that signal the opposite

- "We will just use Kafka." (tool named before problem stated)
- "It will scale." (no mechanism given)
- Listing every technology you know.
- Silence while thinking. Narrate instead - they are assessing reasoning, not answers.

## The default architecture to deviate from

```
Client -> CDN -> Load Balancer -> API Gateway -> Service
                                                   |
                                    +--------------+--------------+
                                    |              |              |
                                  Cache        Database        Queue -> Workers
                                 (Redis)      (primary +               |
                                               replicas)          Blob storage
```

Start here, then justify each deviation. Do not start from a blank page.
