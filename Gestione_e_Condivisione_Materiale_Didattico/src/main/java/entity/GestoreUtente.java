package entity;

import control.GestorePiattaformaSara;
import database.GestorePersistenza;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class GestoreUtente {

    private GestorePersistenza gestorePersistenza;

    public GestoreUtente() {

        gestorePersistenza = new GestorePersistenza();
    }

    public int inserimentoDatiUtente(String email, String nome, String cognome, String password, boolean isStudente) {

        // bisogna ancora scegliere il domino
        String emailLower = email.toLowerCase().trim();
        if (!emailLower.endsWith("@unina.it") && !emailLower.endsWith("@studenti.unina.it")) {
            return GestorePiattaformaSara.REGISTRAZIONE_FALLITA_DOMINIO_ERRATO;
        }

        List<Utente> utentiEsistenti = gestorePersistenza.cercaPerCampo(
                Utente.class,
                "emailIstituzionale",
                emailLower
        );

        if (!utentiEsistenti.isEmpty()) {
            return GestorePiattaformaSara.REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE;
        }

        String idGenerato = UUID.randomUUID().toString();
        Utente nuovoUtente;

        if (isStudente) {
            nuovoUtente = new Studente(idGenerato, nome, cognome, emailLower, password, "Studente");
        } else {
            nuovoUtente = new Docente(idGenerato, nome, cognome, emailLower, password, "Docente");
        }

        boolean salvatoConSuccesso = gestorePersistenza.salva(nuovoUtente);

        if (salvatoConSuccesso) {
            System.out.println("[GestorePersistenza] Nuovo utente registrato nel DB reale!");
            return GestorePiattaformaSara.REGISTRAZIONE_AVVENUTA;
        } else {
            return -1;
        }
    }


    public int inserimentoCredenziali(String email, String password) {
        return verificaCredenziali(email.toLowerCase().trim(), password);
    }


    private int verificaCredenziali(String email, String password) {

        // Usiamo il nome esatto: cercaPrimoPerCampi
        Utente utente = gestorePersistenza.cercaPrimoPerCampi(
                Utente.class,
                Map.of(
                        "emailIstituzionale", email,
                        "password", password
                )
        );

        if (utente == null) {
            System.out.println("[GestorePersistenza] Login fallito: credenziali non trovate.");
            return GestorePiattaformaSara.LOGIN_FALLITO;
        }

        System.out.println("[GestorePersistenza] Login accettato per: " + utente.getNome());

        if (utente instanceof Studente) {
            return GestorePiattaformaSara.LOGIN_SUCCESS_STUDENTE;
        } else if (utente instanceof Docente) {
            return GestorePiattaformaSara.LOGIN_SUCCESS_DOCENTE;
        }

        return GestorePiattaformaSara.LOGIN_FALLITO;
    }
}
