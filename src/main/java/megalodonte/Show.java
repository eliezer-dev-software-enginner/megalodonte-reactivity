package megalodonte;

import javafx.scene.layout.Pane;

import java.util.function.Supplier;

public final class Show extends Component {

    private final ReadableState<Boolean> condition;
    private final Supplier<Component> childFactory;
    private Component mountedChild;

    private Show(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
    ) {
        super(new Pane());
        this.condition = condition;
        this.childFactory = childFactory;

        condition.subscribe(this::update);
    }

    public static Show when(
            ReadableState<Boolean> condition,
            Supplier<Component> childFactory
    ) {
        return new Show(condition, childFactory);
    }

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
