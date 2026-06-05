package entity;

import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import control.GestorePiattaforma;

public class GestoreUtenteTest {
    private GestoreUtente gestoreUtente;
    private GestorePersistenza gestorePersistenza;

    // Dati dell'utente fittizio già registrato
    private final String EMAIL_ESISTENTE = "gia.registrato@unina.it";
    private final String MATRICOLA_ESISTENTE = "N46009999";

    @BeforeEach
    void setUp() {
        // Inizializza il gestore prima di ogni test per avere uno stato pulito
        gestoreUtente = new GestoreUtente();
        gestorePersistenza = new GestorePersistenza();

        // Utente fittizio nel database per simulare uno studente già registrato
        Studente studenteEsistente = new Studente(MATRICOLA_ESISTENTE, "Mario", "Rossi", EMAIL_ESISTENTE, "password123", "Studente");
        gestorePersistenza.salva(studenteEsistente);
    }

    @AfterEach
    void tearDown() {
        //Pulire il database eliminando l'utente fittizio alla fine di ogni test
        gestorePersistenza.elimina(Utente.class, MATRICOLA_ESISTENTE);

        // Per sicurezza, pulire anche l'eventuale matricola/email nuova usata nei test
        // nel caso l'inserimento dovesse riuscire per sbaglio
        gestorePersistenza.elimina(Utente.class, "N46008888");
    }

    // TEST SULLA MATRICOLA DELLO STUDENTE
    @Test
    void testRegistrazioneStudente_MatricolaCorta() {
        // Arrange
        String email = "m.rossi@unina.it";
        String matricolaCorta = "N460012"; // Meno di 9 caratteri

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaCorta, "Mario", "Rossi", "pwd", true);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito,
                "Una matricola studente troppo corta deve fallire");
    }

    @Test
    void testRegistrazioneStudente_PrefissoErrato() {
        // Arrange
        String email = "m.rossi@unina.it";
        String matricolaErrata = "M46001234"; // Non inizia con N4600

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaErrata, "Mario", "Rossi", "pwd", true);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    //TEST SULLA MATRICOLA DEL DOCENTE
    @Test
    void testRegistrazioneDocente_MatricolaCorta() {
        // Arrange
        String email = "d.amalfitano@unina.it";
        String matricolaCorta = "DOC"; // Non è maggiore di 3 caratteri

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaCorta, "Domenico", "Amalfitano", "pwd", false);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    @Test
    void testRegistrazioneDocente_PrefissoErrato() {
        // Arrange
        String email = "d.amalfitano@unina.it";
        String matricolaErrata = "PROF1234"; // Non inizia con DOC

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaErrata, "Domenico", "Amalfitano", "pwd", false);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA, esito);
    }

    // TEST SUL DOMINIO EMAIL
    @Test
    void testRegistrazione_DominioEmailErrato() {
        // Arrange
        String emailErrata = "studente@gmail.com"; // Dominio non unina.it
        String matricolaValida = "N46001234";

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(emailErrata, matricolaValida, "Mario", "Rossi", "pwd", true);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_DOMINIO_ERRATO, esito,
                "L'email deve obbligatoriamente terminare con @unina.it");
    }

    //TEST EMAIL GIA ESISTENTE
    @Test
    void testRegistrazione_EmailGiaEsistente() {
        // Arrange: Proviamo a usare l'email GIÀ ESISTENTE, ma con una matricola NUOVA
        String email = EMAIL_ESISTENTE;
        String matricolaNuova = "N46008888";

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(email, matricolaNuova, "Luigi", "Verdi", "pwd123", true);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE, esito,
                "Il sistema deve bloccare la registrazione se l'email è già presente nel database");
    }

    //TEST MATRICOLA GIA' ESISTENTE
    @Test
    void testRegistrazione_MatricolaGiaEsistente() {
        // Arrange: Proviamo a usare un'email NUOVA, ma con la matricola GIÀ ESISTENTE
        String emailNuova = "nuovo.studente@unina.it";
        String matricola = MATRICOLA_ESISTENTE;

        // Act
        int esito = gestoreUtente.inserimentoDatiUtente(emailNuova, matricola, "Luigi", "Verdi", "pwd123", true);

        // Assert
        assertEquals(GestorePiattaforma.REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE, esito,
                "Il sistema deve bloccare la registrazione se la matricola è già presente nel database");
    }
}
