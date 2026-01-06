package megalodonte;

import java.util.Arrays;
import java.util.List;

/**
 * Exemplo de uso do ForEachState
 * 
 * Este exemplo demonstra como usar o ForEachState para criar uma lista
 * de componentes UI que reagem a mudanças em um State<List<T>>
 */
public class ForEachStateExample {
    
    // Componente simples de exemplo
    public static class TextComponent {
        private String text;
        
        public TextComponent(String text) {
            this.text = text;
        }
        
        public void setText(String text) {
            this.text = text;
        }
        
        public String getText() {
            return text;
        }
        
        @Override
        public String toString() {
            return "TextComponent{text='" + text + "'}";
        }
    }
    
    public static void main(String[] args) {
        // Estado que contém a lista de produtos
        State<List<String>> produtosState = State.of(Arrays.asList("Produto A", "Produto B"));
        
        // Cria o ForEachState que vai gerenciar os componentes
        ForEachState<String, TextComponent> forEachState = ForEachState.of(
            produtosState,
            produto -> new TextComponent(produto) // Factory function
        );
        
        // Mostra estado inicial
        System.out.println("Estado inicial:");
        System.out.println("Produtos: " + produtosState.get());
        System.out.println("Componentes: " + forEachState.getComponents());
        System.out.println();
        
        // Simula mudança: adiciona um novo produto
        System.out.println("Adicionando Produto C...");
        produtosState.set(Arrays.asList("Produto A", "Produto B", "Produto C"));
        System.out.println("Produtos: " + produtosState.get());
        System.out.println("Componentes: " + forEachState.getComponents());
        System.out.println();
        
        // Simula mudança: remove um produto
        System.out.println("Removendo Produto B...");
        produtosState.set(Arrays.asList("Produto A", "Produto C"));
        System.out.println("Produtos: " + produtosState.get());
        System.out.println("Componentes: " + forEachState.getComponents());
        System.out.println();
        
        // Simula mudança: altera um produto
        System.out.println("Alterando Produto A para Produto X...");
        produtosState.set(Arrays.asList("Produto X", "Produto C"));
        System.out.println("Produtos: " + produtosState.get());
        System.out.println("Componentes: " + forEachState.getComponents());
        System.out.println();
        
        // Simula mudança: lista vazia
        System.out.println("Limpando lista...");
        produtosState.set(Arrays.asList());
        System.out.println("Produtos: " + produtosState.get());
        System.out.println("Componentes: " + forEachState.getComponents());
        System.out.println();
    }
}