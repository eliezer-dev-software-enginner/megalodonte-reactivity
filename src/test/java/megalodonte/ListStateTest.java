package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ListStateTest {

    private ListState<String> stringListState;
    private ListState<Integer> intListState;

    @BeforeEach
    void setUp() {
        stringListState = ListState.of(Arrays.asList("A", "B", "C"));
        intListState = ListState.of(Arrays.asList(1, 2, 3));
    }

    @Test
    @DisplayName("add() should add item and trigger reactivity")
    void testAdd() {
        stringListState.add("D");
        assertEquals(4, stringListState.get().size());
        assertTrue(stringListState.get().contains("D"));
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
    @DisplayName("remove() should remove item")
    void testRemove() {
        boolean removed = stringListState.remove("B");
        
        assertTrue(removed);
        assertEquals(2, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "C")));
        assertFalse(stringListState.get().contains("B"));
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
    @DisplayName("retainAll() should retain only specified items")
    void testRetainAll() {
        List<String> toRetain = Arrays.asList("A", "C");
        boolean changed = stringListState.retainAll(toRetain);
        
        assertTrue(changed);
        assertEquals(2, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "C")));
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
    @DisplayName("size() and isEmpty() should work correctly")
    void testSizeAndIsEmpty() {
        assertEquals(3, stringListState.size());
        assertFalse(stringListState.isEmpty());
        
        stringListState.clear();
        assertEquals(0, stringListState.size());
        assertTrue(stringListState.isEmpty());
    }

    @Test
    @DisplayName("get() should return correct item")
    void testGet() {
        assertEquals("A", stringListState.get(0));
        assertEquals("B", stringListState.get(1));
        assertEquals("C", stringListState.get(2));
        
        assertThrows(IndexOutOfBoundsException.class, () -> {
            stringListState.get(3);
        });
    }

    @Test
    @DisplayName("set() with index should work correctly")
    void testSetAtIndex() {
        stringListState.set(1, "X");
        
        assertEquals("X", stringListState.get(1));
        assertEquals(Arrays.asList("A", "X", "C"), stringListState.get());
    }

    @Test
    @DisplayName("replace() should work correctly")
    void testReplace() {
        boolean replaced = stringListState.replace("B", "X");
        
        assertTrue(replaced);
        assertEquals(Arrays.asList("A", "X", "C"), stringListState.get());
        assertTrue(stringListState.get().contains("X"));
        assertFalse(stringListState.get().contains("B"));
    }

    @Test
    @DisplayName("removeIf() should work with predicate")
    void testRemoveIf() {
        stringListState.removeIf(item -> item.equals("B"));
        
        assertEquals(2, stringListState.get().size());
        assertTrue(stringListState.get().containsAll(Arrays.asList("A", "C")));
        assertFalse(stringListState.get().contains("B"));
    }

    @Test
    @DisplayName("reactivity should work")
    void testReactiveUpdates() {
        final String[] lastValue = {""};
        stringListState.subscribe(value -> lastValue[0] = value.toString());
        
        // Test add
        stringListState.add("D");
        assertTrue(lastValue[0].contains("D"));
        
        // Test removeAll
        stringListState.removeAll(Arrays.asList("A", "B"));
        assertFalse(lastValue[0].contains("A"));
    }

    @Test
    @DisplayName("complex operations scenario")
    void testComplexScenario() {
        ListState<String> todos = ListState.of(Arrays.asList("Task 1", "Task 2"));
        
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