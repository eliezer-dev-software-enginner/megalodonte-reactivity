# Reactivity

A simple and lightweight Java library for **reactive state management** with focus on **clean architecture**, **testability**, and **ease of use**.

---

## ✨ Features

### 🔄 **State Management**
- **State<T>** - Mutable state with subscription support
- **ReadableState<T>** - Read-only state interface
- **ComputedState<T>** - Derived/computed states

### 📝 **List State Operations**
Complete reactive list manipulation for **State<List<T>>**:

#### 🔧 **Manipulation Methods**
- `add(item)` - Add item to list
- `removeLast()` - Remove last item
- `remove(item)` - Remove specific item
- `removeIf(predicate)` - Remove items matching predicate
- `set(index, item)` - Replace item by position
- `replace(oldItem, newItem)` - Replace first occurrence
- `indexOf(item)` - Find item index
- `clear()` - Remove all items

#### 🔄 **Dynamic List Rendering**
- **ForEachState** - Reactive component list rendering
- Declarative API integration with Column/Row components
- Automatic reconciliation when state changes

---

## 📦 Installation (Maven Local)

After publishing locally:

```bash
./gradlew publishToMavenLocal
```

Add to your project:

```gradle
repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("megalodonte:reactivity:1.0.0")
    implementation("megalodonte:components:1.0.0") // For UI integration
}
```

---

## 🚀 Basic Usage

### State Management

```java
import megalodonte.State;

// Create state
State<String> nameState = State.of("John");

// Subscribe to changes
nameState.subscribe(name -> {
    System.out.println("Name changed to: " + name);
});

// Update state (triggers subscribers)
nameState.set("Jane");
```

### List State Operations

```java
import megalodonte.State;
import java.util.Arrays;

// Create list state
State<List<String>> itemsState = State.of(Arrays.asList("Apple", "Banana"));

// Add items
itemsState.add("Orange");

// Remove items
itemsState.removeLast();
itemsState.remove("Apple");

// Conditional remove
itemsState.removeIf(item -> ((String)item).startsWith("B"));

// Edit by index
itemsState.set(0, "Grape");

// Edit by reference
itemsState.replace("Banana", "Mango");

// Find item index
int index = itemsState.indexOf("Mango");

// Clear all
itemsState.clear();
```

---

## 🎨 ForEachState Integration

### Declarative API

```java
import megalodonte.*;
import megalodonte.components.*;
import java.util.Arrays;

// Create reactive list
State<List<Product>> productsState = State.of(Arrays.asList(
    new Product("Coffee", 15.00),
    new Product("Bread", 8.00)
));

// Create ForEachState
ForEachState<Product, Button> forEachState = ForEachState.of(
    productsState,
    product -> new Button(product.name + " - $" + product.price)
);

// Declarative UI integration
return new Column()
    .c_child(new Text("Product List"))
    .items(forEachState)                     // Automatic reactive rendering!
    .c_child(new Button("Add Product", () -> {
        productsState.add(new Product("New Item", 99.00));
    }))
    .c_child(new Button("Remove Last", () -> {
        productsState.removeLast();
    }));
```

### How ForEachState Works

1. **Initial Rendering** - Creates components from initial state
2. **State Changes** - Automatically reconciles when state updates
3. **No Diff** - Simple replacement strategy (no virtualization)
4. **No Layout** - Pure component management
5. **No Pagination** - Renders all items

---

## 🧪 Architecture

### 🏗️ **Dependency Inversion Principle (DIP)**

```
State (Public API)
    ↓
ForEachState (Reactive Renderer)
    ↓
ComponentFactory (User-defined)
```

This enables:
- Unit testing without JavaFX
- Mockito integration
- Future implementation flexibility

---

## 🧪 Testing

Tests are **100% unitary**, using **JUnit 5 + Mockito**, without dependency on:
- JavaFX Thread
- Operating System
- Graphical Environment

Example test:

```java
@Test
void add_shouldAddItemToList() {
    // Given
    State<List<String>> state = State.of(Arrays.asList("item1"));
    
    // When
    state.add("item2");
    
    // Then
    List<String> result = state.get();
    assertEquals(2, result.size());
    assertTrue(result.contains("item2"));
}
```

---

## 🔧 Technologies

- **Java 17** (LTS)
- **JavaFX 17** (for UI components)
- **JUnit 5**
- **Mockito**
- **Gradle**

---

## ⚠️ Important Notes

- **No Virtualization** - Renders all items, suitable for small/medium lists
- **No Diff Algorithm** - Simple reconciliation for performance
- **No Layout Management** - Pure component state management
- **Thread Safety** - Subscribe on same thread as UI updates

---

## 📁 Project Structure

```
src/
 ├─ main/java/megalodonte/
 │   ├─ State.java                    # Mutable state
 │   ├─ ReadableState.java           # Read-only interface
 │   ├─ ComputedState.java           # Derived state
 │   └─ ForEachState.java            # Reactive renderer
 │
 └─ test/java/megalodonte/
    ├─ StateTest.java               # State tests
    ├─ StateListMethodsTest.java     # List operations tests
    ├─ StateListExtendedMethodsTest.java # Edit operations tests
    └─ ForEachStateTest.java        # Renderer tests
```

---

## 🎯 Use Cases

### 🛒 **ERP Applications**
- Reactive product lists
- Dynamic form fields
- Real-time inventory management

### 📱 **Desktop Applications**
- Settings screens
- Data tables
- Dynamic menus

### 🎮 **JavaFX Applications**
- Reactive UI components
- State synchronization
- Component lifecycle management

---

## 📜 License

MIT License

---

## 👨‍💻 Author

Developed by **Eliezer**.