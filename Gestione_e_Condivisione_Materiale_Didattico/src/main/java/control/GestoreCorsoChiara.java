package control;

import database.GestoreFile;
import entity.Corso;
import entity.Docente;
import entity.MaterialeDidattico;

import java.io.File;
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

        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);

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

    public Set<Sezione> getSezioni(String emailUtente, String corso){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();

        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        return c.getSezioni();
    }

    public String getPercorsoFile(String emailUtente, String corso, String titolo){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        MaterialeDidattico m = gestorePersistenza.cercaPrimoPerCampi(MaterialeDidattico.class, Map.of("titolo", titolo, "corso", c));

        return c.getPercorsoFileMateriale(m);
    }

    public boolean inserisciMateriale(String emailUtente, String corso, String titolo, String descrizione, String categoria,
                                      String visibilita, File fileScelto, String sezione){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        GestoreFile gestoreFile = new GestoreFile();
        String percorsoFile = gestoreFile.salvaFile(fileScelto);
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        boolean esito = c.inserisciMateriale(titolo,descrizione,visibilita, percorsoFile, sezione, categoria);
        if (esito){
            MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);
            gestorePersistenza.salva(m);
            gestorePersistenza.aggiorna(c);
        }
        return esito;
    }

    public boolean modificaMateriale(String emailUtente, String corso, String idMateriale, String titolo, String descrizione, String categoria,
                                      String visibilita, File fileScelto, String sezione){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        GestoreFile gestoreFile = new GestoreFile();

        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        String percorsoFileCorrente = c.getMaterialeDidatticoPerId(Integer.parseInt(idMateriale)).getPercorsoFile();
        String percorsoFile;
        if(fileScelto != null){
            percorsoFile = gestoreFile.salvaFile(fileScelto);
        }
        else{
            percorsoFile = percorsoFileCorrente;
        }

        boolean esito = c.modificaMateriale(idMateriale,titolo,descrizione,visibilita, percorsoFile, sezione, categoria);
        if (esito){
            MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);
            gestorePersistenza.aggiorna(m);
            gestorePersistenza.aggiorna(c);
        }
        return esito;
    }

    public int getIdMateriale(String emailUtente, String corso, String titolo){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        return c.getMaterialeDidatticoPerTitolo(titolo).getIdMateriale();
    }

    public boolean apriMateriale(String emailUtente, String corso, String titolo){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        GestoreFile gestoreFile = new GestoreFile();

        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));
        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);

        return gestoreFile.apriMateriale(m.getPercorsoFile());


    }
}
