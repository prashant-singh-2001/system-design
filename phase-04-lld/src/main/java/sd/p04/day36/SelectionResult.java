package sd.p04.day36;

/**
 * Everything that can happen after a valid {@code selectItem} call, as DATA - the Day 19
 * lesson, applied here. "Wrong code", "out of stock" and "not enough money" are all things a
 * vending machine deals with dozens of times a day; they are not protocol violations, so they
 * are not exceptions. Calling {@code selectItem} with no coins inserted, by contrast, IS a
 * protocol violation - see {@link VendingMachine}.
 */
public sealed interface SelectionResult {

    record Dispensed(String itemName, long changeCents) implements SelectionResult {
    }

    record InsufficientFunds(long shortfallCents) implements SelectionResult {
    }

    record OutOfStock(String itemCode) implements SelectionResult {
    }

    record UnknownItem(String itemCode) implements SelectionResult {
    }
}
