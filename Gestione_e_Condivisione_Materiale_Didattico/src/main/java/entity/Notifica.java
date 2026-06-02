package entity;

import jakarta.persistence.*;

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
}
