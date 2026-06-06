package control;

import entity.MaterialeDidattico;

import java.util.Set;

public class FiltroDescrizione implements StatoFiltro{
    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {
        //Si è scelto di non vincolare troppo l'uguaglianza
        materiale.removeIf(e->
                Math.abs(e.getTitolo().compareToIgnoreCase(String.valueOf(campo)))<=10);

        return materiale;
    }
}
