package megalodonte.v2;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ListStateRemoveIfTest {
    @Test
    void notifiesOnlyWhenAnItemIsRemoved() {
        var state = ListState.of(List.of("Ada", "Grace"));
        var original = state.get();
        var notifications = new AtomicInteger();
        state.onChange(items -> notifications.incrementAndGet());

        state.removeIf(name -> name.equals("missing"));
        assertSame(original, state.get());
        assertEquals(0, notifications.get());

        state.removeIf(name -> name.equals("Ada"));
        assertEquals(List.of("Grace"), state.get());
        assertEquals(1, notifications.get());
    }
}
