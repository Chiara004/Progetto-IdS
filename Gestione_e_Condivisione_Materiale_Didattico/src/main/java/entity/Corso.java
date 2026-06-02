package entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Corso {
    @Id
    private int codice;

    private String titolo;
    private String descrizione;

    //è un parametro opzionale
    @Column(nullable = true)
    private String annoAccademico;

    @OneToMany(mappedBy = "corso", cascade = CascadeType.ALL)
    private Set<Sezione> sezioni;
    @OneToMany(mappedBy = "corso", cascade = CascadeType.ALL)
    private Set<MaterialeDidattico> materialeDidattico;

    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToMany
    private Set<Studente> studenti = new HashSet<>();

    public Corso() {
    }

    public Corso(int codice, String titolo, String descrizione, String annoAccademico) {
        this.codice = codice;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.annoAccademico = annoAccademico;
        this.sezioni = new HashSet<>();
        this.materialeDidattico = new HashSet<>();
    }

    public Corso(int codice,String titolo, String descrizione) {
        this.codice = codice;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.sezioni = new HashSet<>();
        this.materialeDidattico = new HashSet<>();
    }

    public int getCodice() {
        return codice;
    }

    public void setCodice(int codice) {
        this.codice = codice;
    }

    public String getTitolo() {
        return titolo;
    }

    public void setTitolo(String titolo) {
        this.titolo = titolo;
    }

    public String getDescrizione() {
        return descrizione;
    }

    public void setDescrizione(String descrizione) {
        this.descrizione = descrizione;
    }

    public String getAnnoAccademico() {
        return annoAccademico;
    }

    public void setAnnoAccademico(String annoAccademico) {
        this.annoAccademico = annoAccademico;
    }

    public Set<Sezione> getSezioni() {
        return sezioni;
    }

    public void setSezioni(Set<Sezione> sezioni) {
        this.sezioni = sezioni;
    }

    public Set<MaterialeDidattico> getMaterialeDidattico() {
        return materialeDidattico;
    }

    public void setMaterialeDidattico(Set<MaterialeDidattico> materialeDidattico) {
        this.materialeDidattico = materialeDidattico;
    }
}
