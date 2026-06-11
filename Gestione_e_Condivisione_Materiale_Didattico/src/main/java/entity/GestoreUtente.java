package entity;

import database.GestorePersistenza;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
import java.util.Map;


public class GestoreUtente {
    public static final int REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE = 0;
    public static final int REGISTRAZIONE_FALLITA_DOMINIO_ERRATO = 1;
    public static final int REGISTRAZIONE_AVVENUTA = 2;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA = 3;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE = 4;
    public static final int REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO = 5;

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

        // Validazione lunghezza campi (varchar 255)
        if ((nome.length()>255) || (cognome.length()>255)
        || (email.length()>255) || (password.length()>255))
            return REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO;


        if (isStudente) {
            // Regola Studente: Deve iniziare con N4600 ed essere lunga esattamente 9 caratteri
            if (!mat.startsWith("N4600") || mat.length() != 9) {
                return REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA;
            }
        } else {
            // Regola Docente: Deve iniziare con DOC ed essere più lunga di 3 caratteri
            if (!mat.startsWith("DOC") || mat.length() <= 3) {
                return REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA;
            }
            else if(mat.length() > 255)
                return REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO;
        }

        String emailLower = email.toLowerCase().trim();
        if (!emailLower.endsWith("@unina.it")) {
            return REGISTRAZIONE_FALLITA_DOMINIO_ERRATO;
        }

        List<Utente> utentiEsistenti = gestorePersistenza.cercaPerCampo(
                Utente.class,
                "emailIstituzionale",
                emailLower
        );

        if (!utentiEsistenti.isEmpty()) {
            return REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE;
        }

        List<Utente> matricoleEsistenti = gestorePersistenza.cercaPerCampo(
                Utente.class,
                "idUtente",
                mat
        );

        if (!matricoleEsistenti.isEmpty()) {
            return REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE;
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
            return REGISTRAZIONE_AVVENUTA;
        } else {
            return -1;
        }
    }


    public Utente inserimentoCredenziali(String email, String password) {
        return verificaCredenziali(email.toLowerCase().trim(), password);
    }


    private Utente verificaCredenziali(String email, String password) {
        Utente utente = gestorePersistenza.cercaPrimoPerCampi(
                Utente.class,
                Map.of("emailIstituzionale", email)
        );

        // Se non lo trova o la password è errata, restituisce null
        if (utente == null || !verificaPassword(password, utente.getPassword())) {
            System.out.println("[GestoreUtente] Login fallito: credenziali errate.");
            return null;
        }

        System.out.println("[GestoreUtente] Login accettato per: " + utente.getNome());

        // Se ha successo, restituisce l'oggetto completo
        return utente;
    }
}
