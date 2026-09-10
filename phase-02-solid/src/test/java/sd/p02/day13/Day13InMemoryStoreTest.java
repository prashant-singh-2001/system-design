package sd.p02.day13;

/** The reference implementation. This one passes from the start - that is the baseline. */
class Day13InMemoryStoreTest extends KeyValueStoreContract {

    @Override
    KeyValueStore newStore() {
        return new InMemoryStore();
    }
}
