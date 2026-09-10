package sd.p04.day38;

import java.util.Map;

/**
 * TODO(day38): route a {@link Notification} to the {@link NotificationSender} registered for
 * its channel, retrying that one channel's send up to {@code maxAttempts} times before giving
 * up - the Day 24 {@code RetryDecorator} shape, inlined here because today's exercise is about
 * the DISPATCHER, not about re-wrapping an existing decorator class.
 *
 * <p>{@code dispatch(notification)}:
 * <ol>
 *   <li>look up {@code senders.get(notification.channel())}; if absent,
 *       {@code IllegalArgumentException("no sender registered for channel: " + channel)} -
 *       loudly, not a silently-dropped notification</li>
 *   <li>call {@code sender.send(notification)}; on a {@link NotificationException}, retry, up to
 *       {@code maxAttempts} TOTAL attempts</li>
 *   <li>if every attempt fails, rethrow the LAST {@link NotificationException}</li>
 * </ol>
 *
 * <p>Adding a brand-new channel is registering one more entry in the map handed to the
 * constructor - nothing here needs to change, ever, for that to work. That is the "extensible"
 * half of today's title; the retry loop is the "with retries" half.
 */
public final class NotificationDispatcher {

    public NotificationDispatcher(Map<String, NotificationSender> senders, int maxAttempts) {
        throw new UnsupportedOperationException(
                "TODO(day38): validate maxAttempts >= 1, store the sender registry");
    }

    public void dispatch(Notification notification) {
        throw new UnsupportedOperationException(
                "TODO(day38): look up the sender, retry up to maxAttempts, rethrow the last failure");
    }
}
