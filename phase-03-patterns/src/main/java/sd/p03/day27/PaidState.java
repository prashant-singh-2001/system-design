package sd.p03.day27;

/**
 * GIVEN - a terminal-for-today state that overrides NOTHING. Every one of {@code submit},
 * {@code pay} and {@code cancel} falls straight through to {@link OrderState}'s default refusal.
 * That absence of overrides IS the rule "a paid order cannot be cancelled" - there is no code
 * path left to find and get wrong.
 */
public final class PaidState extends OrderState {

    @Override
    public String name() {
        return "PAID";
    }
}
