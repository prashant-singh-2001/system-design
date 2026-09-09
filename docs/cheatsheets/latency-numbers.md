# Latency numbers

Memorise the *orders of magnitude*, not the digits. On Day 1 you measure several of these
yourself, which is what makes them stick.

| Operation | Time | Relative |
|---|---|---|
| L1 cache reference | 1 ns | 1 |
| Branch mispredict | 3 ns | 3 |
| L2 cache reference | 4 ns | 4 |
| Mutex lock/unlock (uncontended) | 17 ns | 17 |
| Main memory reference | 100 ns | 100 |
| Compress 1 KB with Snappy | 2 us | 2,000 |
| Send 1 KB over 1 Gbps network | 10 us | 10,000 |
| SSD random read | 16 us | 16,000 |
| Read 1 MB sequentially from memory | 50 us | 50,000 |
| Round trip within a datacenter | 500 us | 500,000 |
| Read 1 MB sequentially from SSD | 1 ms | 1,000,000 |
| Disk seek (spinning) | 4 ms | 4,000,000 |
| Read 1 MB sequentially from disk | 20 ms | 20,000,000 |
| Round trip California to Netherlands | 150 ms | 150,000,000 |

## What to actually take from this

- **Memory is 100x faster than SSD; SSD is 20x faster than spinning disk.** This is why caching works.
- **Sequential beats random by 10-100x at every level.** This is why LSM trees, log-structured
  storage and batch writes exist.
- **A datacenter round trip costs 500 us.** Twenty sequential service calls is 10 ms of pure
  network. This is why fanout should be parallel, not serial.
- **Cross-continent is 150 ms and bounded by the speed of light.** No amount of engineering fixes
  it. You move the data closer, which is what a CDN is.

## Useful conversions

- 1 day = 86,400 s, near enough **100k seconds**
- 1 million requests/day = **~12 QPS**
- 1 billion requests/day = **~12,000 QPS**
- Peak is typically **2-3x** the daily average
- 1 million seconds = ~12 days; 1 billion seconds = ~32 years
