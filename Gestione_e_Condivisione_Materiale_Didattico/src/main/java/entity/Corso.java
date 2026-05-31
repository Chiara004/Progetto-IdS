package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Corso {
    @Id
    private int codice;

    private String titolo;
    private String descrizione;

    //è un parametro opzionale
    @Column(nullable = true)
    private String annoAccademico;

    public Corso() {
    }

    public Corso(int codice, String titolo, String descrizione, String annoAccademico) {
        this.codice = codice;
        this.titolo = titolo;
        this.descrizione = descrizione;
        this.annoAccademico = annoAccademico;
    }

    public Corso(int codice,String titolo, String descrizione) {
        this.codice = codice;
        this.titolo = titolo;
        this.descrizione = descrizione;
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
}
