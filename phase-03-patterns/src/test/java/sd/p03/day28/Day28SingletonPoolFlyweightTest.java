package sd.p03.day28;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day28SingletonPoolFlyweightTest {

    @Test
    @DisplayName("SINGLETON: every thread sees the exact same instance")
    void singletonIsSharedAcrossThreads() throws InterruptedException {
        int threadCount = 20;
        Set<AppConfig> seen = ConcurrentHashMap.newKeySet();
        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> seen.add(AppConfig.instance()));
        }
        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        assertThat(seen).as("every thread must have observed the SAME instance").hasSize(1);
    }

    @Test
    @DisplayName("SINGLETON: it actually holds something, not just an empty shell")
    void singletonHoldsSettings() {
        assertThat(AppConfig.instance().settings()).isNotNull();
    }

    @Test
    @DisplayName("POOL: borrowing up to capacity never blocks")
    void borrowUpToCapacitySucceeds() {
        ObjectPool<PooledResource> pool = new ObjectPool<>(PooledResource::new, 3);

        assertThat(pool.borrow()).isNotNull();
        assertThat(pool.borrow()).isNotNull();
        assertThat(pool.borrow()).isNotNull();
    }

    @Test
    @DisplayName("POOL: a released instance is reused rather than recreated")
    void releasedInstancesAreReused() {
        int before = PooledResource.createdCount();
        ObjectPool<PooledResource> pool = new ObjectPool<>(PooledResource::new, 1);

        PooledResource first = pool.borrow();
        pool.release(first);
        PooledResource second = pool.borrow();

        assertThat(second)
                .as("with capacity 1, the second borrow must reuse the released instance")
                .isSameAs(first);
        assertThat(PooledResource.createdCount() - before).isEqualTo(1);
    }

    @Test
    @DisplayName("POOL: borrowing beyond capacity blocks until something is released")
    void borrowBlocksBeyondCapacity() throws InterruptedException {
        ObjectPool<PooledResource> pool = new ObjectPool<>(PooledResource::new, 1);
        PooledResource onlyOne = pool.borrow();

        CountDownLatch borrowed = new CountDownLatch(1);
        Thread borrower = new Thread(() -> {
            pool.borrow();
            borrowed.countDown();
        });
        borrower.start();

        assertThat(borrowed.await(200, TimeUnit.MILLISECONDS))
                .as("with the pool exhausted, a second borrow must block")
                .isFalse();

        pool.release(onlyOne);

        assertThat(borrowed.await(1, TimeUnit.SECONDS))
                .as("releasing the outstanding item must unblock the waiting borrower")
                .isTrue();
        borrower.join();
    }

    @Test
    @DisplayName("FLYWEIGHT: the same ISO code always returns the identical shared instance")
    void factoryReturnsSharedInstance() {
        Country first = CountryFactory.get("US");
        Country second = CountryFactory.get("US");

        assertThat(second).isSameAs(first);
    }

    @Test
    @DisplayName("FLYWEIGHT: different codes are, correctly, different instances")
    void differentCodesAreDifferentInstances() {
        assertThat(CountryFactory.get("US")).isNotSameAs(CountryFactory.get("GB"));
    }

    @Test
    @DisplayName("FLYWEIGHT: repeated lookups of the same code do not keep constructing new ones")
    void repeatedLookupsDoNotGrowCreationCount() {
        CountryFactory.get("FR");
        int afterFirst = Country.createdCount();

        for (int i = 0; i < 50; i++) {
            CountryFactory.get("FR");
        }

        assertThat(Country.createdCount())
                .as("49 more lookups of an already-cached code must create nothing new")
                .isEqualTo(afterFirst);
    }

    @Test
    @DisplayName("FLYWEIGHT: an unknown code fails loudly")
    void unknownCodeThrows() {
        assertThatThrownBy(() -> CountryFactory.get("ZZ"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
