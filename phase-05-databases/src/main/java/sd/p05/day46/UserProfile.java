package sd.p05.day46;

import java.util.List;

/** The one thing a "recent activity" page actually wants, however it was assembled underneath. */
public record UserProfile(long userId, String name, String email, List<OrderSummary> recentOrders) {
}
