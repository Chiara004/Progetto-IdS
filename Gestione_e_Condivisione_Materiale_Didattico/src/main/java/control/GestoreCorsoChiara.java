package control;

import entity.Corso;
import entity.Docente;
import entity.MaterialeDidattico;

import java.util.Set;
import java.util.Map;

import database.GestorePersistenza;

public class GestoreCorsoChiara {
    public Set<MaterialeDidattico> recuperaMateriali(String emailUtente, String corso){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        
        return c.getMaterialeDidattico();
    }
}
