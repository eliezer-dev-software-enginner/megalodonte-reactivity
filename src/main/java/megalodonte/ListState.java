package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Specialized reactive state for list operations.
 * Provides type-safe list manipulation methods while maintaining reactivity.
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * ListState<String> names = ListState.of(Arrays.asList("Alice", "Bob"));
 * names.add("Charlie");
 * names.removeIf(name -> name.startsWith("A"));
 * }</pre>
 * 
 * @param <E> type of elements in the list
 * @author Eliezer
 * @since 1.0.0
 */
public class ListState<E> implements ReadableState<List<E>> {

    private List<E> value;
    private final List<Consumer<List<E>>> listeners = new ArrayList<>();

    public ListState(List<E> initial) {
        this.value = initial != null ? new ArrayList<>(initial) : new ArrayList<>();
    }

    /**
     * Creates a new ListState with the specified initial list.
     * 
     * @param <E> type of elements
     * @param initial initial list
     * @return a new ListState instance
     */
    public static <E> ListState<E> of(List<E> initial) {
        return new ListState<>(initial);
    }

    /**
     * Returns the current list value of this state.
     * 
     * @return current list
     */
    public List<E> get() {
        return value;
    }

    /**
     * Sets a new list for this state and notifies all subscribers if the list changed.
     * 
     * @param newList new list to set
     */
    public void set(List<E> newList) {
        if (Objects.equals(this.value, newList)) {
            return; // ← proteção centralizada
        }

        this.value = newList;

        for (var listener : List.copyOf(listeners)) {
            listener.accept(newList);
        }
    }

    /**
     * Subscribes to list changes and immediately calls the listener with the current list.
     * 
     * @param listener to be notified of list changes
     */
    public void subscribe(Consumer<List<E>> listener) {
        listeners.add(listener);
        listener.accept(value);
    }

    /**
     * Adds an item to the list.
     * 
     * @param item item to be added
     */
    public void add(E item) {
        List<E> newList = new ArrayList<>(value);
        newList.add(item);
        set(newList);
    }

    /**
     * Adds all items from the specified collection to the list.
     * 
     * @param items collection of items to be added
     * @throws IllegalArgumentException if items is null
     */
    public void addAll(java.util.Collection<? extends E> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        
        List<E> newList = new ArrayList<>(value);
        newList.addAll(items);
        set(newList);
    }

    /**
     * Adds all items from the specified array to the list.
     * 
     * @param items array of items to be added
     * @throws IllegalArgumentException if items is null
     */
    public void addAll(E[] items) {
        if (items == null) {
            throw new IllegalArgumentException("Items array cannot be null");
        }
        
        List<E> newList = new ArrayList<>(value);
        java.util.Collections.addAll(newList, items);
        set(newList);
    }

    /**
     * Removes the last item from the list.
     */
    public void removeLast() {
        if (value.isEmpty()) {
            return;
        }
        
        List<E> newList = new ArrayList<>(value);
        newList.remove(newList.size() - 1);
        set(newList);
    }

    /**
     * Clears all items from the list.
     */
    public void clear() {
        set(new ArrayList<>());
    }

    /**
     * Removes items from the list that match the specified predicate.
     * 
     * @param filter predicate to remove items
     */
    public void removeIf(Predicate<E> filter) {
        List<E> newList = new ArrayList<>();
        
        for (E item : value) {
            if (!filter.test(item)) {
                newList.add(item);
            }
        }
        
        set(newList);
    }

    /**
     * Removes the first occurrence of the specified item from the list.
     * 
     * @param item item to be removed
     * @return true if the item was removed, false otherwise
     */
    public boolean remove(E item) {
        List<E> newList = new ArrayList<>(value);
        boolean removed = newList.remove(item);
        
        if (removed) {
            set(newList);
        }
        
        return removed;
    }

    /**
     * Removes all items from the list that are present in the specified collection.
     * 
     * @param items collection of items to be removed
     * @return true if the list changed as a result of this call
     * @throws IllegalArgumentException if items is null
     */
    public boolean removeAll(java.util.Collection<? extends E> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        
        List<E> newList = new ArrayList<>(value);
        boolean changed = newList.removeAll(items);
        
        if (changed) {
            set(newList);
        }
        
        return changed;
    }

    /**
     * Retains only the items in the list that are contained in the specified collection.
     * 
     * @param items collection of items to be retained
     * @return true if the list changed as a result of this call
     * @throws IllegalArgumentException if items is null
     */
    public boolean retainAll(java.util.Collection<? extends E> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        
        List<E> newList = new ArrayList<>(value);
        boolean changed = newList.retainAll(items);
        
        if (changed) {
            set(newList);
        }
        
        return changed;
    }

    /**
     * Replaces an item at a specific position in the list.
     * 
     * @param index index of the item to be replaced
     * @param newItem new item
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public void set(int index, E newItem) {
        if (index < 0 || index >= value.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        
        List<E> newList = new ArrayList<>(value);
        newList.set(index, newItem);
        set(newList);
    }

    /**
     * Replaces the first occurrence of a specified item in the list.
     * 
     * @param oldItem item to be replaced
     * @param newItem new item
     * @return true if the item was found and replaced, false otherwise
     */
    public boolean replace(E oldItem, E newItem) {
        List<E> newList = new ArrayList<>(value);
        int index = newList.indexOf(oldItem);
        
        if (index != -1) {
            newList.set(index, newItem);
            set(newList);
            return true;
        }
        
        return false;
    }

    /**
     * Finds the index of an item in the list.
     * 
     * @param item item to be found
     * @return index of the item, or -1 if not found
     */
    public int indexOf(E item) {
        return value.indexOf(item);
    }

    /**
     * Checks if the list contains the specified item.
     * 
     * @param item item to check for presence
     * @return true if the list contains the item
     */
    public boolean contains(E item) {
        return value.contains(item);
    }

    /**
     * Checks if the list contains all items from the specified collection.
     * 
     * @param items collection of items to check for presence
     * @return true if the list contains all items
     * @throws IllegalArgumentException if items is null
     */
    public boolean containsAll(java.util.Collection<? extends E> items) {
        if (items == null) {
            throw new IllegalArgumentException("Items collection cannot be null");
        }
        return value.containsAll(items);
    }

    /**
     * Returns the number of items in the list.
     * 
     * @return the number of items in the list
     */
    public int size() {
        return value.size();
    }

    /**
     * Checks if the list is empty.
     * 
     * @return true if the list is empty
     */
    public boolean isEmpty() {
        return value.isEmpty();
    }

    /**
     * Returns the item at the specified position in the list.
     * 
     * @param index index of the item to return
     * @return the item at the specified position
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    public E get(int index) {
        return value.get(index);
    }
}