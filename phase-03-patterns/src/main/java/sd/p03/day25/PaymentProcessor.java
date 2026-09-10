package sd.p03.day25;

/** The port the domain depends on. Whatever is on the other side of it is somebody else's SDK. */
@FunctionalInterface
public interface PaymentProcessor {

    PaymentResult charge(PaymentRequest request);
}
