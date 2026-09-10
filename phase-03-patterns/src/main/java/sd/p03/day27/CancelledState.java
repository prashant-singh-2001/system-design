package sd.p03.day27;

/** GIVEN - genuinely terminal. Nothing is legal from here, so nothing is overridden. */
public final class CancelledState extends OrderState {

    @Override
    public String name() {
        return "CANCELLED";
    }
}
