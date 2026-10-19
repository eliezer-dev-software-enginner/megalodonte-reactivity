package megalodonte;

import megalodonte.v2.ListState;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComputedStateNullTest {
    @Test
    void unchangedNullValueDoesNotNotifySubscribers() {
        ListState<String> dependency = ListState.ofEmpty();
        try (var computed = ComputedState.of(() -> dependency.isEmpty() ? null : "present", dependency)) {
            var notifications = new AtomicInteger();
            computed.subscribe(value -> notifications.incrementAndGet());
            assertEquals(1, notifications.get());

            dependency.refresh();
            assertEquals(1, notifications.get());

            dependency.add("item");
            assertEquals(2, notifications.get());

            dependency.clear();
            assertEquals(3, notifications.get());
        }
    }
}
