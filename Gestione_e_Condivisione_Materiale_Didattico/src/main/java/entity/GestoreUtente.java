package entity;

import control.GestorePiattaforma;
import control.SessionManager;
import database.GestorePersistenza;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Map;


public class GestoreUtente {

    private GestorePersistenza gestorePersistenza;

    public GestoreUtente() {

        gestorePersistenza = new GestorePersistenza();
    }

    private String hashPassword(String passwordInChiaro) {
        if (passwordInChiaro == null) return null;
        return BCrypt.hashpw(passwordInChiaro, BCrypt.gensalt());
    }

    private boolean verificaPassword(String passwordInChiaro, String hashSalvato) {
        if (passwordInChiaro == null || hashSalvato == null) return false;
        return BCrypt.checkpw(passwordInChiaro, hashSalvato);
    }

    public int inserimentoDatiUtente(String email, String matricola, String nome, String cognome, String password, boolean isStudente) {

        String mat = matricola.trim();

        if (isStudente) {
            // Regola Studente: Deve iniziare con N4600 ed essere lunga esattamente 9 caratteri
            if (!mat.startsWith("N4600") || mat.length() != 9) {
                return GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA;
            }
        } else {
            // Regola Docente: Deve iniziare con DOC ed essere più lunga di 3 caratteri
            if (!mat.startsWith("DOC") || mat.length() <= 3) {
                return GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA;
            }
        }

        String emailLower = email.toLowerCase().trim();
        if (!emailLower.endsWith("@unina.it")) {
            return GestorePiattaforma.REGISTRAZIONE_FALLITA_DOMINIO_ERRATO;
        }

        List<Utente> utentiEsistenti = gestorePersistenza.cercaPerCampo(
                Utente.class,
                "emailIstituzionale",
                emailLower
        );

        if (!utentiEsistenti.isEmpty()) {
            return GestorePiattaforma.REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE;
        }

        List<Utente> matricoleEsistenti = gestorePersistenza.cercaPerCampo(
                Utente.class,
                "idUtente",
                mat
        );

        if (!matricoleEsistenti.isEmpty()) {
            return GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE;
        }

        Utente nuovoUtente;
        String passwordSicura = hashPassword(password);

        if (isStudente) {
            nuovoUtente = new Studente(matricola, nome, cognome, emailLower, passwordSicura, "Studente");
        } else {
            nuovoUtente = new Docente(matricola, nome, cognome, emailLower, passwordSicura, "Docente");
        }


        boolean salvatoConSuccesso = gestorePersistenza.salva(nuovoUtente);

        if (salvatoConSuccesso) {
            System.out.println("[GestorePersistenza] Nuovo utente registrato nel DB reale!");
            return GestorePiattaforma.REGISTRAZIONE_AVVENUTA;
        } else {
            return -1;
        }
    }


    public int inserimentoCredenziali(String email, String password) {
        return verificaCredenziali(email.toLowerCase().trim(), password);
    }


    private int verificaCredenziali(String email, String password) {


        Utente utente = gestorePersistenza.cercaPrimoPerCampi(
                Utente.class,
                Map.of(
                        "emailIstituzionale", email
                )
        );

        if (utente == null || !verificaPassword(password, utente.getPassword())) {
            System.out.println("[GestorePersistenza] Login fallito: credenziali non trovate.");
            return GestorePiattaforma.LOGIN_FALLITO;
        }

        System.out.println("[GestorePersistenza] Login accettato per: " + utente.getNome());
        SessionManager.getInstance().setUtenteLoggato(utente);

        if (utente instanceof Studente) {
            return GestorePiattaforma.LOGIN_SUCCESS_STUDENTE;
        } else if (utente instanceof Docente) {
            return GestorePiattaforma.LOGIN_SUCCESS_DOCENTE;
        }

        return GestorePiattaforma.LOGIN_FALLITO;
    }
}
