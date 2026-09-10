package sd.p03.day25;

import java.util.ArrayList;
import java.util.List;

/** GIVEN - the second subsystem the facade coordinates. Deliberately trivial. */
public final class ReceiptService {

    private final List<Receipt> receipts = new ArrayList<>();

    public void record(Receipt receipt) {
        receipts.add(receipt);
    }

    public List<Receipt> all() {
        return List.copyOf(receipts);
    }
}
