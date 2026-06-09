package entity;

import database.GestorePersistenza;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class GestoreNotifica {
    private GestorePersistenza gestorePersistenza;

    public GestoreNotifica() {
        gestorePersistenza = new GestorePersistenza();
    }
    public void inviaNotifica(Docente d, String corso) {

        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        Set<Studente> studenti = c.getStudenti();

        for (Studente s : studenti) {
            Notifica n = new Notifica("Nuovo materiale didattico disponibile per il corso: " + corso);
            n.setStudente(s);
            gestorePersistenza.salva(n);

        }
    }
    public List<Notifica> getNotifiche(Studente s){
            return gestorePersistenza.cercaPerCampo(Notifica.class, "studente", s);
    }
}
