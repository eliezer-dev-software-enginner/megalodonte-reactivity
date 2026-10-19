# megalodonte-reactivity

This module includes JavaFX-based `Show` and depends on `megalodonte-base`.
`ComputedState` and `ForEachState` implement `AutoCloseable`; call `close()`
when they are no longer needed. `subscribe(...)` returns `void`; use
`unsubscribe(listener)` or `ListenerManager.disposeAll()` to remove listeners.

Reactive state primitives for the Megalodonte framework: `ComputedState<T>`,
`ListState<T>`, `ForEachState<T, C>`, `Show`, and `ListenerManager`. `State<T>` and
`ReadableState<T>` are imported from `megalodonte.base.state` and supplied by
`megalodonte-base`, a dependency of this module.

The UI examples below refer to separate applications and are illustrative;
their source files are not included in this repository. A self-contained
`ForEachState` example is available at
`src/main/java/megalodonte/ForEachStateExample.java`.

## Installation (Maven Local)

This build requires a Java 25 toolchain. It resolves `megalodonte-base:1.0.0-beta`
from Maven Central or Maven Local; ensure that dependency is available before
building. The included Gradle wrapper uses Gradle 9.2.0.

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
}
```

UI examples also require the appropriate UI libraries, which are not provided
by this module.

## `State<T>` and `.map(...)`

`State<T>` (from `megalodonte-core`) is the base mutable, subscribable value. Full
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

Multiple declared dependencies for a currency total (trimmed from `TotaisState`,
`plics-sw`). This excerpt calculates from price and quantity; although it lists
discount as a dependency, it does not apply the discount:

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
and notifies subscribers. `set(List<E>)` skips notification only when passed
the same list instance; a new list instance triggers notification even if its
contents compare equal.

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

Reconciles a `ReadableState<List<T>>` into a `List<C>` of components, reusing
components at the same index when their items compare equal instead of rebuilding
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

Global listener registry used by `ComputedState` and `ListState` in this module.
Call `ListenerManager.disposeAll()` at application shutdown to remove registered
listeners. The following is an integration example from a separate application:

```java
MegalodonteApp.run(context -> context.useView(new HomeScreen()), ev -> {
    if (ev == MegalodonteApp.Event.CloseRequest) {
        ListenerManager.disposeAll();
    }
});
```

## Technologies

- Java 25
- JavaFX 25.0.1 (`javafx.controls` configured by the Gradle plugin; the module
  declares `javafx.graphics`)
- JUnit 5 + Mockito (tests)
- Gradle with Kotlin DSL

## License

No license file is present in this repository. The licensing terms require
confirmation by the project maintainer.

## Author

Developed by **Eliezer**.
