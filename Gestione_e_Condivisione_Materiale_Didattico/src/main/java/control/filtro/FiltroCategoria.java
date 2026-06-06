package control.filtro;

import entity.MaterialeDidattico;
import java.util.Set;

public class FiltroCategoria implements StatoFiltro {
    @Override
    public void filtra(Set<MaterialeDidattico> materiale, Object campo) {
        if (campo == null || String.valueOf(campo).trim().isEmpty() || String.valueOf(campo).equals("Inserisci parola chiave ...")) {
            return;
        }

        String query = String.valueOf(campo).toLowerCase().trim();
        // Confronto sicuro convertendo l'enum in stringa minuscola
        materiale.removeIf(e -> e.getCategoria() == null || !e.getCategoria().toString().toLowerCase().contains(query));

    }
}