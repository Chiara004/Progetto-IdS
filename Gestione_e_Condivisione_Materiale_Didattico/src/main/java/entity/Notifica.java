package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Notifica {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idNotifica;

    private String messaggio;

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
