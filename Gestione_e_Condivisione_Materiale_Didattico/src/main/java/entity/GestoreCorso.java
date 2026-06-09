package entity;

import database.GestoreFile;
import database.GestorePersistenza;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestoreCorso {
    private GestorePersistenza gestorePersistenza;
    private GestoreFile gestoreFile;

    public GestoreCorso(){
        gestorePersistenza = new GestorePersistenza();
        gestoreFile = new GestoreFile();
    }

    public Set<MaterialeDidattico> recuperaMateriali(Utente utente, String corso){
        Corso c = recuperaCorso(utente, corso);

        return c.getMaterialeDidattico();
    }

    public boolean rimuoviMateriale(Utente utenteLoggato, String corso, String titolo){
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

    public Set<Sezione> getSezioni(Utente utenteLoggato, String corso){
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        return c.getSezioni();
    }

    public String getPercorsoFile(Utente utenteLoggato, String corso, String titolo){
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        if (c == null) {
            return null;
        }

        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);

        return c.getPercorsoFileMateriale(m);
    }

    public boolean inserisciMateriale(Utente utenteLoggato, String corso, String titolo, String descrizione, String categoria,
                                      String visibilita, File fileScelto, String sezione){
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

    public boolean modificaMateriale(Utente utenteLoggato, String corso, String idMateriale, String titolo, String descrizione, String categoria,
                                     String visibilita, File fileScelto, String sezione){
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

    public int getIdMateriale(Utente utenteLoggato, String corso, String titolo){
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente.idUtente", utenteLoggato.getIdUtente()));
        return c.getMaterialeDidatticoPerTitolo(titolo).getIdMateriale();
    }

    public boolean apriMateriale(Utente utenteLoggato, String corso, String titolo){
        Corso c = recuperaCorso(utenteLoggato, corso);
        MaterialeDidattico m = c.getMaterialeDidatticoPerTitolo(titolo);
        // Se il materiale non esiste (o se il suo percorso è null), restituisce false
        if (m == null || m.getPercorsoFile() == null) {
            return false;
        }
        return gestoreFile.apriMateriale(m.getPercorsoFile());
    }

    public Corso recuperaCorso(Utente utenteLoggato, String corso){

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

    public List<Corso> getCorsiPerDocente(Docente d) {
        GestorePersistenza gestorePersistenza = new GestorePersistenza();

        return gestorePersistenza.cercaPerCampo(
                Corso.class,
                "docente",
                d
        );
    }
}
