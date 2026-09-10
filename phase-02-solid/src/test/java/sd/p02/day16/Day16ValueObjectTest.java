package sd.p02.day16;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day16ValueObjectTest {

    @Test
    @DisplayName("value equality, not reference equality")
    void valueEquality() {
        assertThat(Money.of("GBP", 1_000)).isEqualTo(Money.of("GBP", 1_000));
        assertThat(Money.of("GBP", 1_000)).isNotEqualTo(Money.of("USD", 1_000));
        assertThat(Money.of("GBP", 1_000)).hasSameHashCodeAs(Money.of("GBP", 1_000));
    }

    @Test
    @DisplayName("arithmetic returns new instances and never mutates")
    void arithmetic() {
        Money ten = Money.of("GBP", 1_000);
        Money three = Money.of("GBP", 300);

        assertThat(ten.plus(three)).isEqualTo(Money.of("GBP", 1_300));
        assertThat(ten.minus(three)).isEqualTo(Money.of("GBP", 700));
        assertThat(ten.times(3)).isEqualTo(Money.of("GBP", 3_000));
        assertThat(three.minus(ten).isNegative()).isTrue();

        assertThat(ten)
                .as("the original must be untouched by every operation above")
                .isEqualTo(Money.of("GBP", 1_000));
    }

    @Test
    @DisplayName("mixing currencies is refused, loudly")
    void currencyMismatch() {
        assertThatThrownBy(() -> Money.of("GBP", 100).plus(Money.of("USD", 100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("GBP")
                .hasMessageContaining("USD");
    }

    @Test
    @DisplayName("a blank currency is not a currency")
    void currencyIsRequired() {
        assertThatThrownBy(() -> Money.of("", 100)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Money.of(null, 100)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("allocate: 100 into 3 is 34/33/33, and the pennies always add up")
    void allocateDistributesTheRemainder() {
        List<Money> shares = Money.of("GBP", 100).allocate(3);

        assertThat(shares).containsExactly(
                Money.of("GBP", 34), Money.of("GBP", 33), Money.of("GBP", 33));

        long sum = shares.stream().mapToLong(Money::minorUnits).sum();
        assertThat(sum).as("no penny may be created or destroyed").isEqualTo(100);
    }

    @Test
    @DisplayName("allocate holds for awkward splits too")
    void allocateEdgeCases() {
        assertThat(Money.of("GBP", 10).allocate(1)).containsExactly(Money.of("GBP", 10));
        assertThat(Money.of("GBP", 10).allocate(4)).containsExactly(
                Money.of("GBP", 3), Money.of("GBP", 3), Money.of("GBP", 2), Money.of("GBP", 2));

        for (int parts = 1; parts <= 7; parts++) {
            long sum = Money.of("GBP", 1_000).allocate(parts).stream()
                    .mapToLong(Money::minorUnits).sum();
            assertThat(sum).as("splitting into %d parts", parts).isEqualTo(1_000);
        }
    }

    @Test
    @DisplayName("the basket copies on the way in - the caller cannot reach inside afterwards")
    void defensiveCopyOnConstruction() {
        List<Money> mutable = new ArrayList<>(List.of(Money.of("GBP", 100)));
        Basket basket = new Basket("GBP", mutable);

        mutable.add(Money.of("GBP", 9_999));        // the caller still holds the list

        assertThat(basket.items())
                .as("if this basket grew, the constructor stored the caller's list rather than a copy")
                .hasSize(1);
        assertThat(basket.total()).isEqualTo(Money.of("GBP", 100));
    }

    @Test
    @DisplayName("and does not hand out its internals on the way out")
    void defensiveCopyOnRead() {
        Basket basket = new Basket("GBP", List.of(Money.of("GBP", 100)));

        assertThatThrownBy(() -> basket.items().add(Money.of("GBP", 1)))
                .as("items() must return an unmodifiable view or a copy")
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("an empty basket totals zero in its own currency")
    void emptyBasket() {
        assertThat(new Basket("EUR", List.of()).total()).isEqualTo(Money.of("EUR", 0));
    }
}
