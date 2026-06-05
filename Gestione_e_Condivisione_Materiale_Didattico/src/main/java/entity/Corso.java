package entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
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

    @OneToMany(mappedBy = "corso", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<Sezione> sezioni;
    @OneToMany(mappedBy = "corso", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private Set<MaterialeDidattico> materialeDidattico;

    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "corso_studente",
            joinColumns = @JoinColumn(name = "corso_id"),
            inverseJoinColumns = @JoinColumn(name = "studente_id")
    )
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

    public Docente getDocente() {
        return docente;
    }

    public void setDocente(Docente docente) {
        this.docente = docente;
    }

    public Set<Studente> getStudenti() {
        return studenti;
    }

    public void setStudenti(Set<Studente> studenti) {
        this.studenti = studenti;
    }

    public void rimuoviMateriale(MaterialeDidattico materiale){
        this.materialeDidattico.remove(materiale);
    }

    public String getPercorsoFileMateriale(MaterialeDidattico materiale){
        return materiale.getPercorsoFile();
    }

    public MaterialeDidattico getMaterialeDidatticoPerTitolo(String titolo){
        for(MaterialeDidattico materiale : materialeDidattico){
            if(materiale.getTitolo().equals(titolo))
                return materiale;
        }
        return null;
    }

    public MaterialeDidattico getMaterialeDidatticoPerId(int id){
        for(MaterialeDidattico materiale : materialeDidattico){
            if(materiale.getIdMateriale() == id)
                return materiale;
        }
        return null;
    }

    public Sezione getSezionePerTitolo(String titolo){
        for(Sezione sezione : sezioni){
            if(sezione.getTitolo().equals(titolo))
                return sezione;
        }
        return null;
    }

    public boolean inserisciMateriale(String titolo, String descrizione, String visibilita, String percorsoFile, String sezione, String categoria){
        // Validazione lunghezza campi (boundary varchar 255)
        if (descrizione.length() > 255 ||
                (titolo.length()>255) ||
                (percorsoFile.length() > 255) )
            return false;

        // Controllo omonimia (Regola di business)
        for (MaterialeDidattico m : this.materialeDidattico) {
            if (m.getTitolo().equalsIgnoreCase(titolo)) {
                // Esiste già un materiale con questo titolo!
                return false;
            }
        }

        // Se passa il controllo, si aggiunge alla lista
        Categoria cat = Categoria.valueOf(categoria);
        Visibilita vis = Visibilita.valueOf(visibilita);
        LocalDate data = LocalDate.now();
        MaterialeDidattico nuovoMateriale = new MaterialeDidattico(titolo, descrizione, data, percorsoFile, cat, vis);
        nuovoMateriale.setCorso(this);
        this.materialeDidattico.add(nuovoMateriale);

        if(!sezione.equals("null")){
            Sezione s = getSezionePerTitolo(sezione);
            nuovoMateriale.setSezione(s);
        }
        return true;
    }

    public boolean modificaMateriale(String idMateriale, String titolo, String descrizione, String visibilita, String percorsoFile, String sezione, String categoria){
        if (descrizione.length() > 255 ||
                (titolo.length()>255) ||
                (percorsoFile.length() > 255) )
            return false;

        // Controllo omonimia (Regola di business)
        for (MaterialeDidattico m : this.materialeDidattico) {
            if (m.getTitolo().equalsIgnoreCase(titolo) && m.getIdMateriale() != Integer.parseInt(idMateriale)) {
                // Esiste già un materiale con questo titolo!
                return false;
            }
        }

        // Se passa il controllo, si aggiunge alla lista
        Categoria cat = Categoria.valueOf(categoria);
        Visibilita vis = Visibilita.valueOf(visibilita);
        LocalDate data = LocalDate.now();
        int id = Integer.parseInt(idMateriale);
        MaterialeDidattico materialeAggiornato = getMaterialeDidatticoPerId(id);
        materialeAggiornato.setTitolo(titolo);
        materialeAggiornato.setDescrizione(descrizione);
        materialeAggiornato.setDataPubblicazione(data);
        materialeAggiornato.setPercorsoFile(percorsoFile);
        materialeAggiornato.setCategoria(cat);
        materialeAggiornato.setVisibilita(vis);
        materialeAggiornato.setCorso(this);

        this.materialeDidattico.add(materialeAggiornato);

        if(!sezione.equals("null")){
            Sezione s = getSezionePerTitolo(sezione);
            materialeAggiornato.setSezione(s);
        }
        return true;
    }


    @Override
    public String toString(){
        return "Corso{" +
                "codice=" + codice +
                ", titolo='" + titolo + '\'' +
                ", descrizione='" + descrizione + '\'' +
                ", annoAccademico='" + annoAccademico + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (!(o instanceof Corso))
            return false;

        Corso altro = (Corso) o;
        return Objects.equals(codice, altro.codice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codice);
    }
}
