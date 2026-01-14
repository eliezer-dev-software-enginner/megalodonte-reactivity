package megalodonte;

import javafx.scene.Node;
import javafx.scene.layout.Pane;
import megalodonte.components.Component;

import java.util.function.Supplier;

/**
 * Demonstrates conditional component rendering using reactive state.
 * 
 * <p>Show component conditionally renders a child component based on
 * a boolean state condition. When the condition changes, it automatically
 * shows or hides the child component by adding or removing it from the
 * JavaFX scene graph.</p>
 * 
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * State<Boolean> isVisible = State.of(true);
 * Show show = new Show(isVisible, () -> new Text("Visible Content"));
 * 
 * isVisible.set(false); // Automatically hides the content
 * isVisible.set(true);  // Automatically shows the content
 * }</pre>
 * 
 * @author Eliezer
 * @since 1.0.0
 */

//class Component{
//    Node node;
//    public Component(Pane pane) {
//
//    }
//
//    public Node getNode() {
//        return node;
//    }
//}

public final class Show extends Component {

    private final ReadableState<Boolean> condition;
    private final Supplier<Component> childFactory;
    private Component mountedChild;
    
    // For conditional builder
    private Supplier<Component> trueComponent;
    private Supplier<Component> falseComponent;

    /**
     * Creates a new Show component with the specified condition and child factory.
     * 
     * @param condition reactive boolean state controlling visibility
     * @param childFactory factory that creates the child component
     */
    private Show(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
            ) {
        super(new Pane());
        this.condition = condition;
        this.childFactory = childFactory;
        
        condition.subscribe(this::update);
    }

    /**
     * Factory method to create a new Show instance.
     * 
     * @param condition reactive boolean state controlling visibility
     * @param childFactory factory that creates the child component
     * @return a new Show instance
     */
    public static Show when(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
            ) {
        return new Show(condition, childFactory);
    }
    
/**
     * Factory method for conditional rendering with true/false branches.
     * 
     * @param condition reactive boolean state controlling which branch to render
     * @param trueComponent factory for component when condition is true
     * @param falseComponent factory for component when condition is false
     * @return a new Show instance
     */
    public static Show when(
            ReadableState<Boolean> condition,
            Supplier<Component> trueComponent,
            Supplier<Component> falseComponent
            ) {
        return new Show(condition, () -> {
            boolean conditionValue = condition.get();
            return conditionValue ? trueComponent.get() : falseComponent.get();
        });
    }
    
    /**
     * Updates the component visibility based on the condition.
     * Shows child when condition becomes true, hides when false.
     * 
     * @param visible the current visibility condition
     */
    private void update(boolean visible) {
        Pane pane = (Pane) node;

        if (visible && mountedChild == null) {
            mountedChild = childFactory.get();
            pane.getChildren().add(mountedChild.getNode());
        }

        if (!visible && mountedChild != null) {
            pane.getChildren().clear();
            mountedChild = null;
        }
    }
}
