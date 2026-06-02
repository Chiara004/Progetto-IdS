package entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class MaterialeDidattico {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idMateriale;

    private String titolo;
    private String descrizione;
    private LocalDate dataPubblicazione;
    private String percorsoFile;

    @Enumerated(EnumType.STRING)
    private Categoria categoria;

    @Enumerated(EnumType.STRING)
    private Visibilita visibilita;

    @ManyToOne
    @JoinColumn(name = "corso_id")
    private Corso corso;

    @ManyToOne
    @JoinColumn(name = "sezione_titolo")
    private Sezione sezione;

    public MaterialeDidattico() {
    }

    public MaterialeDidattico(int idMateriale, String titolo, String descrizione, LocalDate dataPubblicazione,
                              String percorsoFile, Categoria categoria, Visibilita visibilita) {
        this.idMateriale = idMateriale;
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

    public void setIdMateriale(int idMateriale) {
        this.idMateriale = idMateriale;
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
}
