package megalodonte.v2;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ListStateUpdateIfBugTest {

    static class Item {
        String nome;
        Item(String nome) { this.nome = nome; }
    }

    @Test
    void updateIfDeveNotificarQuandoUpdaterMutaEDevolveAMesmaReferencia() {
        var item = new Item("original");
        var lista = ListState.of(new ArrayList<>(List.of(item)));

        var notificado = new boolean[]{false};
        lista.onChange(l -> notificado[0] = true);

        // Padrão real: mutar o objeto já presente na lista e devolver a MESMA referência
        // (é exatamente o que ClienteViewModel.handleAddOrUpdate() faz ao editar).
        item.nome = "editado";
        lista.updateIf(it -> it == item, it -> item);

        assertTrue(notificado[0],
                "listener deveria ter sido notificado — o objeto mudou de verdade, mesmo com a mesma referência");
    }
}
