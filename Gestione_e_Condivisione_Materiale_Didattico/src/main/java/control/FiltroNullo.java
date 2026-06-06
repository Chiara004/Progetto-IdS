package control;

import entity.MaterialeDidattico;

import java.util.Set;

//Classe di Default nel caso non debba essere effettuato il filtraggio
public class FiltroNullo implements StatoFiltro{
    @Override
    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo) {

        return materiale;
    }
}
