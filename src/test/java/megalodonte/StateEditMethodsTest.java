package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateEditMethodsTest {

    private State<List<String>> listState;

    @BeforeEach
    void setUp() {
        listState = State.of(Arrays.asList("item1", "item2", "item3"));
    }

    @Test
    void set_byIndex_shouldReplaceItem() {
        // Given
        assertEquals(3, listState.get().size());
        assertEquals("item2", listState.get().get(1));

        // When
        listState.set(1, "item2-editado");

        // Then
        List<String> result = listState.get();
        assertEquals(3, result.size());
        assertEquals("item2-editado", result.get(1));
        assertEquals(Arrays.asList("item1", "item2-editado", "item3"), result);
    }

    @Test
    void set_invalidIndex_shouldThrowException() {
        // When & Then
        assertThrows(IndexOutOfBoundsException.class, () -> {
            listState.set(3, "invalid");
        });
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            listState.set(-1, "invalid");
        });
    }

    @Test
    void replace_existingItem_shouldReplace() {
        // Given
        assertEquals("item2", listState.get().get(1));

        // When
        boolean replaced = listState.replace("item2", "item2-novo");

        // Then
        assertTrue(replaced);
        List<String> result = listState.get();
        assertEquals("item2-novo", result.get(1));
        assertEquals(Arrays.asList("item1", "item2-novo", "item3"), result);
    }

    @Test
    void replace_nonExistingItem_shouldReturnFalse() {
        // Given
        List<String> original = listState.get();

        // When
        boolean replaced = listState.replace("nonexistent", "novo");

        // Then
        assertFalse(replaced);
        assertEquals(original, listState.get()); // Lista não mudou
    }

    @Test
    void indexOf_existingItem_shouldReturnIndex() {
        // When & Then
        assertEquals(0, listState.indexOf("item1"));
        assertEquals(1, listState.indexOf("item2"));
        assertEquals(2, listState.indexOf("item3"));
    }

    @Test
    void indexOf_nonExistingItem_shouldReturnMinusOne() {
        // When
        int result = listState.indexOf("nonexistent");

        // Then
        assertEquals(-1, result);
    }

    @Test
    void set_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.set(0, "item");
        });
    }

    @Test
    void replace_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.replace("old", "new");
        });
    }

    @Test
    void indexOf_onNonListState_shouldThrowException() {
        // Given
        State<String> stringState = State.of("not a list");

        // When & Then
        assertThrows(UnsupportedOperationException.class, () -> {
            stringState.indexOf("item");
        });
    }

    @Test
    void edit_methods_shouldTriggerSubscribers() {
        // Given
        boolean[] wasCalled = {false};
        listState.subscribe(items -> {
            wasCalled[0] = true;
        });

        // When
        listState.set(1, "edited");

        // Then
        assertTrue(wasCalled[0]);
    }

    @Test
    void replace_shouldTriggerSubscribers() {
        // Given
        boolean[] wasCalled = {false};
        listState.subscribe(items -> {
            wasCalled[0] = true;
        });

        // When
        listState.replace("item2", "edited");

        // Then
        assertTrue(wasCalled[0]);
    }
}