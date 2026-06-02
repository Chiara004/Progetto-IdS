package entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@PrimaryKeyJoinColumn(name = "id_utente")
public class Studente extends Utente{

    @ManyToMany(mappedBy = "studenti")
    private Set<Corso> corsi= new HashSet<>();

    public Studente() {

        super();

    }

    @OneToMany(mappedBy = "studente", cascade = CascadeType.ALL)
    private List<Notifica> notifiche = new ArrayList<>();

    public Studente(String idUtente, String nome, String cognome,
                    String emailIstituzionale, String password, String ruolo) {
        super(idUtente, nome, cognome, emailIstituzionale, password, ruolo);
    }


    public Set<Corso> getCorsi() {
        return corsi;
    }

    public void setCorsi(Set<Corso> corsi) {
        this.corsi = corsi;
    }
}
