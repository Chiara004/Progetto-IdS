package control;

import entity.Utente;

public class SessionManager {
    private static SessionManager instance;
    private Utente utenteLoggato;

    private SessionManager() {
        this.utenteLoggato = null;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setUtenteLoggato(Utente utente) {
        if (this.utenteLoggato != null) {
            // Se c'è già qualcuno loggato, blocchiamo tutto lanciando un'eccezione!
            // Per cambiare utente bisogna prima fare il logout.
            throw new IllegalStateException("Attenzione: C'è già un utente connesso nella sessione!");
        }
        this.utenteLoggato = utente;
    }

    public Utente getUtenteLoggato() {
        return utenteLoggato;
    }

    public void logout() {
        this.utenteLoggato = null;
    }
}
