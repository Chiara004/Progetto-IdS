package entity;

import control.SessionManager;
import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import control.GestorePiattaforma;
import org.mindrot.jbcrypt.BCrypt;

public class GestoreUtenteTest {
    private GestoreUtente gestoreUtente;
    private GestorePersistenza gestorePersistenza;

    // Dati dell'utente fittizio già registrato
    private final String EMAIL_ESISTENTE = "gia.registrato@unina.it";
    private final String MATRICOLA_ESISTENTE = "N46009999";

    //Dati utente non registrati per testare la registrazione
    private final String MATRICOLA_STUDENTE_NUOVA = "N46008888";
    private final String MATRICOLA_DOCENTE_NUOVA  = "DOC12345";
    private final String EMAIL_STUDENTE_NUOVA     = "nuovo.studente@unina.it";
    private final String EMAIL_DOCENTE_NUOVA      = "nuovo.docente@unina.it";

    private static String stringa(int n) {
        return "a".repeat(n);
    }

    // Stringa di esattamente 255 caratteri (limite valido)
    private static final String STRINGA_255  = stringa(255);
    // Stringa di 256 caratteri (un carattere oltre il limite)
    private static final String STRINGA_256  = stringa(256);

    @BeforeEach
    void setUp() {
        // Inizializza il gestore prima di ogni test per avere uno stato pulito
        gestoreUtente = new GestoreUtente();
        gestorePersistenza = new GestorePersistenza();

        // Utente fittizio nel database per simulare uno studente già registrato
        String passwordCriptata = BCrypt.hashpw("password123", BCrypt.gensalt());
        Studente studenteEsistente = new Studente(MATRICOLA_ESISTENTE, "Mario", "Rossi", EMAIL_ESISTENTE, passwordCriptata, "Studente");
        gestorePersistenza.salva(studenteEsistente);
    }

    @AfterEach
    void tearDown() {
        //Pulizia utente fittizio
        gestorePersistenza.elimina(Utente.class, MATRICOLA_ESISTENTE);

        //Pulizia registrazioni andate a buon fine
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STUDENTE_NUOVA);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE_NUOVA);
    }

    // TEST SULLA MATRICOLA DELLO STUDENTE
    @Test
    void testRegistrazioneStudente_MatricolaCorta() {
        String email = "m.rossi@unina.it";
        String matricolaCorta = "N460012"; // Meno di 9 caratteri
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaCorta, "Mario", "Rossi", "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito,
                "Una matricola studente troppo corta deve fallire");
    }

    @Test
    void testRegistrazioneStudente_MatricolaLunga() {
        String email = "m.rossi@unina.it";
        String matricolaCorta = "N46001200000"; // Piu di 9 caratteri
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaCorta, "Mario", "Rossi", "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito,
                "Una matricola studente troppo corta deve fallire");
    }

    @Test
    void testRegistrazioneStudente_PrefissoErrato() {
        String email = "m.rossi@unina.it";
        String matricolaErrata = "M46001234"; // Non inizia con N4600
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaErrata, "Mario", "Rossi", "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    //TEST SULLA MATRICOLA DEL DOCENTE
    @Test
    void testRegistrazioneDocente_MatricolaCorta() {
        String email = "d.amalfitano@unina.it";
        String matricolaCorta = "DOC"; // Non è maggiore di 3 caratteri
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaCorta, "Domenico", "Amalfitano", "pwd", false);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    @Test
    void testRegistrazioneDocente_PrefissoErrato() {
        String email = "d.amalfitano@unina.it";
        String matricolaErrata = "PROF1234"; // Non inizia con DOC
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaErrata, "Domenico", "Amalfitano", "pwd", false);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    // TEST SUL DOMINIO EMAIL
    @Test
    void testRegistrazione_DominioEmailErrato() {
        String emailErrata = "studente@gmail.com"; // Dominio non unina.it
        String matricolaValida = "N46001234";
        int esito = gestoreUtente.inserimentoDatiUtente(emailErrata, matricolaValida, "Mario", "Rossi", "pwd", true);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_DOMINIO_ERRATO, esito,
                "L'email deve obbligatoriamente terminare con @unina.it");
    }

    //TEST EMAIL GIA ESISTENTE
    @Test
    void testRegistrazione_EmailGiaEsistente() {
        String email = EMAIL_ESISTENTE;
        String matricolaNuova = "N46008888";
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaNuova, "Luigi", "Verdi", "pwd123", true);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE, esito,
                "Il sistema deve bloccare la registrazione se l'email è già presente nel database");
    }

    //TEST MATRICOLA GIA' ESISTENTE
    @Test
    void testRegistrazione_MatricolaGiaEsistente() {
        String emailNuova = "nuovo.studente@unina.it";
        String matricola = MATRICOLA_ESISTENTE;
        int esito = gestoreUtente.inserimentoDatiUtente(emailNuova, matricola, "Luigi", "Verdi", "pwd123", true);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE, esito,
                "Il sistema deve bloccare la registrazione se la matricola è già presente nel database");
    }

    //TEST REGISTRAZIONE STUDENTE ANDATA A BUON FINE
    @Test
    void testRegistrazioneStudente_DatiValidi() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_STUDENTE_NUOVA, MATRICOLA_STUDENTE_NUOVA, "Luigi", "Verdi", "pwd123", true);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_AVVENUTA, esito,
                "Una registrazione studente con dati validi deve avere successo");
    }

    //TEST REGISTRAZIONE DOCENTE ANDATA A BUON FINE
    @Test
    void testRegistrazioneDocente_DatiValidi() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_DOCENTE_NUOVA, MATRICOLA_DOCENTE_NUOVA, "Domenico", "Amalfitano", "pwd123", false);
        assertEquals(GestorePiattaforma.REGISTRAZIONE_AVVENUTA, esito,
                "Una registrazione docente con dati validi deve avere successo");
    }

    //TEST LOGIN STUDENTE ANDATO A BUON FINE
    @Test
    void testLogin_CredenzialStudenteCorrette() {
        int esito = gestoreUtente.inserimentoCredenziali(EMAIL_ESISTENTE, "password123");

        assertEquals(GestorePiattaforma.LOGIN_SUCCESS_STUDENTE, esito,
                "Il login con credenziali studente corrette deve restituire LOGIN_SUCCESS_STUDENTE");

        SessionManager.getInstance().logout();
    }

    //TEST LOGIN DOCENTE ANDATO A BUON FINE
    @Test
    void testLogin_CredenzialDocenteCorrette() {
        gestoreUtente.inserimentoDatiUtente(
                EMAIL_DOCENTE_NUOVA, MATRICOLA_DOCENTE_NUOVA, "Domenico", "Amalfitano", "docpwd", false);
        int esito = gestoreUtente.inserimentoCredenziali(EMAIL_DOCENTE_NUOVA, "docpwd");
        assertEquals(GestorePiattaforma.LOGIN_SUCCESS_DOCENTE, esito,
                "Il login con credenziali docente corrette deve restituire LOGIN_SUCCESS_DOCENTE");

        SessionManager.getInstance().logout();
    }

    //TEST LOGIN PASSWORD ERRATA
    @Test
    void testLogin_PasswordErrata() {
        int esito = gestoreUtente.inserimentoCredenziali(EMAIL_ESISTENTE, "passwordSbagliata");
        assertEquals(GestorePiattaforma.LOGIN_FALLITO, esito,
                "Il login con password errata deve fallire");
    }

    //TEST LOGIN EMAIL ERRATA
    @Test
    void testLogin_EmailNonEsistente() {
        // Act
        int esito = gestoreUtente.inserimentoCredenziali("inesistente@unina.it", "password123");

        // Assert
        assertEquals(GestorePiattaforma.LOGIN_FALLITO, esito,
                "Il login con email non registrata deve fallire");
    }

    //TEST LOGIN EMAIL CON MAIUSCOLE
    @Test
    void testLogin_EmailConMaiuscole() {
        int esito = gestoreUtente.inserimentoCredenziali(EMAIL_ESISTENTE.toUpperCase(), "password123");
        assertEquals(GestorePiattaforma.LOGIN_SUCCESS_STUDENTE, esito,
                "Il login deve funzionare indipendentemente dal case dell'email");
        SessionManager.getInstance().logout();
    }

    //TEST LOGIN PASSWORD VUOTA
    @Test
    void testLogin_PasswordVuota() {
        int esito = gestoreUtente.inserimentoCredenziali(EMAIL_ESISTENTE, "");
        assertEquals(GestorePiattaforma.LOGIN_FALLITO, esito,
                "Il login con password vuota deve fallire");
    }


    //TEST REGISTRAZIONE NOME 256 CARATTERI
    @Test
    void testRegistrazione_Nome256Caratteri_Rifiutato() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_STUDENTE_NUOVA, MATRICOLA_STUDENTE_NUOVA, STRINGA_256, "Rossi", "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO, esito,
                "Un nome di 256 caratteri deve essere rifiutato");
    }


    //TEST REGISTRAZIONE COGNOME 256 CARATTERI
    @Test
    void testRegistrazione_Cognome256Caratteri_Rifiutato() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_STUDENTE_NUOVA, MATRICOLA_STUDENTE_NUOVA, "Mario", STRINGA_256, "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO, esito,
                "Un cognome di 256 caratteri deve essere rifiutato");
    }

    //TEST REGISTRAZIONE MATRICOLA 256 CARATTERI
    @Test
    void testRegistrazione_MatricolaDocente256Caratteri_Rifiutato() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_STUDENTE_NUOVA, MATRICOLA_STUDENTE_NUOVA, "Mario", STRINGA_256, "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO, esito,
                "Un matricola di 256 caratteri deve essere rifiutato");
    }

    //TEST REGISTRAZIONE DATI 255 CARATTERI
    @Test
    void testRegistrazione_255Caratteri_Accettata() {
        // Costruisce un'email lunga esattamente 255 caratteri con dominio @unina.it valido
        String prefisso = stringa(255 - "@unina.it".length()); // 246 'a'
        String email255 = prefisso + "@unina.it";
        assertEquals(255, email255.length());
        String suffissoMatricola = stringa(252);
        String matrciola255= "DOC"+suffissoMatricola;
        int esito = gestoreUtente.inserimentoDatiUtente(
                email255, matrciola255, STRINGA_255, STRINGA_255, STRINGA_255, false);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_AVVENUTA, esito,
                "I dati di esattamente 255 caratteri devono essere accettata");

        gestorePersistenza.elimina(Utente.class, matrciola255);
    }

    //TEST REGISTRAZIONE EMAIL 256 CARATTERI
    @Test
    void testRegistrazione_Email256Caratteri_Rifiutata() {
        // 256 caratteri totali — supera il limite prima ancora del controllo dominio
        String prefisso = stringa(256 - "@unina.it".length()); // 247 'a'
        String email256 = prefisso + "@unina.it";
        assertEquals(256, email256.length());

        int esito = gestoreUtente.inserimentoDatiUtente(
                email256, MATRICOLA_STUDENTE_NUOVA, "Mario", "Rossi", "pwd", true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO, esito,
                "Un'email di 256 caratteri deve essere rifiutata");
    }

    //TEST REGISTRAZIONE PASSWORD 256 CARATTERI
    @Test
    void testRegistrazione_Password256Caratteri_Rifiutata() {
        int esito = gestoreUtente.inserimentoDatiUtente(
                EMAIL_STUDENTE_NUOVA, MATRICOLA_STUDENTE_NUOVA, "Mario", "Rossi", STRINGA_256, true);

        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_CAMPO_TROPPO_LUNGO, esito,
                "Una password di 256 caratteri deve essere rifiutata");
    }
}
