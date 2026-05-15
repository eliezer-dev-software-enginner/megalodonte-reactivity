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
    private final Component trueChild;
    private final Component falseChild; // null no modo single

    private Show(ReadableState<Boolean> condition,
                 Component trueChild,
                 Component falseChild) {
        super(new VBox());
        this.condition  = condition;
        this.trueChild  = trueChild;
        this.falseChild = falseChild;

        VBox box = (VBox) node;

        if (falseChild != null) {
            box.getChildren().addAll(trueChild.getNode(), falseChild.getNode());
        } else {
            box.getChildren().add(trueChild.getNode());
        }

        applyVisibility(condition.get());
        condition.subscribe(this::applyVisibility);
    }

    private void applyVisibility(boolean value) {
        setVisible(trueChild,  value);
        if (falseChild != null) setVisible(falseChild, !value);
    }

    private static void setVisible(Component c, boolean v) {
        c.getNode().setVisible(v);
        c.getNode().setManaged(v);
    }

    // Factory: modo single (esconde/mostra um único filho)
    public static Show when(ReadableState<Boolean> condition,
                            Supplier<Component> childFactory) {
        return new Show(condition, childFactory.get(), null);
    }

    // Factory: modo ternário (alterna entre dois filhos)
    public static Show when(ReadableState<Boolean> condition,
                            Supplier<Component> trueFactory,
                            Supplier<Component> falseFactory) {
        return new Show(condition, trueFactory.get(), falseFactory.get());
    }
}