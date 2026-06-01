package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@Entity
@PrimaryKeyJoinColumn(name = "id_utente")
public class Docente extends Utente{
    private Set<Corso> corsiDocente = new HashSet<>();

    public Docente() {
        super();
        Set<Corso> corsiDocente = new HashSet<>();
    }

    public Docente(String idUtente, String nome, String cognome,
                   String emailIstituzionale, String password, String ruolo) {
        super(idUtente, nome, cognome, emailIstituzionale, password, ruolo);
        Set<Corso> corsiDocente = new HashSet<>();
    }
}
