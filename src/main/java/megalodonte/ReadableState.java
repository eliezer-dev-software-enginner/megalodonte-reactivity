package megalodonte;

/**
 * Read-only interface for reactive state that allows observation without modification.
 * 
 * <p>This interface provides read access to state values while preventing
 * direct modifications. Subscribe to changes through the {@link #subscribe}
 * method.</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Read-only access to a state
 * ReadableState<String> readOnlyName = someState;
 * String value = readOnlyName.get();
 * 
 * // Subscribe to changes
 * readOnlyName.subscribe(newValue -> System.out.println("Changed to: " + newValue));
 * }</pre>
 * 
 * @param <T> type of the value held by this state
 * @author Eliezer
 * @since 1.0.0
 */
public interface ReadableState<T> {
    T get();
    void subscribe(java.util.function.Consumer<T> listener);

    default <R> ReadableState<R> map(java.util.function.Function<T, R> mapper) {
        State<R> derived = new State<>(mapper.apply(get()));

        subscribe(value -> derived.set(mapper.apply(value)));

        return derived;
    }
}