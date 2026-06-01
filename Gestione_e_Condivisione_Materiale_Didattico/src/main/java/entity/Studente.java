package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;

@Entity
@PrimaryKeyJoinColumn(name = "id_utente")
public class Studente extends Utente{


    public Studente() {

        super();
    }

    public Studente(String idUtente, String nome, String cognome,
                    String emailIstituzionale, String password, String ruolo) {
        super(idUtente, nome, cognome, emailIstituzionale, password, ruolo);
    }

}
