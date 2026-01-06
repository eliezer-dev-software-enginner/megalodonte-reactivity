package megalodonte;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Reactive component renderer that automatically updates a list of components
 * when a state list changes. Provides declarative rendering similar to Jetpack Compose.
 * 
 * <p>ForEachState manages component reconciliation without diff, virtualization,
 * or pagination. It follows a simple strategy: when the state changes, it creates
 * new components for new items, removes components for deleted items, and replaces
 * components for modified items.</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Create state with list of products
 * State<List<Product>> productsState = State.of(Arrays.asList(
 *     new Product("Coffee", 15.0),
 *     new Product("Bread", 8.0)
 * ));
 * 
 * // Create ForEachState that renders each product as a Button
 * ForEachState<Product, Button> forEachState = ForEachState.of(
 *     productsState,
 *     product -> new Button(product.name + " - $" + product.price)
 * );
 * 
 * // Use in UI components
 * Column.of()
 *     .c_child(new Text("Products"))
 *     .items(forEachState) // Declarative reactive rendering
 *     .c_child(new Button("Add Product", () -> productsState.add(new Item())));
 * }</pre>
 * 
 * @param <T> the type of items in the state list
 * @param <C> the type of components to render
 * @author Eliezer
 * @since 1.0.0
 */
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