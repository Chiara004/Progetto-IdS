package control;

import entity.Docente;
import entity.Studente;
import entity.Utente;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SessionManagerTest {

    private SessionManager sessionManager;
    private Studente studente;
    private Docente  docente;

    @BeforeEach
    void setUp() {
        sessionManager = SessionManager.getInstance();

        // Garantisce che la sessione sia pulita prima di ogni test
        sessionManager.logout();

        studente = new Studente("N46001234", "Mario", "Rossi",
                "m.rossi@unina.it", "pwd", "Studente");
        docente  = new Docente("DOC12345", "Anna", "Bianchi",
                "a.bianchi@unina.it", "pwd", "Docente");
    }

    @AfterEach
    void tearDown() {
        // Pulisce la sessione dopo ogni test per non inquinare i successivi
        sessionManager.logout();
    }

    //TEST GET INSTANCE
    @Test
    void testGetInstance_RestituisceStessaIstanza() {
        SessionManager istanza1 = SessionManager.getInstance();
        SessionManager istanza2 = SessionManager.getInstance();

        assertSame(istanza1, istanza2,
                "getInstance deve restituire sempre la stessa istanza (Singleton)");
    }

    //TEST ALL'INIZIO NESSUNO DEVE ESSERE LOGGATO
    @Test
    void testSessioneIniziale_NessunUtenteLoggato() {
        assertNull(sessionManager.getUtenteLoggato(),
                "A sessione appena pulita getUtenteLoggato deve restituire null");
    }

    //TEST STUDENTE LOGGATO CORRETTAMENTE
    @Test
    void testSetUtenteLoggato_StudenteLoggato_Correttamente() {
        sessionManager.setUtenteLoggato(studente);
        assertEquals(studente, sessionManager.getUtenteLoggato(),
                "L'utente loggato deve corrispondere allo studente impostato");
    }

    //TEST DOCENTE LOGGATO CORRETTAMENTE
    @Test
    void testSetUtenteLoggato_DocenteLoggato_Correttamente() {
        sessionManager.setUtenteLoggato(docente);
        assertEquals(docente, sessionManager.getUtenteLoggato(),
                "L'utente loggato deve corrispondere al docente impostato");
    }

    //TEST LOGGARE UN SECONDO UTENTE
    @Test
    void testSetUtenteLoggato_ConUtenteGiaPresente_LanciaEccezione() {
        sessionManager.setUtenteLoggato(studente);
        try {
            sessionManager.setUtenteLoggato(docente);
            fail("Loggare un secondo utente senza logout deve lanciare IllegalStateException");

        } catch (IllegalStateException eccezione) {
            assertTrue(eccezione.getMessage().contains("già un utente connesso"),
                    "Il messaggio dell'eccezione deve segnalare la presenza di un utente già connesso");
        }
    }

    //TEST LOGOUT
    @Test
    void testLogout_SessioneVuotaDopoLogout() {
        sessionManager.setUtenteLoggato(studente);
        sessionManager.logout();
        assertNull(sessionManager.getUtenteLoggato(),
                "Dopo il logout getUtenteLoggato deve restituire null");
    }

    //TEST LOGOUT SU SESSIONE VUOTA
    @Test
    void testLogout_SuSessioneGiaPulita_NonLanciaEccezione() {
        try {
            sessionManager.logout();

        } catch (Exception eccezione) {
            fail("Il logout su una sessione già vuota non deve lanciare eccezioni, ma ha lanciato: "
                    + eccezione.getClass().getSimpleName());
        }
    }

    //TEST LOGIN IN SEGUITO A UN LOGOUT
    @Test
    void testLogout_PermetteDiLoggareNuovoUtenteSuccessivamente() {
        sessionManager.setUtenteLoggato(studente);
        sessionManager.logout();

        try {
            sessionManager.setUtenteLoggato(docente);

        } catch (Exception eccezione) {
            fail("Dopo il logout deve essere possibile loggare un nuovo utente senza eccezioni. Errore lanciato: "
                    + eccezione.getClass().getSimpleName());
        }
        assertEquals(docente, sessionManager.getUtenteLoggato(),
                "Il nuovo utente deve essere correttamente impostato dopo il logout del precedente");
    }

    //TEST RILOGGARE STESSO UTENTE
    @Test
    void testLogout_DopoLogoutRiloggoStessoUtente() {
        sessionManager.setUtenteLoggato(studente);
        sessionManager.logout();
        sessionManager.setUtenteLoggato(studente);
        assertEquals(studente, sessionManager.getUtenteLoggato(),
                "Dopo il logout deve essere possibile riloggare lo stesso utente");
    }

    //TEST GET STUDENTE LOGGATO
    @Test
    void testGetUtenteLoggato_TipoStudentePreservato() {
        sessionManager.setUtenteLoggato(studente);
        Utente loggato = sessionManager.getUtenteLoggato();
        assertInstanceOf(Studente.class, loggato,
                "L'utente loggato deve essere riconoscibile come Studente");
    }

    //TEST GET DOCENTE LOGGATO
    @Test
    void testGetUtenteLoggato_TipoDocentePreservato() {
        sessionManager.setUtenteLoggato(docente);
        Utente loggato = sessionManager.getUtenteLoggato();
        assertInstanceOf(Docente.class, loggato,
                "L'utente loggato deve essere riconoscibile come Docente");
    }
}