package control.filtro;

import entity.MaterialeDidattico;

import java.util.Set;

public class Filtro {
    private StrategyFiltro stato;

    public Filtro()
    {
        stato=new FiltroNullo();
    }

    public void setStato(StrategyFiltro stato)
    {
        this.stato=stato;
    }

    public void filtra(Set<MaterialeDidattico> materiale, Object campo){
        stato.filtra(materiale,campo);
    }
}
