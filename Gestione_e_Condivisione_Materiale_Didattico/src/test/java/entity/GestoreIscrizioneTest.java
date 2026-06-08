package entity;

import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreIscrizioneTest {

    private GestoreIscrizione  gestoreIscrizione;
    private GestorePersistenza gestorePersistenza;

    // Studente iscritto a corsi
    private static final String MATRICOLA_STU1 = "N46005001";
    private static final String EMAIL_STU1     = "studente.iscritto@unina.it";

    // Studente non iscritto a nessun corso
    private static final String MATRICOLA_STU2 = "N46005002";
    private static final String EMAIL_STU2     = "studente.noniscritto@unina.it";

    // Docenti e corsi di test
    private static final String MATRICOLA_DOC1 = "DOC_ISC01";
    private static final String MATRICOLA_DOC2 = "DOC_ISC02";
    private static final int    CODICE_CORSO1  = 9500;
    private static final int    CODICE_CORSO2  = 9501;
    private static final String TITOLO_CORSO1  = "Corso Iscrizione Alpha";
    private static final String TITOLO_CORSO2  = "Corso Iscrizione Beta";

    private Studente studente1;
    private Studente studente2;
    private Docente  docente1;
    private Docente  docente2;
    private Corso    corso1;
    private Corso    corso2;

    @BeforeEach
    void setUp() {
        gestoreIscrizione  = new GestoreIscrizione();
        gestorePersistenza = new GestorePersistenza();

        // Crea e salva i docenti
        String passwordCriptata1 = BCrypt.hashpw("password123", BCrypt.gensalt());
        docente1 = new Docente(MATRICOLA_DOC1, "Anna", "Bianchi", "doc1.isc@unina.it", passwordCriptata1, "Docente");
        String passwordCriptata2 = BCrypt.hashpw("password123", BCrypt.gensalt());
        docente2 = new Docente(MATRICOLA_DOC2, "Paolo", "Neri",   "doc2.isc@unina.it", passwordCriptata2, "Docente");
        gestorePersistenza.salva(docente1);
        gestorePersistenza.salva(docente2);

        // Crea e salva gli studenti
        String passwordCriptata3 = BCrypt.hashpw("password123", BCrypt.gensalt());
        studente1 = new Studente(MATRICOLA_STU1, "Mario", "Rossi", EMAIL_STU1, passwordCriptata3, "Studente");
        String passwordCriptata4 = BCrypt.hashpw("password123", BCrypt.gensalt());
        studente2 = new Studente(MATRICOLA_STU2, "Luigi", "Verdi", EMAIL_STU2, passwordCriptata4, "Studente");
        gestorePersistenza.salva(studente1);
        gestorePersistenza.salva(studente2);

        // Crea i corsi e iscrive studente1 a entrambi
        corso1 = new Corso(CODICE_CORSO1, TITOLO_CORSO1, "desc", "2024/2025");
        corso1.setDocente(docente1);
        corso1.getStudenti().add(studente1);

        corso2 = new Corso(CODICE_CORSO2, TITOLO_CORSO2, "desc", "2024/2025");
        corso2.setDocente(docente2);
        corso2.getStudenti().add(studente1);

        // studente2 non viene iscritto a nessun corso
        gestorePersistenza.salva(corso1);
        gestorePersistenza.salva(corso2);
    }

    @AfterEach
    void tearDown() {
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO1);
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO2);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STU1);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STU2);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOC1);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOC2);
    }

    //VISUALIZZA ELENCO CORSI
    @Test
    void testVisualizzaElencoCorsi_StudenteIscritto_RestituisceCorsi() {
        // Act
        Set<Corso> corsi = gestoreIscrizione.visualizzaElencoCorsi(EMAIL_STU1);

        // Assert
        assertNotNull(corsi, "Il set dei corsi non deve essere null");
        assertFalse(corsi.isEmpty(),
                "Uno studente iscritto deve avere almeno un corso");
    }

    @Test
    void testVisualizzaElencoCorsi_CoincideConVerificaIscrizione() {
        // visualizzaElencoCorsi è una delega pura — deve restituire lo stesso risultato
        Set<Corso> daVisualizza  = gestoreIscrizione.visualizzaElencoCorsi(EMAIL_STU1);
        Set<Corso> daVerifica    = gestoreIscrizione.verificaIscrizioneCorso(EMAIL_STU1);

        assertEquals(daVerifica.size(), daVisualizza.size(),
                "visualizzaElencoCorsi deve restituire lo stesso risultato di verificaIscrizioneCorso");
    }

    //TEST VERIFICA ISCRIZIONE STUDENTE ISCRITTO A DUE CORSI
    @Test
    void testVerificaIscrizione_StudenteIscrittoADueCorsi_RestituisceEntrambi() {
        // Act
        Set<Corso> corsi = gestoreIscrizione.verificaIscrizioneCorso(EMAIL_STU1);

        // Assert
        assertEquals(2, corsi.size(),
                "Lo studente iscritto a 2 corsi deve riceverne esattamente 2");
    }

    //TEST VERIFICA ISCRIZIONE STUDENTE NON ISCRITTO A NESSUN CORSO
    @Test
    void testVerificaIscrizione_StudenteSenzaCorsi_SetVuoto() {
        // studente2 non è iscritto a nessun corso
        Set<Corso> corsi = gestoreIscrizione.verificaIscrizioneCorso(EMAIL_STU2);

        assertNotNull(corsi, "Il set non deve essere null anche se lo studente non ha corsi");
        assertTrue(corsi.isEmpty(),
                "Uno studente non iscritto a nessun corso deve ricevere un set vuoto");
    }


    //TEST VERIFICA ISCRIZIONE CORSI DI UN STUDENTE
    @Test
    void testVerificaIscrizione_NonRestituisceCorsiDiAltriStudenti() {
        // I corsi di studente1 non devono comparire tra quelli di studente2
        Set<Corso> corsiStudente1 = gestoreIscrizione.verificaIscrizioneCorso(EMAIL_STU1);
        Set<Corso> corsiStudente2 = gestoreIscrizione.verificaIscrizioneCorso(EMAIL_STU2);

        boolean intersezioneVuota = true;
        for (Corso c2 : corsiStudente2) {
            for (Corso c1 : corsiStudente1) {
                if (c1.getCodice() == c2.getCodice()) {
                    intersezioneVuota = false;
                    break;
                }
            }
            if (!intersezioneVuota) {
                break;
            }
        }

        assertTrue(intersezioneVuota,
                "I corsi di uno studente non devono comparire nell'elenco di un altro");
    }
}