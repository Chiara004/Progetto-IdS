package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;

import java.util.HashSet;
import java.util.Set;

@Entity
@PrimaryKeyJoinColumn(name = "id_utente")
public class Docente extends Utente{

    @OneToMany(mappedBy = "docente")
    private Set<Corso> insegnamenti = new HashSet<>();

    public Docente() {
        super();
    }

    public Docente(String idUtente, String nome, String cognome,
                   String emailIstituzionale, String password, String ruolo) {
        super(idUtente, nome, cognome, emailIstituzionale, password, ruolo);
    }


    public Set<Corso> getInsegnamenti() {
        return insegnamenti;
    }

    public void setInsegnamenti(Set<Corso> insegnamenti) {
        this.insegnamenti = insegnamenti;
    }

}
