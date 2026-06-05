package entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
public class MaterialeDidattico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idMateriale;

    @Column(nullable = false)
    private String titolo;
    private String descrizione;
    private LocalDate dataPubblicazione;
    private String percorsoFile;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private Visibilita visibilita;

    @ManyToOne
    @JoinColumn(name = "corso_id", nullable = false)
    private Corso corso;

    @ManyToOne
    @JoinColumn(name = "sezione_id", nullable = true)
    private Sezione sezione;

    public MaterialeDidattico() {
    }

    public MaterialeDidattico(String titolo, String descrizione, LocalDate dataPubblicazione,
                              String percorsoFile, Categoria categoria, Visibilita visibilita) {

        this.titolo = titolo;
        this.descrizione = descrizione;
        this.dataPubblicazione = dataPubblicazione;
        this.percorsoFile = percorsoFile;
        this.categoria = categoria;
        this.visibilita = visibilita;
    }

    public int getIdMateriale() {
        return idMateriale;
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

    public LocalDate getDataPubblicazione() {
        return dataPubblicazione;
    }

    public void setDataPubblicazione(LocalDate dataPubblicazione) {
        this.dataPubblicazione = dataPubblicazione;
    }

    public String getPercorsoFile() {
        return percorsoFile;
    }

    public void setPercorsoFile(String percorsoFile) {
        this.percorsoFile = percorsoFile;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Visibilita getVisibilita() {
        return visibilita;
    }

    public void setVisibilita(Visibilita visibilita) {
        this.visibilita = visibilita;
    }

    public Corso getCorso() {
        return corso;
    }

    public void setCorso(Corso corso) {
        this.corso = corso;
    }

    public Sezione getSezione() {
        return sezione;
    }

    public void setSezione(Sezione sezione) {
        this.sezione = sezione;
    }

    @Override
    public String toString(){
        return "MaterialeDidattico{" +
                "idMateriale=" + idMateriale +
                ", titolo='" + titolo + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", dataPubblicazione='" + dataPubblicazione + '\'' +
                ", percorsoFile='" + percorsoFile + '\'' +
                ", categoria=" + categoria +
                ", visibilita=" + visibilita +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof MaterialeDidattico))
            return false;

        MaterialeDidattico altro = (MaterialeDidattico) o;
        return Objects.equals(titolo, altro.titolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titolo);
    }
}
