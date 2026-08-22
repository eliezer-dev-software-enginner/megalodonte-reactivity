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
        // 1. "Camisa de força" vertical: Força a VBox a ter EXATAMENTE a altura do filho visível
        box.setMinHeight(Region.USE_PREF_SIZE);
        box.setMaxHeight(Region.USE_PREF_SIZE);

        // 2. Garante que se o pai for outra VBox, ele nunca vai esticar este componente verticalmente
        VBox.setVgrow(box, Priority.NEVER);

        if (falseChild != null) {
            box.getChildren().addAll(trueChild.getJavaFxNode(), falseChild.getJavaFxNode());
        } else {
            box.getChildren().add(trueChild.getJavaFxNode());
        }

        applyVisibility(condition.get());
        condition.subscribe(this::applyVisibility);
    }

    // Fluent — encadeia após o when()
    public Show withTransition(Transition transition) {
        this.transition = transition;
        return this;
    }

    /**
     * Deixa este Show esticar verticalmente se o pai (uma VBox) oferecer mais
     * espaço, em vez de travar na altura preferida do filho visível (o padrão do
     * construtor, ver acima). Necessário quando o próprio filho visível precisa
     * de altura real pra funcionar — por exemplo um {@code Scroll} lá dentro, que
     * só rola de verdade se receber uma altura delimitada pra se conter.
     */
    public Show fillHeight() {
        VBox box = (VBox) node;
        // Desfaz TAMBÉM o minHeight(USE_PREF_SIZE) do construtor, não só o max -
        // sem isso este Show nunca poderia ficar menor que a altura preferida do
        // filho visível (ex.: uma lista inteira sem cortar dentro de um Scroll),
        // o que forçaria toda a árvore acima a crescer além do espaço real da
        // janela em vez de dar ao filho uma altura delimitada pra rolar dentro.
        box.setMinHeight(Region.USE_COMPUTED_SIZE);
        box.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(box, Priority.ALWAYS);
        return this;
    }

    private void applyVisibility(boolean value) {
        applyTo(trueChild, value);
        if (falseChild != null) applyTo(falseChild, !value);
    }

    private void applyTo(Component c, boolean entering) {
        if (transition == null) {
            c.getJavaFxNode().setVisible(entering);
            c.getJavaFxNode().setManaged(entering);
            return;
        }
        if (entering) {
            c.getJavaFxNode().setVisible(true);
            c.getJavaFxNode().setManaged(true);
        }
        Animation anim = transition.play(c, entering);
        if (anim != null) {
            if (!entering) {
                anim.setOnFinished(e -> {
                    c.getJavaFxNode().setVisible(false);
                    c.getJavaFxNode().setManaged(false);
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