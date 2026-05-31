package entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Sezione {
    @Id
    private String titolo;

    public Sezione() {

    }
    public Sezione(String titolo) {
        this.titolo = titolo;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }
}

