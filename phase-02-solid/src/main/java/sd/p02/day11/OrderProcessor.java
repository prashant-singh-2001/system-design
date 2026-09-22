package sd.p02.day11;

/**
 * TODO(day11): the coordinator that replaces {@link LegacyOrderProcessor}.
 *
 * <p>
 * Take the validator, the pricing service, and both ports as constructor
 * parameters. Then
 * {@code process} becomes four lines: validate, price, save, notify - and
 * return the total.
 *
 * <p>
 * Notice what happened. This class now has ONE reason to change: the steps of
 * the workflow.
 * The tax rate changing does not touch it. Swapping Postgres for DynamoDB does
 * not touch it.
 * Moving from email to SMS does not touch it.
 *
 * <p>
 * That is the test for SRP, and it is not "how many lines is this class". It is
 * "how many
 * independent teams could each force a change here".
 */
public final class OrderProcessor {

    private final OrderValidator validator;
    private final PricingService pricing;
    private final OrderRepository repository;
    private final ConfirmationSender sender;

    public OrderProcessor(OrderValidator validator,
            PricingService pricing,
            OrderRepository repository,
            ConfirmationSender sender) {
        this.validator = validator;
        this.pricing = pricing;
        this.repository = repository;
        this.sender = sender;
    }

    public long process(Order order) {
        validator.validate(order);
        long total = pricing.totalCents(order);
        repository.save(order,total);
        sender.sendConfirmation(order.customerEmail(), order.id(), total);
        return total;
    }
}
