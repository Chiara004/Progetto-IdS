package control;

import entity.GestoreUtente;

public class GestorePiattaformaSara {

    public static final int REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE = 0;
    public static final int REGISTRAZIONE_FALLITA_DOMINIO_ERRATO = 1;
    public static final int REGISTRAZIONE_AVVENUTA = 2;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA = 3;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE = 4;

    public static final int LOGIN_FALLITO = 0;
    public static final int LOGIN_SUCCESS_STUDENTE = 1;
    public static final int LOGIN_SUCCESS_DOCENTE = 2;

    private GestoreUtente gestoreUtente;

    public GestorePiattaformaSara() {
        this.gestoreUtente = new GestoreUtente();
    }

    public int inserimentoDatiUtente(String email, String matricola, String nome, String cognome, String password, boolean isStudente) {
        return gestoreUtente.inserimentoDatiUtente(email, matricola, nome, cognome, password, isStudente);
    }

    public int inserimentoCredenziali(String email, String password) {
        return gestoreUtente.inserimentoCredenziali(email, password);
    }

}
