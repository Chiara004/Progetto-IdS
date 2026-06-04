package entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Notifica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idNotifica;

    private String messaggio;

    @ManyToOne
    @JoinColumn(name = "studente_id")
    private Studente studente;

    public Notifica() {
    }

    public Notifica(String messaggio) {
        this.messaggio = messaggio;
    }


    public int getIdNotifica() {
        return idNotifica;
    }

    public String getMessaggio() {
        return messaggio;
    }

    public void setMessaggio(String messaggio) {
        this.messaggio = messaggio;
    }

    public Studente getStudente() {
        return studente;
    }

    public void setStudente(Studente studente) {
        this.studente = studente;
    }

    @Override
    public String toString(){
        return "Notifica{" +
                "idNotifica=" + idNotifica +
                ", messaggio='" + messaggio + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Notifica))
            return false;

        Notifica altro = (Notifica) o;
        return Objects.equals(idNotifica, altro.idNotifica);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNotifica);
    }
}
