package control;

import entity.MaterialeDidattico;
import java.util.Set;

public class FiltroDescrizione implements StatoFiltro {
    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {
        if (campo == null || String.valueOf(campo).trim().isEmpty() || String.valueOf(campo).equals("Inserisci parola chiave ...")) {
            return materiale;
        }

        String query = String.valueOf(campo).toLowerCase().trim();
        // Corretto: ora controlla la descrizione e non il titolo
        materiale.removeIf(e -> e.getDescrizione() == null || !e.getDescrizione().toLowerCase().contains(query));

        return materiale;
    }
}