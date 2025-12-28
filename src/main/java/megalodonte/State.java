package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class State<T> implements ReadableState<T> {

    private T value;
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public State(T initial) {
        this.value = initial;
    }

    public T get() {
        return value;
    }

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

    public void subscribe(Consumer<T> listener) {
        listeners.add(listener);
        listener.accept(value);
    }
}
