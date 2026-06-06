package control;

import entity.MaterialeDidattico;

import java.util.Set;

public class Filtro {
    private StatoFiltro stato;

    public Filtro()
    {
        stato=new FiltroNullo();
    }

    public void setStato(StatoFiltro stato)
    {
        this.stato=stato;
    }

    public Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale, Object campo){
        return stato.filtra(materiale,campo);
    }
}
