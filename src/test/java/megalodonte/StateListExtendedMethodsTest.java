package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateListExtendedMethodsTest {

    private State<List<String>> listState;

    @BeforeEach
    void setUp() {
        listState = State.of(Arrays.asList("item1", "item2", "item3"));
    }

    @Test
    void removeIf_shouldRemoveMatchingItems() {
        // Given
        assertEquals(3, listState.get().size());

        // When - remove itens que contêm "item2"
        listState.removeIf(item -> ((String) item).contains("item2"));

        // Then
        List<String> result = listState.get();
        assertEquals(2, result.size());
        assertTrue(result.contains("item1"));
        assertFalse(result.contains("item2"));
        assertTrue(result.contains("item3"));
    }

    @Test
    void removeIf_shouldRemoveMultipleMatchingItems() {
        // Given
        listState.add("item4");
        listState.add("item5");

        // When - remove todos os itens que começam com "item1" ou "item3"
        listState.removeIf(item -> {
            String str = (String) item;
            return str.startsWith("item1") || str.startsWith("item3");
        });

        // Then
        List<String> result = listState.get();
        assertEquals(3, result.size());
        assertFalse(result.contains("item1"));
        assertTrue(result.contains("item2"));
        assertFalse(result.contains("item3"));
        assertTrue(result.contains("item4"));
        assertTrue(result.contains("item5"));
    }

    @Test
    void removeIf_shouldNotRemoveWhenNoMatch() {
        // Given
        assertEquals(3, listState.get().size());

        // When - remove itens que não existem
        listState.removeIf(item -> ((String) item).contains("xyz"));

        // Then
        List<String> result = listState.get();
        assertEquals(3, result.size());
        assertEquals(Arrays.asList("item1", "item2", "item3"), result);
    }

    @Test
    void removeIf_withEmptyList_shouldDoNothing() {
        // Given
        listState.clear();
        assertEquals(0, listState.get().size());

        // When
        listState.removeIf(item -> true);

        // Then
        assertEquals(0, listState.get().size());
    }

    @Test
    void remove_shouldRemoveSpecificItem() {
        // Given
        assertEquals(3, listState.get().size());

        // When
        boolean removed = listState.remove("item2");

        // Then
        assertTrue(removed);
        List<String> result = listState.get();
        assertEquals(2, result.size());
        assertEquals(Arrays.asList("item1", "item3"), result);
    }

    @Test
    void remove_nonExistentItem_shouldReturnFalse() {
        // Given
        assertEquals(3, listState.get().size());

        // When
        boolean removed = listState.remove("nonexistent");

        // Then
        assertFalse(removed);
        List<String> result = listState.get();
        assertEquals(3, result.size());
        assertEquals(Arrays.asList("item1", "item2", "item3"), result);
    }

    @Test
    void remove_onEmptyList_shouldReturnFalse() {
        // Given
        listState.clear();
        assertEquals(0, listState.get().size());

        // When
        boolean removed = listState.remove("anything");

        // Then
        assertFalse(removed);
        assertEquals(0, listState.get().size());
    }

    @Test
    void removeIf_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.removeIf(item -> true);
        });
    }

    @Test
    void remove_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.remove("item");
        });
    }

    @Test
    void removeIf_shouldTriggerSubscribers() {
        // Given
        boolean[] wasCalled = {false};
        listState.subscribe(items -> {
            wasCalled[0] = true;
        });

        // When
        listState.removeIf(item -> ((String) item).equals("item2"));

        // Then
        assertTrue(wasCalled[0]);
    }

    @Test
    void remove_shouldTriggerSubscribers() {
        // Given
        boolean[] wasCalled = {false};
        listState.subscribe(items -> {
            wasCalled[0] = true;
        });

        // When
        listState.remove("item2");

        // Then
        assertTrue(wasCalled[0]);
    }
}