package control;

import entity.Categoria;
import entity.MaterialeDidattico;

import java.util.Set;

public class FiltroCategoria implements StatoFiltro{

    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {
        materiale.removeIf(e-> e.getCategoria()== Categoria.valueOf(String.valueOf(campo)));
        return materiale;
    }
}
