package megalodonte;

import megalodonte.base.state.ReadableState;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ForEachState<T, C> implements megalodonte.base.state.ForEachState<T, C> {

    private final ReadableState<List<T>> state;
    private final Function<T, C> componentFactory;
    private final List<C> components = new ArrayList<>();
    private final List<T> lastItems = new ArrayList<>();

    private final Object lock = new Object();

    private ForEachState(ReadableState<List<T>> state, Function<T, C> componentFactory) {
        this.state = state;
        this.componentFactory = componentFactory;

        state.subscribe(this::reconcile);
    }

    public static <T, C> ForEachState<T, C> of(ReadableState<List<T>> state, Function<T, C> componentFactory) {
        return new ForEachState<>(state, componentFactory);
    }

    @Override
    public List<C> getComponents() {
        synchronized (lock) {
            return new ArrayList<>(components);
        }
    }

    @Override
    public ReadableState<List<T>> getState() {
        return state;
    }

    private void reconcile(List<T> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }

        synchronized (lock) {
            for (int i = lastItems.size() - 1; i >= newItems.size(); i--) {
                components.remove(i);
                lastItems.remove(i);
            }

            for (int i = 0; i < newItems.size(); i++) {
                T newItem = newItems.get(i);

                if (i < lastItems.size()) {
                    T oldItem = lastItems.get(i);
                    if (!java.util.Objects.equals(oldItem, newItem)) {
                        C newComponent = componentFactory.apply(newItem);
                        components.set(i, newComponent);
                        lastItems.set(i, newItem);
                    }
                } else {
                    C newComponent = componentFactory.apply(newItem);
                    components.add(newComponent);
                    lastItems.add(newItem);
                }
            }
        }
    }
}
