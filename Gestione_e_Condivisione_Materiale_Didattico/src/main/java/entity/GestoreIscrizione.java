package entity;

import database.GestorePersistenza;

import java.util.Map;
import java.util.Set;

public class GestoreIscrizione {
    public Set<Corso> visualizzaElencoCorsi(String email){
        return verificaIscrizioneCorso(email);
    }

    public Set<Corso> verificaIscrizioneCorso(String email){
        GestorePersistenza gestorePersistenza=new GestorePersistenza();
        Studente studente = gestorePersistenza.cercaPrimoPerCampi(
                Studente.class,
                Map.of(
                        "emailIstituzionale", email
                )
        );

        return studente.getCorsi();
    }
}
