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

    public static <T> State<T> of(T initial) {
        return new State<>(initial);
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
    
    /**
     * Adiciona um item à lista se este State contiver uma List<T>
     * 
     * @param item item a ser adicionado
     * @throws UnsupportedOperationException se o estado não contiver uma lista
     */
    @SuppressWarnings("unchecked")
    public void add(Object item) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.add() só funciona com State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>(currentList);
        newList.add(item);
        
        this.set((T) newList);
    }
    
    /**
     * Remove o último item da lista se este State contiver uma List<T>
     * 
     * @throws UnsupportedOperationException se o estado não contiver uma lista
     */
    @SuppressWarnings("unchecked")
    public void removeLast() {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.removeLast() só funciona com State<List<T>>");
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
     * Remove todos os itens da lista se este State contiver uma List<T>
     * 
     * @throws UnsupportedOperationException se o estado não contiver uma lista
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.clear() só funciona com State<List<T>>");
        }
        
        this.set((T) new ArrayList<>());
    }
    
    /**
     * Remove itens da lista que correspondem ao predicado se este State contiver uma List<T>
     * 
     * @param filter predicado para remover itens
     * @throws UnsupportedOperationException se o estado não contiver uma lista
     */
    @SuppressWarnings("unchecked")
    public void removeIf(java.util.function.Predicate<Object> filter) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.removeIf() só funciona com State<List<T>>");
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
     * Remove a primeira ocorrência do item específico da lista se este State contiver uma List<T>
     * 
     * @param item item a ser removido
     * @return true se o item foi removido, false caso contrário
     * @throws UnsupportedOperationException se o estado não contiver uma lista
     */
    @SuppressWarnings("unchecked")
    public boolean remove(Object item) {
        if (!(value instanceof List)) {
            throw new UnsupportedOperationException("State.remove() só funciona com State<List<T>>");
        }
        
        List<Object> currentList = (List<Object>) value;
        List<Object> newList = new ArrayList<>(currentList);
        boolean removed = newList.remove(item);
        
        if (removed) {
            this.set((T) newList);
        }
        
        return removed;
    }
}
