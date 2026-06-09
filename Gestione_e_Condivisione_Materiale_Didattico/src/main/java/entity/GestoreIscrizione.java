package entity;

import database.GestorePersistenza;

import java.util.Map;
import java.util.Set;

public class GestoreIscrizione {
    private GestorePersistenza gestorePersistenza;
    public Set<Corso> visualizzaElencoCorsi(String email){
        return verificaIscrizioneCorso(email);
    }

    public Set<Corso> verificaIscrizioneCorso(String email){
        gestorePersistenza=new GestorePersistenza();
        Studente studente = gestorePersistenza.cercaPrimoPerCampi(
                Studente.class,
                Map.of(
                        "emailIstituzionale", email
                )
        );

        return studente.getCorsi();
    }
}
