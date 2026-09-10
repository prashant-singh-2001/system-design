# Day 59 - HTTP caching and the CDN

**Phase 6 - Caching** | 45 minutes

## Concept (10 min)

`Cache-Control` is the largest cache you will ever operate. Every browser, every proxy and every
CDN edge obeys it. One correct header removes more load than an entire Redis tier - and one
wrong header serves a logged-in user's page to a stranger.

The directives, and what people get wrong about them:

- **`no-store`** - never write this down anywhere. For genuinely sensitive responses.
- **`no-cache`** - badly named. It means *store it, but revalidate before every reuse*. It does
  not mean "do not cache"; that is `no-store`.
- **`private`** - browsers may cache it, shared caches (CDNs, proxies) must not. This one
  directive is what stands between a per-user response and a CDN serving it to everybody.
  Treat it as a correctness rule, not a performance tweak.
- **`max-age=N`** - fresh for N seconds.
- **`s-maxage=N`** - like `max-age`, but only for shared caches, and it *overrides* `max-age`
  there. This is how you say "browsers 60 seconds, CDN one hour" - a very useful asymmetry,
  because you can purge a CDN and you cannot purge a browser.
- **`must-revalidate`** - once stale, never serve without checking.

Then **revalidation**: the client sends `If-None-Match: <etag>`, and if it matches, the origin
returns `304 Not Modified` with no body. You still pay the round trip but not the bytes - for a
large asset on a slow link, that is most of the cost.

**The trade-off, and it is the sharpest one in caching:** a long `max-age` is enormously
effective and completely irreversible. You cannot recall a response a browser has cached for a
year. That asymmetry is why the standard pattern is immutable, content-hashed asset URLs
(`app.a1b2c3.js`) with a one-year `max-age`, and a short TTL on the HTML that references them.
The hash makes the URL change instead of the content.

## Build (25 min)

In `src/main/java/sd/p06/day59/`:

1. `CacheControl.parse` / `format` - comma-separated, case-insensitive, **ignore unrecognised
   directives** (that is how HTTP stays extensible), and treat a malformed `max-age=abc` as
   absent rather than fatal. A bad header should not take a page down.
2. `HttpCachePolicy.isStorable` - `no-store` for anyone; `private` for shared caches.
3. `HttpCachePolicy.evaluate` - no-cache, then lifetime (`s-maxage` for shared, else `max-age`),
   then age. **No freshness information means revalidate** - absence is not permission.
4. `isNotModified` - the ETag comparison.

## Reflect (10 min)

1. Write the header you would send for: a content-hashed JS bundle, a logged-in dashboard, a
   public product page that changes hourly.
2. You shipped `max-age=31536000` on your HTML by mistake. What can you actually do about it?
3. `s-maxage` lets the CDN hold something far longer than the browser. Why is that asymmetry
   useful rather than just confusing?

**Interview angle:** "static assets get content-hashed filenames and a one-year immutable
max-age; HTML gets a short TTL with ETag revalidation; anything per-user is private" is a
complete CDN caching story in three clauses.

## Stretch

Implement `stale-while-revalidate`: serve the stale response immediately and refresh in the
background. That is Day 54's early recompute, standardised as an HTTP directive - the same idea
arriving from a different direction.

## Checkpoint

```powershell
.\day.cmd 59
```
