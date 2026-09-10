package sd.p04.day31;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class Day31MinStackTest {

    @Test
    @DisplayName("push/pop/top behave like a normal stack")
    void basicStackBehaviour() {
        MinStack stack = new MinStack();

        stack.push(1);
        stack.push(2);

        assertThat(stack.top()).isEqualTo(2);
        assertThat(stack.pop()).isEqualTo(2);
        assertThat(stack.top()).isEqualTo(1);
    }

    @Test
    @DisplayName("getMin tracks the minimum of what is currently on the stack")
    void getMinTracksCurrentMinimum() {
        MinStack stack = new MinStack();

        stack.push(5);
        stack.push(3);
        stack.push(7);

        assertThat(stack.getMin()).isEqualTo(3);
    }

    @Test
    @DisplayName("popping the minimum reveals the PREVIOUS minimum, not the smallest ever seen")
    void getMinAfterPoppingTheMinimum() {
        MinStack stack = new MinStack();

        stack.push(5);
        stack.push(3);
        stack.push(7);
        stack.pop();               // pops 7, min is still 3
        assertThat(stack.getMin()).isEqualTo(3);

        stack.pop();               // pops 3, min must fall back to 5
        assertThat(stack.getMin()).isEqualTo(5);
    }

    @Test
    @DisplayName("a duplicate minimum is handled correctly - popping one copy leaves the other")
    void duplicateMinimumSurvivesOnePop() {
        MinStack stack = new MinStack();

        stack.push(5);
        stack.push(3);
        stack.push(3);
        stack.push(7);

        stack.pop();                // pops 7
        assertThat(stack.getMin()).isEqualTo(3);

        stack.pop();                // pops one of the two 3s
        assertThat(stack.getMin())
                .as("the other 3 is still on the stack")
                .isEqualTo(3);

        stack.pop();                // pops the last 3
        assertThat(stack.getMin()).isEqualTo(5);
    }

    @Test
    @DisplayName("every operation on an empty stack fails loudly")
    void emptyStackOperationsThrow() {
        MinStack stack = new MinStack();

        assertThat(stack.isEmpty()).isTrue();
        assertThatThrownBy(stack::pop).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(stack::top).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(stack::getMin).isInstanceOf(IllegalStateException.class);
    }
}
