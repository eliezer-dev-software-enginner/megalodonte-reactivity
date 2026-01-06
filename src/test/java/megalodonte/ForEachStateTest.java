package megalodonte;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForEachStateTest {

    @Mock
    private Function<String, TestComponent> componentFactory;
    
    @Mock
    private TestComponent component1;
    
    @Mock
    private TestComponent component2;
    
    @Mock
    private TestComponent component3;
    
    private State<List<String>> listState;
    private ForEachState<String, TestComponent> forEachState;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listState = State.of(new ArrayList<>());
        
        when(componentFactory.apply(any())).thenReturn(component1, component2, component3);
    }
    
    @Test
    void of_shouldCreateForEachStateAndSubscribeToState() {
        forEachState = ForEachState.of(listState, componentFactory);
        
        assertNotNull(forEachState);
        assertEquals(0, forEachState.getComponents().size());
    }
    
    @Test
    void reconcile_whenListGrows_shouldCreateNewComponents() {
        forEachState = ForEachState.of(listState, componentFactory);
        
        // Adiciona primeiro item
        listState.set(Arrays.asList("item1"));
        
        List<TestComponent> components = forEachState.getComponents();
        assertEquals(1, components.size());
        assertSame(component1, components.get(0));
        verify(componentFactory).apply("item1");
        
        // Adiciona segundo item
        when(componentFactory.apply("item2")).thenReturn(component2);
        listState.set(Arrays.asList("item1", "item2"));
        
        components = forEachState.getComponents();
        assertEquals(2, components.size());
        assertSame(component1, components.get(0));
        assertSame(component2, components.get(1));
        verify(componentFactory).apply("item2");
    }
    
    @Test
    void reconcile_whenListShrinks_shouldRemoveComponents() {
        // Começa com 3 itens
        listState.set(Arrays.asList("item1", "item2", "item3"));
        forEachState = ForEachState.of(listState, componentFactory);
        
        assertEquals(3, forEachState.getComponents().size());
        
        // Remove para 2 itens
        listState.set(Arrays.asList("item1", "item2"));
        
        List<TestComponent> components = forEachState.getComponents();
        assertEquals(2, components.size());
        assertSame(component1, components.get(0));
        assertSame(component2, components.get(1));
    }
    
    @Test
    void reconcile_whenItemChanges_shouldReplaceComponent() {
        // Começa com itens
        listState.set(Arrays.asList("item1", "item2"));
        forEachState = ForEachState.of(listState, componentFactory);
        
        assertEquals(2, forEachState.getComponents().size());
        
        // Muda o segundo item
        when(componentFactory.apply("item2-changed")).thenReturn(component3);
        listState.set(Arrays.asList("item1", "item2-changed"));
        
        List<TestComponent> components = forEachState.getComponents();
        assertEquals(2, components.size());
        assertSame(component1, components.get(0)); // Primeiro não mudou
        assertSame(component3, components.get(1)); // Segundo foi substituído
        verify(componentFactory).apply("item2-changed");
    }
    
    @Test
    void reconcile_whenSameItems_shouldNotReplaceComponents() {
        listState.set(Arrays.asList("item1", "item2"));
        forEachState = ForEachState.of(listState, componentFactory);
        
        // Reset mocks para contar chamadas após setup inicial
        reset(componentFactory);
        when(componentFactory.apply(any())).thenReturn(component1, component2);
        
        // Atualiza com mesma lista
        listState.set(Arrays.asList("item1", "item2"));
        
        // Não deve criar novos componentes
        verify(componentFactory, never()).apply(any());
        assertEquals(2, forEachState.getComponents().size());
    }
    
    @Test
    void reconcile_whenNullList_shouldCreateEmptyComponents() {
        forEachState = ForEachState.of(listState, componentFactory);
        
        listState.set(null);
        
        assertEquals(0, forEachState.getComponents().size());
    }
    
    @Test
    void reconcile_whenEmptyList_shouldHaveNoComponents() {
        listState.set(Arrays.asList("item1", "item2"));
        forEachState = ForEachState.of(listState, componentFactory);
        
        assertEquals(2, forEachState.getComponents().size());
        
        listState.set(new ArrayList<>());
        
        assertEquals(0, forEachState.getComponents().size());
    }
    
    @Test
    void getComponents_shouldReturnCopy() {
        listState.set(Arrays.asList("item1"));
        forEachState = ForEachState.of(listState, componentFactory);
        
        List<TestComponent> components1 = forEachState.getComponents();
        List<TestComponent> components2 = forEachState.getComponents();
        
        assertNotSame(components1, components2); // São cópias diferentes
        assertEquals(components1, components2); // Mas têm mesmo conteúdo
        
        // Modificar uma cópia não deve afetar a outra
        components1.clear();
        assertEquals(1, forEachState.getComponents().size());
    }
    
    // Interface de teste para simular componentes
    interface TestComponent {
        void setValue(String value);
        String getValue();
    }
}