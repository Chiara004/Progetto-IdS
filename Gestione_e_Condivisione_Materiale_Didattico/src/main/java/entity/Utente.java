package entity;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Utente {
    @Id
    @Column(name = "id_utente")
    private String idUtente;

    private String nome;
    private String cognome;
    private String emailIstituzionale;
    private String password;
    private String ruolo;


    public Utente() {
    }

    public Utente(String idUtente, String nome, String cognome,
                  String emailIstituzionale, String password, String ruolo) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.emailIstituzionale = emailIstituzionale;
        this.password = password;
        this.ruolo = ruolo;
    }

    public String getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(String idUtente) {
        this.idUtente = idUtente;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public String getEmailIstituzionale() {
        return emailIstituzionale;
    }

    public void setEmailIstituzionale(String emailIstituzionale) {
        this.emailIstituzionale = emailIstituzionale;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRuolo() {
        return ruolo;
    }

    public void setRuolo(String ruolo) {
        this.ruolo = ruolo;
    }
}
