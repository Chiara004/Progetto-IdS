package control.filtro;

import entity.MaterialeDidattico;

import java.util.Set;

//Classe di Default nel caso non debba essere effettuato il filtraggio
public class FiltroNullo implements StrategyFiltro {
    @Override
    public void filtra(Set<MaterialeDidattico> materiale, Object campo) {

    }
}
