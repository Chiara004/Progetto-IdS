package control;

import entity.MaterialeDidattico;
import entity.Visibilita;

import java.util.Set;

public class FiltroPubblicato implements StatoFiltro{
    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {
        materiale.removeIf(e -> e.getVisibilita()==Visibilita.PUBBLICATO);

        return materiale;
    }
}
