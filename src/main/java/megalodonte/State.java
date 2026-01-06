package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Mutable reactive state holder that notifies subscribers when its value changes.
 * 
 * <p>State is the core building block for reactive programming in this library.
 * It holds a value of type T and allows mutations through {@code set()} method.
 * Whenever the value changes, all registered subscribers are automatically notified.</p>
 * 
 * <p>For list operations, use specialized methods:
 * {@link #add(Object)}, {@link #remove(Object)}, {@link #removeLast()},
 * {@link #removeIf(Predicate)}, {@link #set(int, Object)},
 * {@link #replace(Object, Object)}, {@link #indexOf(Object)}, {@link #clear()}</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * State<String> name = State.of("John");
 * name.subscribe(newName -> System.out.println("Name: " + newName));
 * name.set("Jane"); // Triggers subscription
 * 
 * State<List<String>> items = State.of(Arrays.asList("A", "B"));
 * items.add("C"); // Reactive list operation
 * }</pre>
 * 
 * @param <T> type of value held by this state
 * @author Eliezer
 * @since 1.0.0
 */
public class State<T> implements ReadableState<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public State(T initial) {
        this.value = initial;
    }

    /**
     * Creates a new State with the specified initial value.
     * 
     * @param <T> type of value
     * @param initial initial value
     * @return a new State instance
     */
    public static <T> State<T> of(T initial) {
        return new State<>(initial);
    }

    /**
     * Returns the current value of this state.
     * 
     * @return current value
     */
    public T get() {
        return value;
    }

    /**
     * Sets a new value for this state and notifies all subscribers if the value changed.
     * The method has built-in protection against setting the same value to avoid unnecessary notifications.
     * 
     * @param newValue new value to set
     */
    public void set(T newValue) {
        if (Objects.equals(this.value, newValue)) {
            return; // ← proteção centralizada
        }

        this.value = newValue;

        //mesmo que um método (notifySubscribers)
//        listeners.forEach(l -> l.accept(value));
        for (var listener : List.copyOf(listeners)) {
            listener.accept(newValue);
        }
    }

    /**
     * Subscribes to state changes and immediately calls the listener with the current value.
     * Subscribers are automatically notified whenever the state value changes.
     * 
     * @param listener to be notified of state changes
     */
    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
    }
    
    /**
     * Adds an item to the list if this State contains a List<T>
     * 
     * @param item item to be added
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public void add(Object item) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.add() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>(currentList);
        newList.add(item);
        
        this.set((T) newList);
    }
    
    /**
     * Removes the last item from the list if this State contains a List<T>
     * 
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public void removeLast() {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.removeLast() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        if (currentList.isEmpty()) {
            return;
        }
        
        List<Object> newList = new ArrayList<>(currentList);
        newList.remove(newList.size() - 1);
        
        this.set((T) newList);
    }
    
    /**
     * Removes all items from the list if this State contains a List<T>
     * 
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.clear() only works with State<List<T>>");
        }
        
        this.set((T) new ArrayList<>());
    }
    
    /**
     * Removes items from the list that match the predicate if this State contains a List<T>
     * 
     * @param filter predicate to remove items
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public void removeIf(Predicate<Object> filter) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.removeIf() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>();
        
        for (Object item : currentList) {
            if (!filter.test(item)) {
                newList.add(item);
            }
        }
        
        this.set((T) newList);
    }
    
    /**
     * Removes the first occurrence of the specific item from the list if this State contains a List<T>
     * 
     * @param item item to be removed
     * @return true if the item was removed, false otherwise
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public boolean remove(Object item) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.remove() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>(currentList);
        boolean removed = newList.remove(item);
        
        if (removed) {
            this.set((T) newList);
        }
        
        return removed;
    }
    
    /**
     * Replaces an item at a specific position in the list if this State contains a List<T>
     * 
     * @param index index of the item to be replaced
     * @param newItem new item
     * @throws IndexOutOfBoundsException if the index is invalid
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public void set(int index, Object newItem) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.set(index, item) only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        if (index < 0 || index >= currentList.size()) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }
        
        List<Object> newList = new ArrayList<>(currentList);
        newList.set(index, newItem);
        
        this.set((T) newList);
    }
    
    /**
     * Replaces the first occurrence of a specific item in the list if this State contains a List<T>
     * 
     * @param oldItem item to be replaced
     * @param newItem new item
     * @return true if the item was found and replaced, false otherwise
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public boolean replace(Object oldItem, Object newItem) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.replace() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>(currentList);
        
        int index = newList.indexOf(oldItem);
        if (index != -1) {
            newList.set(index, newItem);
            this.set((T) newList);
            return true;
        }
        
        return false;
    }
    
    /**
     * Finds the index of an item in the list if this State contains a List<T>
     * 
     * @param item item to be found
     * @return index of the item, or -1 if not found
     * @throws UnsupportedOperationException if the state doesn't contain a list
     */
    @SuppressWarnings("unchecked")
    public int indexOf(Object item) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.indexOf() only works with State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        return currentList.indexOf(item);
    }
}