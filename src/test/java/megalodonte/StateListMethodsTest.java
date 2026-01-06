package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateListMethodsTest {

    private State<List<String>> listState;

    @BeforeEach
    void setUp() {
        listState = State.of(Arrays.asList("item1", "item2"));
    }

    @Test
    void add_shouldAddItemToList() {
        // Given
        assertEquals(2, listState.get().size());

        // When
        listState.add("item3");

        // Then
        List<String> result = listState.get();
        assertEquals(3, result.size());
        assertTrue(result.contains("item3"));
        assertEquals(Arrays.asList("item1", "item2", "item3"), result);
    }

    @Test
    void removeLast_shouldRemoveLastItem() {
        // Given
        assertEquals(2, listState.get().size());

        // When
        listState.removeLast();

        // Then
        List<String> result = listState.get();
        assertEquals(1, result.size());
        assertEquals(Arrays.asList("item1"), result);
    }

    @Test
    void removeLast_onEmptyList_shouldDoNothing() {
        // Given
        listState.clear();
        assertEquals(0, listState.get().size());

        // When
        listState.removeLast();

        // Then
        assertEquals(0, listState.get().size());
    }

    @Test
    void clear_shouldEmptyList() {
        // Given
        assertEquals(2, listState.get().size());

        // When
        listState.clear();

        // Then
        List<String> result = listState.get();
        assertEquals(0, result.size());
        assertTrue(result.isEmpty());
    }

    @Test
    void add_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.add("item");
        });
    }

    @Test
    void removeLast_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.removeLast();
        });
    }

    @Test
    void clear_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.clear();
        });
    }

    @Test
    void add_shouldTriggerSubscribers() {
        // Given
        boolean[] wasCalled = {false};
        listState.subscribe(items -> {
            wasCalled[0] = true;
        });

        // When
        listState.add("new item");

        // Then
        assertTrue(wasCalled[0]);
    }
}