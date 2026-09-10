package sd.p02.day11;

/** The other port: how the customer is told, without saying which channel. */
public interface ConfirmationSender {

    void sendConfirmation(String customerEmail, String orderId, long totalCents);
}
