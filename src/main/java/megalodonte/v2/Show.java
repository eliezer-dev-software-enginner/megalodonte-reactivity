package megalodonte.v2;

import javafx.animation.Animation;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import megalodonte.base.state.ReadableState;
import megalodonte.base.components.Component;
import megalodonte.base.state.State;

import java.util.function.Supplier;

/**
 * Conditional rendering component.
 * Shows or hides a component based on reactive state.
 */
public final class Show extends Component {
    private final ReadableState<Boolean> condition;
    private final Component trueChild;
    private final Component falseChild; // null no modo single

    private Transition transition; // null = sem animação

    private Show(ReadableState<Boolean> condition,
                 Component trueChild,
                 Component falseChild) {
        super(new VBox());
        this.condition = condition;
        this.trueChild = trueChild;
        this.falseChild = falseChild;

        VBox box = (VBox) node;
        box.setStyle("-fx-background-color:black;");
        // 1. "Camisa de força" vertical: Força a VBox a ter EXATAMENTE a altura do filho visível
        box.setMinHeight(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        // 2. Garante que se o pai for outra VBox, ele nunca vai esticar este componente verticalmente
        VBox.setVgrow(box, Priority.NEVER);

        if (falseChild != null) {
            box.getChildren().addAll(trueChild.getNode(), falseChild.getNode());
        } else {
            box.getChildren().add(trueChild.getNode());
        }

        applyVisibility(condition.get());
        condition.subscribe(this::applyVisibility);
    }

    // Fluent — encadeia após o when()
    public Show withTransition(Transition transition) {
        this.transition = transition;
        return this;
    }

    private void applyVisibility(boolean value) {
        applyTo(trueChild, value);
        if (falseChild != null) applyTo(falseChild, !value);
    }

    private void applyTo(Component c, boolean entering) {
        if (transition == null) {
            c.getNode().setVisible(entering);
            c.getNode().setManaged(entering);
            return;
        }
        if (entering) {
            c.getNode().setVisible(true);
            c.getNode().setManaged(true);
        }
        Animation anim = transition.play(c, entering);
        if (anim != null) {
            if (!entering) {
                anim.setOnFinished(e -> {
                    c.getNode().setVisible(false);
                    c.getNode().setManaged(false);
                });
            }
            anim.play();
        }
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

    // Factory: modo single, condição não-reativa (boolean puro)


    // Factory: modo single, condição não-reativa (boolean puro)
    public static Show when(boolean condition, Supplier<Component> childFactory) {
        // Se for verdadeiro, pega o filho. Se for falso, cria um nó invisível e desativado de fato.
        Component child = condition ? childFactory.get() : empty();
        return new Show(new State<>(condition), child, null);
    }

    // Factory: modo ternário, condição não-reativa (boolean puro)
    public static Show when(boolean condition,
                            Supplier<Component> trueFactory,
                            Supplier<Component> falseFactory) {
        // Evita instanciar o factory que nunca será usado!
        Component selectedChild = condition ? trueFactory.get() : falseFactory.get();
        return new Show(new State<>(condition), selectedChild, null);
    }

    // Ajuste no método empty para garantir que ele ocupe ZERO espaço de layout de forma segura
    private static Component empty() {
        VBox emptyBox = new VBox();
        emptyBox.setManaged(false);
        emptyBox.setVisible(false);
        emptyBox.setMinHeight(0);
        emptyBox.setMinWidth(0);
        emptyBox.setMaxHeight(0);
        emptyBox.setMaxWidth(0);
        return Component.CreateFromJavaFxNode(emptyBox);
    }

}