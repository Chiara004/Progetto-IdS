package entity;

import control.SessionManager;
import database.GestorePersistenza;

import java.util.Map;
import java.util.Set;

public class GestoreIscrizione {
    public Set<Corso> visualizzaElencoCorsi(String email){
        GestoreUtente gestore=new GestoreUtente();
        return verificaIscrizioneCorso(email);
    }

    public Set<Corso> verificaIscrizioneCorso(String email){
        GestorePersistenza gestorePersistenza=new GestorePersistenza();
        Utente utente;
        utente = gestorePersistenza.cercaPrimoPerCampi(
                Studente.class,
                Map.of(
                        "emailIstituzionale", email
                )
        );
        if(utente==null)
        {
            utente = gestorePersistenza.cercaPrimoPerCampi(
                    Docente.class,
                    Map.of(
                            "emailIstituzionale", email
                    )
            );
        }

        GestoreCorso gestoreCorso=new GestoreCorso();
        if(utente instanceof Studente)
            return gestoreCorso.getElencoCorsiStudente((Studente) utente);
        else
            return gestoreCorso.getElencoCorsiDocente((Docente) utente);
    }
}
