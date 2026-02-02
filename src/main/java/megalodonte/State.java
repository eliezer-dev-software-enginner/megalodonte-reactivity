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
 * Whenever value changes, all registered subscribers are automatically notified.</p>
 * 
 * <p>For list operations, create {@link ListState} or use specialized methods.</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Single value state
 * State<String> name = State.of("John");
 * name.subscribe(newName -> System.out.println("Name: " + newName));
 * name.set("Jane"); // Triggers subscription
 * 
 * // List state
 * ListState<String> items = ListState.of(Arrays.asList("A", "B"));
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

    @Override
    public boolean isNull() {
        return get() == null;
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
            listener.accept(value);
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
}