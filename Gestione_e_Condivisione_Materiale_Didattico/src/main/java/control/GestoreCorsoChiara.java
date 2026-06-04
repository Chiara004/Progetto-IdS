package control;

import database.GestoreFile;
import entity.Corso;
import entity.Docente;
import entity.MaterialeDidattico;

import java.util.Set;
import java.util.Map;

import database.GestorePersistenza;
import entity.Sezione;

public class GestoreCorsoChiara {
    public Set<MaterialeDidattico> recuperaMateriali(String emailUtente, String corso){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        
        return c.getMaterialeDidattico();
    }

    public boolean rimuoviMateriale(String emailUtente, String corso, String titolo){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        GestoreFile gestoreFile = new GestoreFile();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        MaterialeDidattico m = gestorePersistenza.cercaPrimoPerCampi(MaterialeDidattico.class, Map.of("titolo", titolo, "corso", c));

        if(m != null){
            // 1. Elimina il file fisico
            gestoreFile.eliminaFileFisico(m.getPercorsoFile());

            // 2. Scollega dalla sezione(se presente)
            if (m.getSezione() != null) {
                m.setSezione(null);                  // Rimuove il riferimento alla sezione dentro il materiale
                gestorePersistenza.aggiorna(m);      // Aggiorna la sezione nel database
            }

            // 3. Scollega dal corso
            c.rimuoviMateriale(m); // Rimuove dalla lista del corso

            gestorePersistenza.aggiorna(c);

            return true;
        }
        return false;
    }
}
