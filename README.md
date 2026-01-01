# Reactivity

Esta biblioteca contém classes que lidam com reatividade para aplicações Megalodonte. 
Eu usei como inspiração o Vue, React e Jetpack compose. Essas classes de reatividade fazem uso dos listeners JavaFX internamente.

---

## ✨ Classes disponíveis 

- State<T>
- ComputedState<T>

Tudo isso sem expor detalhes internos do JavaFX para quem consome a biblioteca.
    

---

## Uso básico com State

```java
import megalodonte.*;

State<String> nameState = new State<>();

new Text(nameState);
new Input(newState);

```

## Uso básico com ComputedState

```java
import megalodonte.*;

State<String> nameState = new State<>();

ComputedState<String> label = new ComputedState.of(v -> Your name is + nameState.get(), nameState);

new Text(label);
new Input(newState);

```

---

## 📜 Licença

MIT License

---

## 👨‍💻 Autor

Projeto desenvolvido por **Eliezer**.
