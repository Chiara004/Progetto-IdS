package control.filtro;

import entity.MaterialeDidattico;
import entity.Visibilita;

import java.util.Set;

public class FiltroPubblicato implements StatoFiltro {
    @Override
    public void filtra(Set<MaterialeDidattico> materiale, Object campo) {
        materiale.removeIf(e -> e.getVisibilita()==Visibilita.NON_PUBBLICATO);

    }
}
