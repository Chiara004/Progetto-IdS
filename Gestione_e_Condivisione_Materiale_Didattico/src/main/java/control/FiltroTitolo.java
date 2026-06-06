package control;

import entity.MaterialeDidattico;

import java.util.Objects;
import java.util.Set;

import static java.lang.Math.abs;

public class FiltroTitolo implements StatoFiltro{
    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {
        //Si è scelto di non vincolare troppo l'uguaglianza
        materiale.removeIf(e->
                Math.abs(e.getTitolo().compareToIgnoreCase(String.valueOf(campo)))<=3);

        return materiale;
    }
}
