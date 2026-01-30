package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListStateUpdateIfTest {

    private ListState<String> stringListState;

    @BeforeEach
    void setUp() {
        stringListState = ListState.of(Arrays.asList("apple", "banana", "cherry"));
    }

    @Test
    @DisplayName("updateIf() should update matching items")
    void testUpdateIfShouldUpdateMatchingItems() {
        // Update "apple" to "APPLE" (uppercase)
        boolean updated = stringListState.updateIf(
            item -> item.equals("apple"),
            item -> item.toUpperCase()
        );
        
        assertTrue(updated);
        assertEquals(3, stringListState.get().size());
        assertEquals("APPLE", stringListState.get().get(0)); // apple was updated to APPLE
        assertEquals("banana", stringListState.get().get(1)); // banana unchanged
        assertEquals("cherry", stringListState.get().get(2)); // cherry unchanged
    }

    @Test
    @DisplayName("updateIf() should return false when no items match")
    void testUpdateIfShouldReturnFalseWhenNoMatch() {
        boolean updated = stringListState.updateIf(
            item -> item.equals("orange"),
            item -> item.toUpperCase()
        );
        
        assertFalse(updated);
        assertEquals(3, stringListState.get().size());
        // All items unchanged
        assertEquals("apple", stringListState.get().get(0));
        assertEquals("banana", stringListState.get().get(1));
        assertEquals("cherry", stringListState.get().get(2));
    }

    @Test
    @DisplayName("updateIf() should update multiple matching items")
    void testUpdateIfShouldUpdateMultipleItems() {
        // Update all items to uppercase
        boolean updated = stringListState.updateIf(
            item -> true, // match all items
            item -> item.toUpperCase()
        );
        
        assertTrue(updated);
        assertEquals(3, stringListState.get().size());
        assertEquals("APPLE", stringListState.get().get(0));
        assertEquals("BANANA", stringListState.get().get(1));
        assertEquals("CHERRY", stringListState.get().get(2));
    }

    @Test
    @DisplayName("updateIf() should work with custom updater function")
    void testUpdateIfWithCustomUpdater() {
        // Add prefix to "apple" items
        boolean updated = stringListState.updateIf(
            item -> item.startsWith("app"),
            oldItem -> "app" + oldItem
        );
        
        assertTrue(updated);
        assertEquals(3, stringListState.get().size());
        assertEquals("apple", stringListState.get().get(0)); // apple unchanged
        assertEquals("appbanana", stringListState.get().get(1)); // banana -> appbanana
        assertEquals("cherry", stringListState.get().get(2)); // cherry unchanged
    }

    @Test
    @DisplayName("updateIf() should be efficient for large lists")
    void testUpdateIfEfficiency() {
        // Setup large list
        for (int i = 0; i < 1000; i++) {
            stringListState.add("item" + i);
        }
        assertEquals(1000, stringListState.get().size());
        
        // Update only one item (should be O(n), not O(n²))
        long startTime = System.nanoTime();
        boolean updated = stringListState.updateIf(
            item -> item.equals("item500"),
            oldItem -> "updated" + oldItem
        );
        long endTime = System.nanoTime();
        
        assertTrue(updated);
        assertTrue((endTime - startTime) < 100_000_000); // Should complete quickly
    }

    @Test
    @DisplayName("updateIf() should trigger reactivity")
    void testUpdateIfReactivity() {
        final String[] lastValue = {""};
        final boolean[] wasCalled = {false};
        
        stringListState.subscribe(value -> {
            lastValue[0] = value.toString();
            wasCalled[0] = true;
        });
        
        // Perform update
        stringListState.updateIf(
            item -> item.equals("apple"),
            item -> "APPLE"
        );
        
        // Verify subscription was called
        assertTrue(lastValue[0].contains("APPLE"));
        assertTrue(wasCalled[0]);
    }

    @Test
    @DisplayName("updateIf() with null predicate should throw exception")
    void testUpdateIfWithNullPredicate() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringListState.updateIf(null, item -> item);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            stringListState.updateIf(item -> true, null);
        });
    }

    @Test
    @DisplayName("updateIf() with null updater should throw exception")
    void testUpdateIfWithNullUpdater() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringListState.updateIf(item -> true, null);
        });
    }
}