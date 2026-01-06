package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class ForEachState<T, C> {
    
    private final ReadableState<List<T>> state;
    private final Function<T, C> componentFactory;
    private final List<C> components = new ArrayList<>();
    private final List<T> lastItems = new ArrayList<>();
    
    private ForEachState(ReadableState<List<T>> state, Function<T, C> componentFactory) {
        this.state = state;
        this.componentFactory = componentFactory;
        
        state.subscribe(this::reconcile);
    }
    
    public static <T, C> ForEachState<T, C> of(ReadableState<List<T>> state, Function<T, C> componentFactory) {
        return new ForEachState<>(state, componentFactory);
    }
    
    public List<C> getComponents() {
        return new ArrayList<>(components);
    }
    
    /**
     * Retorna o estado para permitir que componentes se inscrevam nas mudanças
     * 
     * @return ReadableState<List<T>> o estado interno
     */
    public ReadableState<List<T>> getState() {
        return state;
    }
    
    private void reconcile(List<T> newItems) {
        if (newItems == null) {
            newItems = new ArrayList<>();
        }
        
        // Remove components que não existem mais
        for (int i = lastItems.size() - 1; i >= newItems.size(); i--) {
            components.remove(i);
            lastItems.remove(i);
        }
        
        // Atualiza ou cria componentes
        for (int i = 0; i < newItems.size(); i++) {
            T newItem = newItems.get(i);
            
            if (i < lastItems.size()) {
                // Verifica se o item mudou
                T oldItem = lastItems.get(i);
                if (!java.util.Objects.equals(oldItem, newItem)) {
                    // Item mudou, substitui o componente
                    C newComponent = componentFactory.apply(newItem);
                    components.set(i, newComponent);
                    lastItems.set(i, newItem);
                }
            } else {
                // Novo item, cria novo componente
                C newComponent = componentFactory.apply(newItem);
                components.add(newComponent);
                lastItems.add(newItem);
            }
        }
    }
}