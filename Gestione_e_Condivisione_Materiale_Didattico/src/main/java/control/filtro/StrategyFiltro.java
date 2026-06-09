package control.filtro;

import entity.MaterialeDidattico;

import java.util.Set;

/*Implementazione dei diversi metodi di Filtraggio dei campi di ricerca usando il
design patern: "pattern stategy"
 */
public interface StrategyFiltro {
    void filtra(Set<MaterialeDidattico> materiale,Object campo);
}
