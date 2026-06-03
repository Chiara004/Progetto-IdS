package entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Sezione {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idSezione;

    @Column(nullable = false)
    private String titolo;

    @ManyToOne
    @JoinColumn(name = "corso_id", nullable= false)
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

    public int getIdSezione() {
        return idSezione;
    }


    public Corso getCorso() {
        return corso;
    }

    public void setCorso(Corso corso) {
        this.corso = corso;
    }

    public Set<MaterialeDidattico> getMaterialeDidattico() {
        return materialeDidattico;
    }

    public void setMaterialeDidattico(Set<MaterialeDidattico> materialeDidattico) {
        this.materialeDidattico = materialeDidattico;
    }
}

