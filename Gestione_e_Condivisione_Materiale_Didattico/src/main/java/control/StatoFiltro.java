package control;

import entity.MaterialeDidattico;

import java.util.Set;

/*Implementazione dei diversi metodi di Filtraggio dei campi di ricerca usando il
design patern: "pattern state"
 */
public interface StatoFiltro {
    Set<MaterialeDidattico> filtra(Set<MaterialeDidattico> materiale,Object campo);
}
