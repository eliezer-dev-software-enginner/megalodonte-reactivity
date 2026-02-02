package megalodonte;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Computed state that automatically recalculates its value when dependencies change.
 * ComputedState is useful for derived values that depend on other states.
 * 
 * <p>ComputedState automatically subscribes to its dependencies and
 * recalculates when any of them change. The computation is lazy
 * and cached until dependencies change.</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * State<String> firstName = State.of("John");
 * State<String> lastName = State.of("Doe");
 * 
 * // Computed full name that updates when either first or last name changes
 * ComputedState<String> fullName = ComputedState.of(
 *     () -> firstName.get() + " " + lastName.get(),
 *     firstName, lastName // Dependencies
 * );
 * 
 * fullName.subscribe(name -> System.out.println("Full name: " + name));
 * 
 * firstName.set("Jane"); // Automatically triggers fullName update
 * }</pre>
 * 
 * @param <T> type of computed value
 * @author Eliezer
 * @since 1.0.0
 */
public class ComputedState<T> implements ReadableState<T> {

    private T value;

    private ComputedState(Supplier<T> compute,
                           ReadableState<?>... deps) {

        Runnable recompute = () -> {
            T newValue = compute.get();
            if (value == null || !value.equals(newValue)) {
                value = newValue;
                listeners.forEach(l -> l.accept(value));
            }
        };

        for (ReadableState<?> dep : deps) {
            dep.subscribe(e -> recompute.run());
        }

        recompute.run();
    }

    private final List<Consumer<T>> listeners = new java.util.ArrayList<>();

    @Override
    public T get() {
        return value;
    }

    @Override
    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
    }

    @Override
    public boolean isNull() {
        return get() == null;
    }

    /**
     * Creates a new computed state with the specified computation and dependencies.
     * 
     * @param <T> type of computed value
     * @param compute function that computes the value
     * @param deps states this computed state depends on
     * @return a new ComputedState instance
     */
    public static <T> ComputedState<T> of(Supplier<T> compute,
                                           ReadableState<?>... deps) {
        return new ComputedState<>(compute, deps);
    }
}