package entity;

import database.GestorePersistenza;

import java.util.Map;
import java.util.Set;

public class GestoreNotifica {
    public void inviaNotifica(String emailUtente, String corso){
        GestorePersistenza gestorePersistenza = new GestorePersistenza();
        Docente d = gestorePersistenza.cercaPrimoPerCampi(Docente.class, Map.of("emailIstituzionale", emailUtente));
        Corso c = gestorePersistenza.cercaPrimoPerCampi(Corso.class, Map.of("titolo", corso, "docente", d));

        Set<Studente> studenti = c.getStudenti();

        for(Studente s : studenti){
            Notifica n = new Notifica("Nuovo materiale didattico disponibile per il corso: " + corso);
            n.setStudente(s);
            gestorePersistenza.salva(n);

        }

    }
}
