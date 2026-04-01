package megalodonte.v2;

import javafx.scene.layout.VBox;
import megalodonte.ReadableState;
import megalodonte.base.components.Component;

import java.util.function.Supplier;

/**
 * Conditional rendering component.
 * Shows or hides a component based on reactive state.
 */
public final class Show extends Component {

    private final ReadableState<Boolean> condition;
    private final Supplier<Component> childFactory;
    private Component mountedChild;

    private Show(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
    ) {
        super(new VBox());

        this.condition = condition;
        this.childFactory = childFactory;

        // Cria o componente uma única vez
        mountedChild = childFactory.get();

        VBox box = (VBox) node;
        box.getChildren().add(mountedChild.getNode());

        // Estado inicial
        boolean initial = condition.get();
        mountedChild.getNode().setVisible(initial);
        mountedChild.getNode().setManaged(initial);

        // Reatividade
        condition.subscribe(this::update);
    }

    public static Show when(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
    ) {
        return new Show(condition, childFactory);
    }

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

    private void update(boolean visible) {
        if (mountedChild != null) {
            mountedChild.getNode().setVisible(visible);
            mountedChild.getNode().setManaged(visible);
        }
    }
}