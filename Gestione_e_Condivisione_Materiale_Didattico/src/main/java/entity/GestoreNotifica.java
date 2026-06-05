package entity;

import control.SessionManager;
import database.GestorePersistenza;

import java.util.Map;
import java.util.Set;

public class GestoreNotifica {
    public void inviaNotifica(String corso){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();

        Docente d = (Docente) SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        Set<Studente> studenti = c.getStudenti();

        for(Studente s : studenti){
            Notifica n = new Notifica("Nuovo materiale didattico disponibile per il corso: " + corso);
            n.setStudente(s);
            gestorePersistenza.salva(n);

        }

    }
}
