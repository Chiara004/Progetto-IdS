package entity;

import database.GestoreFile;
import database.GestorePersistenza;
import control.SessionManager;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class GestoreCorso {
    GestorePersistenza gestorePersistenza;
    GestoreFile gestoreFile;
    Utente utenteLoggato;

    public GestoreCorso(){
        gestorePersistenza = new GestorePersistenza();
        gestoreFile = new GestoreFile();
    }

    public Set<MaterialeDidattico> recuperaMateriali(String corso){
        Corso c = recuperaCorso(corso);

        return c.getMaterialeDidattico();
    }

    public boolean rimuoviMateriale(String corso, String titolo){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
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

    public Set<Sezione> getSezioni(String corso){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        return c.getSezioni();
    }

    public String getPercorsoFile(String corso, String titolo){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        if (c == null) {
            return null;
        }

        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);

        return c.getPercorsoFileMateriale(m);
    }

    public boolean inserisciMateriale(String corso, String titolo, String descrizione, String categoria,
                                      String visibilita, File fileScelto, String sezione){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        String percorsoFile = gestoreFile.salvaFile(fileScelto);
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));

        boolean esito = c.inserisciMateriale(titolo,descrizione,visibilita, percorsoFile, sezione, categoria);
        if (esito){
            MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);
            gestorePersistenza.salva(m);
            gestorePersistenza.aggiorna(c);
        }
        else{
            gestoreFile.eliminaFileFisico(percorsoFile);
        }
        return esito;
    }

    public boolean modificaMateriale(String corso, String idMateriale, String titolo, String descrizione, String categoria,
                                     String visibilita, File fileScelto, String sezione){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));

        String percorsoFileCorrente = c.getMaterialeDidatticoPerId(Integer.parseInt(idMateriale)).getPercorsoFile();
        String percorsoFile;
        if(fileScelto != null){
            gestoreFile.eliminaFileFisico(percorsoFileCorrente);
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

    public int getIdMateriale(String corso, String titolo){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        return c.getMaterialeDidatticoPerTitolo(titolo).getIdMateriale();
    }

    public boolean apriMateriale(String corso, String titolo){
        Corso c = recuperaCorso(corso);
        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);
        // Se il materiale non esiste (o se il suo percorso è null), restituisce false
        if (m == null || m.getPercorsoFile() == null) {
            return false;
        }
        return gestoreFile.apriMateriale(m.getPercorsoFile());
    }

    public Corso recuperaCorso(String corso){
        utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Corso c = null;
        if(utenteLoggato.getRuolo().equals("Studente")){
            Studente studente = gestorePersistenza.trovaPerId(Studente.class, utenteLoggato.getIdUtente());
            if (studente != null && studente.getCorsi() != null) {
                for (Corso corsoDaCercare : studente.getCorsi()) {
                    if (corsoDaCercare.getTitolo().equals(corso)) {
                        c=corsoDaCercare;
                        break;
                    }
                }
            }
        }

        else
            c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));

        return c;
    }
}
