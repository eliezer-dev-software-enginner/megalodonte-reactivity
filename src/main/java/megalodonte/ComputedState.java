package megalodonte;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 🧠 O que é Computed State (conceito)
 *
 *<br/>Um Computed State é um estado que:
 *
 * <li>não é setado diretamente</li>
 *
 * <li>deriva de outros estados</li>
 *
 * <li>se recalcula automaticamente</li>
 *
 * <li>notifica quem depende dele</li>
 * @param <T>
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

    public static <T> ComputedState<T> of(Supplier<T> compute,
                                          ReadableState<?>... deps) {
        return new ComputedState<>(compute, deps);
    }
}