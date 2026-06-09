package control.filtro;

import entity.MaterialeDidattico;
import java.util.Set;

public class FiltroDescrizione implements StrategyFiltro {
    @Override
    public void filtra(Set<MaterialeDidattico> materiale, Object campo) {
        if (campo == null || String.valueOf(campo).trim().isEmpty() || String.valueOf(campo).equals("Inserisci parola chiave ...")) {
            return;
        }

        String query = String.valueOf(campo).toLowerCase().trim();
        // Corretto: ora controlla la descrizione e non il titolo
        materiale.removeIf(e -> e.getDescrizione() == null || !e.getDescrizione().toLowerCase().contains(query));
    }
}