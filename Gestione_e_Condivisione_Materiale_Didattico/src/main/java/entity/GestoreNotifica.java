package entity;

import control.SessionManager;
import database.GestorePersistenza;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestoreNotifica {
    public void inviaNotifica(String corso) {
        GestorePersistenza gestorePersistenza = new GestorePersistenza();

        Docente d = (Docente) SessionManager.getInstance().getUtenteLoggato();
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        Set<Studente> studenti = c.getStudenti();

        for (Studente s : studenti) {
            Notifica n = new Notifica("Nuovo materiale didattico disponibile per il corso: " + corso);
            n.setStudente(s);
            gestorePersistenza.salva(n);

        }
    }
    public static List<Notifica> getNotifiche(){

            Studente s = (Studente) SessionManager.getInstance().getUtenteLoggato();
            GestorePersistenza gp = new GestorePersistenza();
            return gp.cercaPerCampo(Notifica.class, "studente", s);
    }
}
