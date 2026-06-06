package control.filtro;

import entity.MaterialeDidattico;
import java.util.Set;

public class FiltroTitolo implements StatoFiltro {
    @Override
    public void filtra(Set<MaterialeDidattico> materiale, Object campo) {
        if (campo == null || String.valueOf(campo).trim().isEmpty() || String.valueOf(campo).equals("Inserisci parola chiave ...")) {
            return;
        }

        String query = String.valueOf(campo).toLowerCase().trim();
        // Rimuove se il titolo NON contiene la parola cercata
        materiale.removeIf(e -> e.getTitolo() == null || !e.getTitolo().toLowerCase().contains(query));

    }
}