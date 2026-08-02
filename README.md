# megalodonte-reactivity

Reactive state primitives for the Megalodonte framework: `ComputedState<T>`,
`ListState<T>`, `ForEachState<T, C>`, `Show`, and `ListenerManager`. `State<T>` and
`ReadableState<T>` themselves live in `megalodonte-base` (a dependency of this
module) — everything here builds on top of them.

All examples below are real code from `plics-sw` and the demo apps in this
`megalodonte-ecossystem` repo (`megalodonte-app1-welcome`, `megalodonte-app2-counter`,
`megalodonte-app3-string-lenght`), not made up for the README.

## Installation (Maven Local)

Publish the library locally:

```bash
./gradlew publishToMavenLocal
```

Add it to your project:

```kotlin
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("megalodonte:megalodonte-reactivity:1.0.0-beta")
    implementation("megalodonte:megalodonte-components:1.0.0-beta") // for UI integration
}
```

## `State<T>` and `.map(...)`

`State<T>` (from `megalodonte-base`) is the base mutable, subscribable value. Full
example from `megalodonte-app2-counter`:

```java
public class HomeScreen implements ScreenComponent {
    State<Integer> counter = new State<>(0);

    @Override
    public Component render() {
        ButtonProps btnProps = new ButtonProps().fontSize(30);

        return new Container(new ContainerProps().paddingAll(20)).children(
                new Text(counter.map(Object::toString), new TextProps().fontSize(90)),
                new Button("Decrement", btnProps).onClick(() -> counter.set(counter.get() - 1)),
                new SpacerVertical(10),
                new Button("Increment", btnProps).onClick(() -> counter.set(counter.get() + 1))
        );
    }
}
```

`ReadableState<T>` (the read-only supertype `State<T>` implements) ships a default
`map(Function<T, R>)` — a quick way to derive a single value without reaching for
`ComputedState` when there's only one dependency, as `counter.map(Object::toString)`
does above to feed an `int` into a `Text` component that expects a `ReadableState<String>`.

## `ComputedState<T>`

Recomputes automatically whenever any of its declared dependencies change, and only
notifies subscribers if the recomputed value actually differs from the last one.

Simplest real example — a derived string from a single dependency, from
`megalodonte-app3-string-lenght`:

```java
public class HomeScreen implements ScreenComponent {
    State<String> textState = new State<>("");
    ComputedState<String> textLenghtComputed = ComputedState.of(
            () -> "Size is: " + textState.get().length(), textState
    );

    @Override
    public Component render() {
        return new Container(new ContainerProps().paddingAll(20)).children(
                new Input(textState),
                new Text(textLenghtComputed)
        );
    }
}
```

Multiple dependencies — recomputing a currency total whenever price, quantity, *or*
discount change (trimmed from `TotaisState`, `plics-sw`):

```java
public TotaisState(State<String> preco, State<String> qtd, State<String> desconto) {
    this.totalBruto = ComputedState.of(() -> {
        BigDecimal qtdValue = qtd.get().trim().isEmpty()
                ? BigDecimal.ZERO : new BigDecimal(qtd.get());
        BigDecimal precoValue = new BigDecimal(preco.get()).movePointLeft(2);
        return Utils.toBRLCurrency(qtdValue.multiply(precoValue));
    }, qtd, preco, desconto);
}
```

## `ListState<T>`

Purpose-built reactive list — every mutation method (`add`, `remove`, `removeIf`,
`clear`, `set(index, item)`, `replace`, `updateIf`, ...) replaces the internal list
and notifies subscribers, with a built-in `Objects.equals` guard so a no-op `set`
doesn't fire spurious updates.

Real shopping-cart usage, from `PDVScreenViewModel` (`plics-sw`):

```java
final ListState<ItemVenda> itensCarrinho = ListState.ofEmpty();

// existing item → bump quantity in place, notify without swapping the list reference
existente.get().quantidade = existente.get().quantidade.add(BigDecimal.ONE);
itensCarrinho.refresh();

// new item → append
itensCarrinho.add(item);

// remove
itensCarrinho.remove(item);
```

Reacting to *future* changes only (no fire-on-subscribe) to keep a derived subtotal
in sync, from the same class's `onInit()`:

```java
itensCarrinho.onChange(itens -> {
    BigDecimal total = itens.stream()
            .map(ItemVenda::totalItem)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    subtotal.set(Utils.deRealParaCentavos(total));
});
```

Replacing one item by predicate without touching the rest, from
`VendaMercadoriaScreenViewModel` (`plics-sw`):

```java
allDataList.updateIf(it -> it.getId().equals(atualizado.getId()), it -> atualizado);
```

### `subscribe` vs `onChange` vs `refresh`

| Method      | Fires immediately on registration | Use it for |
|-------------|:---:|---|
| `subscribe` | yes | UI binding — you want the current value as soon as the component mounts |
| `onChange`  | no  | ViewModel logic — you only care about *future* changes, not the initial value |
| `refresh`   | n/a | In-place mutation — notify subscribers without replacing the list/state reference |

## `ForEachState<T, C>`

Reconciles a `ReadableState<List<T>>` into a `List<C>` of UI components, reusing
components for items that didn't change (by `equals`) instead of rebuilding
everything on every update.

Real usage rendering a card grid, from `HomeScreen` (`plics-sw`):

```java
private Column centerContent() {
    var cardsState = State.of(cardItemList);
    ForEachState<CardItem, Component> cardsForEach = ForEachState.of(cardsState, this::CardColumn);

    return new Column(new ColumnProps().spacingOf(10).fillWidth()).children(
            new FlowRow(new FlowRowProps().fillWidth().spacingOf(10))
                    .withTransition(Animations::riseIn)
                    .items(cardsForEach)
                    .children(saudacaoComponent())
    );
}
```

## `Show`

Conditional rendering, single-child or ternary, reactive or plain `boolean`. Real
usage — only show the installments field for "pay later" sales, from `PDVScreen`:

```java
Show.when(vm.tipoPagamentoIsAPrazo, () ->
        Components.InputColumn("Nº Parcelas", vm.numeroParcelas, "Ex: 3")
)
```

Non-reactive (plain `boolean`) variant, from `VendaMercadoriaScreen`:

```java
Show.when(model.getProduto().getImagem() != null,
        () -> new Image(model.getProduto().getImagem(), new ImageProps().size(100)))
```

Chain `.withTransition(...)` on either form to animate the enter/exit instead of an
instant show/hide (see `Animations` in `megalodonte-base`).

## `ListenerManager`

Global registry every `subscribe(...)` call (across `State`, `ComputedState`,
`ListState`, ...) registers itself into. Call `ListenerManager.disposeAll()` once,
on app shutdown, to drop every listener still held — every `Main.java` in this
ecosystem does it in its `CloseRequest` handler:

```java
MegalodonteApp.run(context -> context.useView(new HomeScreen()), ev -> {
    if (ev == MegalodonteApp.Event.CloseRequest) {
        ListenerManager.disposeAll();
    }
});
```

## Technologies

- Java 25
- JavaFX (only `javafx.controls`, for the `Show`/`ForEachState` component plumbing)
- JUnit 5 + Mockito (tests)
- Gradle with Kotlin DSL

## License

MIT License

## Author

Developed by **Eliezer**.
