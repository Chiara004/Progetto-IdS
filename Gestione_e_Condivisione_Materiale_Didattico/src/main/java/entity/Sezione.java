package entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Sezione {
    @Id
    private String titolo;

    @ManyToOne
    @JoinColumn(name = "corso_id")
    private Corso corso;

    @OneToMany(mappedBy = "sezione")
    private Set<MaterialeDidattico> materialeDidattico = new HashSet<>();

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

