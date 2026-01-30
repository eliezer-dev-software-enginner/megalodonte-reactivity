package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StateNewListMethodsTest {

    private State<List<String>> stringListState;
    private State<List<Integer>> intListState;

    @BeforeEach
    void setUp() {
        stringListState = State.of(Arrays.asList("A", "B", "C"));
        intListState = State.of(Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("addAll() with collection should add all items")
    void testAddAllWithCollection() {
        List<String> toAdd = Arrays.asList("D", "E", "F");
        stringListState.addAll(toAdd);
        
        assertEquals(6, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "B", "C", "D", "E", "F")));
    }

    @Test
    @DisplayName("addAll() with array should add all items")
    void testAddAllWithArray() {
        String[] toAdd = {"D", "E", "F"};
        stringListState.addAll(toAdd);
        
        assertEquals(6, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "B", "C", "D", "E", "F")));
    }

    @Test
    @DisplayName("addAll() should throw for null collection")
    void testAddAllWithNullCollection() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringListState.addAll((List<?>) null);
        });
    }

    @Test
    @DisplayName("addAll() should throw for null array")
    void testAddAllWithNullArray() {
        assertThrows(IllegalArgumentException.class, () -> {
            stringListState.addAll((Object[]) null);
        });
    }

    @Test
    @DisplayName("removeAll() should remove all specified items")
    void testRemoveAll() {
        List<String> toRemove = Arrays.asList("A", "C");
        boolean changed = stringListState.removeAll(toRemove);
        
        assertTrue(changed);
        assertEquals(1, stringListState.get().size());
        assertTrue(stringListState.get().contains("B"));
        assertFalse(stringListState.get().contains("A"));
        assertFalse(stringListState.get().contains("C"));
    }

    @Test
    @DisplayName("removeAll() should return false when no items removed")
    void testRemoveAllNoChange() {
        List<String> toRemove = Arrays.asList("X", "Y");
        boolean changed = stringListState.removeAll(toRemove);
        
        assertFalse(changed);
        assertEquals(3, stringListState.get().size());
    }

    @Test
    @DisplayName("retainAll() should retain only specified items")
    void testRetainAll() {
        List<String> toRetain = Arrays.asList("A", "C");
        boolean changed = stringListState.retainAll(toRetain);
        
        assertTrue(changed);
        assertEquals(2, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "C")));
    }

    @Test
    @DisplayName("retainAll() should return false when no items removed")
    void testRetainAllNoChange() {
        List<String> toRetain = Arrays.asList("A", "B", "C");
        boolean changed = stringListState.retainAll(toRetain);
        
        assertFalse(changed);
        assertEquals(3, stringListState.get().size());
    }

    @Test
    @DisplayName("contains() should check item presence")
    void testContains() {
        assertTrue(stringListState.contains("A"));
        assertTrue(stringListState.contains("B"));
        assertFalse(stringListState.contains("X"));
    }

    @Test
    @DisplayName("containsAll() should check all items presence")
    void testContainsAll() {
        assertTrue(stringListState.containsAll(Arrays.asList("A", "B")));
        assertFalse(stringListState.containsAll(Arrays.asList("A", "X")));
        assertFalse(stringListState.containsAll(Arrays.asList("X", "Y")));
    }

    @Test
    @DisplayName("size() should return list size")
    void testSize() {
        assertEquals(3, stringListState.size());
        assertEquals(3, intListState.size());
        
        stringListState.add("D");
        assertEquals(4, stringListState.size());
    }

    @Test
    @DisplayName("isEmpty() should check if list is empty")
    void testIsEmpty() {
        assertFalse(stringListState.isEmpty());
        
        stringListState.clear();
        assertTrue(stringListState.isEmpty());
    }

    @Test
    @DisplayName("get() should return item at index")
    void testGet() {
        assertEquals("A", stringListState.get(0));
        assertEquals("B", stringListState.get(1));
        assertEquals("C", stringListState.get(2));
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            stringListState.get(3);
        });
    }

    @Test
    @DisplayName("list operations should trigger reactive updates")
    void testReactiveUpdates() {
        final String[] lastValue = {""};
        stringListState.subscribe(value -> lastValue[0] = value.toString());
        
        // Test addAll
        stringListState.addAll(Arrays.asList("D", "E"));
        assertTrue(lastValue[0].contains("D"));
        
        // Test removeAll
        stringListState.removeAll(Arrays.asList("A", "B"));
        assertFalse(lastValue[0].contains("A"));
        
        // Test retainAll
        stringListState.retainAll(Arrays.asList("C"));
        assertEquals(1, stringListState.size());
    }

    @Test
    @DisplayName("list operations should throw when state doesn't contain list")
    void testListOperationsOnNonListState() {
        State<String> stringState = State.of("not a list");
        
        assertThrows(UnsupportedOperationException.class, () -> stringState.add("item"));
        assertThrows(UnsupportedOperationException.class, () -> stringState.addAll(Arrays.asList("item")));
        assertThrows(UnsupportedOperationException.class, () -> stringState.remove("item"));
        assertThrows(UnsupportedOperationException.class, () -> stringState.removeAll(Arrays.asList("item")));
        assertThrows(UnsupportedOperationException.class, () -> stringState.retainAll(Arrays.asList("item")));
        assertThrows(UnsupportedOperationException.class, () -> stringState.contains("item"));
        assertThrows(UnsupportedOperationException.class, () -> stringState.containsAll(Arrays.asList("item")));
        assertThrows(UnsupportedOperationException.class, () -> stringState.size());
        assertThrows(UnsupportedOperationException.class, () -> stringState.isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> stringState.get(0));
        assertThrows(UnsupportedOperationException.class, () -> stringState.set(0, "item"));
    }

    @Test
    @DisplayName("complex operations scenario")
    void testComplexScenario() {
        State<List<String>> todos = State.of(Arrays.asList("Task 1", "Task 2"));
        
        // Add multiple tasks
        todos.addAll(Arrays.asList("Task 3", "Task 4", "Task 5"));
        assertEquals(5, todos.size());
        
        // Remove completed tasks
        todos.removeAll(Arrays.asList("Task 1", "Task 3"));
        assertEquals(3, todos.size());
        
        // Check if remaining tasks exist
        assertTrue(todos.containsAll(Arrays.asList("Task 2", "Task 4", "Task 5")));
        
        // Get specific task
        assertEquals("Task 4", todos.get(1));
        
        // Keep only high priority tasks
        todos.retainAll(Arrays.asList("Task 4", "Task 5"));
        assertEquals(2, todos.size());
        
        // Verify final state
        assertTrue(todos.contains("Task 4"));
        assertTrue(todos.contains("Task 5"));
        assertFalse(todos.contains("Task 2"));
    }
}