package entity;

import control.SessionManager;
import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreNotificaTest {

    private GestorePersistenza gestorePersistenza;
    private GestoreNotifica    gestoreNotifica;

    // Docente proprietario del corso
    private static final String MATRICOLA_DOCENTE = "DOC_NOT01";
    private static final String EMAIL_DOCENTE     = "docente.notifica@unina.it";

    // Studenti iscritti al corso
    private static final String MATRICOLA_STU1 = "N46001001";
    private static final String MATRICOLA_STU2 = "N46001002";
    private static final String EMAIL_STU1     = "studente.uno@unina.it";
    private static final String EMAIL_STU2     = "studente.due@unina.it";

    // Studente NON iscritto al corso
    private static final String MATRICOLA_STU3 = "N46001003";
    private static final String EMAIL_STU3     = "studente.tre@unina.it";

    // Corso di test
    private static final int    CODICE_CORSO  = 9100;
    private static final String TITOLO_CORSO  = "Corso Notifica Test";

    private Docente  docente;
    private Studente studente1;
    private Studente studente2;
    private Studente studenteNonIscritto;
    private Corso    corso;

    @BeforeEach
    void setUp() {
        gestorePersistenza = new GestorePersistenza();
        gestoreNotifica    = new GestoreNotifica();

        // Crea e salva il docente
        String passwordCriptata1 = BCrypt.hashpw("pwd", BCrypt.gensalt());
        docente = new Docente(MATRICOLA_DOCENTE, "Anna", "Bianchi", EMAIL_DOCENTE, passwordCriptata1, "Docente");
        gestorePersistenza.salva(docente);

        // Crea e salva gli studenti iscritti
        String passwordCriptata2 = BCrypt.hashpw("pwd", BCrypt.gensalt());
        studente1 = new Studente(MATRICOLA_STU1, "Mario", "Rossi",  EMAIL_STU1, passwordCriptata2, "Studente");
        String passwordCriptata3 = BCrypt.hashpw("pwd", BCrypt.gensalt());
        studente2 = new Studente(MATRICOLA_STU2, "Luigi", "Verdi",  EMAIL_STU2, passwordCriptata3, "Studente");
        gestorePersistenza.salva(studente1);
        gestorePersistenza.salva(studente2);

        // Crea e salva lo studente NON iscritto
        String passwordCriptata4 = BCrypt.hashpw("pwd", BCrypt.gensalt());
        studenteNonIscritto = new Studente(MATRICOLA_STU3, "Carlo", "Neri", EMAIL_STU3, passwordCriptata4, "Studente");
        gestorePersistenza.salva(studenteNonIscritto);

        // Crea il corso, associa docente e studenti iscritti
        corso = new Corso(CODICE_CORSO, TITOLO_CORSO, "Descrizione test", "2024/2025");
        corso.setDocente(docente);
        corso.getStudenti().add(studente1);
        corso.getStudenti().add(studente2);
        gestorePersistenza.salva(corso);

        studente1.getCorsi().add(corso);
        studente2.getCorsi().add(corso);
        gestorePersistenza.aggiorna(studente1);
        gestorePersistenza.aggiorna(studente2);

        // Imposta il docente come utente loggato nel SessionManager
        SessionManager.getInstance().setUtenteLoggato(docente);
    }

    @AfterEach
    void tearDown() {
        // Rimuove le notifiche generate durante i test
        List<Notifica> notifiche = gestorePersistenza.cercaPerCampo(Notifica.class, "studente", studente1);
        for (Notifica n : notifiche) {
            gestorePersistenza.elimina(Notifica.class, n.getIdNotifica());
        }

        notifiche = gestorePersistenza.cercaPerCampo(Notifica.class, "studente", studente2);
        for (Notifica n : notifiche) {
            gestorePersistenza.elimina(Notifica.class, n.getIdNotifica());
        }

        notifiche = gestorePersistenza.cercaPerCampo(Notifica.class, "studente", studenteNonIscritto);
        for (Notifica n : notifiche) {
            gestorePersistenza.elimina(Notifica.class, n.getIdNotifica());
        }

        // Rimuove corso, studenti e docente
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STU1);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STU2);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_STU3);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE);

        // Resetta la sessione
        SessionManager.getInstance().logout();
    }

    //TEST NOTIFICHE GENERATE CORRETTE
    @Test
    void testInviaNotifica_GeneraNotificaPerOgniStudenteIscritto() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        List<Notifica> notificheS1 = gestorePersistenza.cercaPerCampo(
                Notifica.class, "studente", studente1);
        List<Notifica> notificheS2 = gestorePersistenza.cercaPerCampo(
                Notifica.class, "studente", studente2);

        assertEquals(1, notificheS1.size(),
                "Lo studente 1 deve ricevere esattamente una notifica");
        assertEquals(1, notificheS2.size(),
                "Lo studente 2 deve ricevere esattamente una notifica");
    }

    //TEST TESTO NOTIFICA CORRETTO
    @Test
    void testInviaNotifica_TestoNotificaContieneNomeCorso() {
        // Act
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        List<Notifica> notifiche = gestorePersistenza.cercaPerCampo(
                Notifica.class, "studente", studente1);

        assertFalse(notifiche.isEmpty(), "Deve esserci almeno una notifica per lo studente 1");
        assertTrue(notifiche.get(0).getMessaggio().contains(TITOLO_CORSO),
                "Il testo della notifica deve contenere il titolo del corso");
    }

    //TEST NOTIFICHE NON GENERATE PER STUDENTI NON ISCRITTI
    @Test
    void testInviaNotifica_StudenteNonIscrittoNonRiceveNotifica() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        List<Notifica> notifiche = gestorePersistenza.cercaPerCampo(
                Notifica.class, "studente", studenteNonIscritto);
        assertTrue(notifiche.isEmpty(),
                "Uno studente non iscritto al corso non deve ricevere notifiche");
    }

    //TEST NUMERO NOTIFICHE CORRETTO
    @Test
    void testInviaNotifica_NumeroTotaleNotificheUgualeAStudentiIscritti() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        String messaggioAtteso = "Nuovo materiale didattico disponibile per il corso: " + TITOLO_CORSO;
        List<Notifica> tutteLeNotifiche = gestorePersistenza.cercaPerCampo(
                Notifica.class, "messaggio", messaggioAtteso);

        assertEquals(2, tutteLeNotifiche.size(),
                "Il numero di notifiche generate deve coincidere con il numero di studenti iscritti");
    }

    //TEST NOTIFICHE GENERATE PER CORSO SENZA ISCRITTI
    @Test
    void testInviaNotifica_CorsoSenzaStudenti_NessunaSalvata() {
        Corso corsoVuoto = new Corso(9101, "Corso Vuoto", "Nessuno iscritto", "2024/2025");
        corsoVuoto.setDocente(docente);
        gestorePersistenza.salva(corsoVuoto);
        gestoreNotifica.inviaNotifica("Corso Vuoto");

        String messaggioAtteso = "Nuovo materiale didattico disponibile per il corso: Corso Vuoto";
        List<Notifica> notifiche = gestorePersistenza.cercaPerCampo(
                Notifica.class, "messaggio", messaggioAtteso);

        assertTrue(notifiche.isEmpty(),
                "Un corso senza studenti iscritti non deve generare notifiche");

        gestorePersistenza.elimina(Corso.class, 9101);
    }

    // TEST STUDENTE SENZA NOTIFICHE
    @Test
    void testGetNotifiche_StudenteSenzaNotifiche_ListaVuota() {
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente1);

        List<Notifica> notifiche = GestoreNotifica.getNotifiche();

        assertNotNull(notifiche, "La lista non deve essere null");
        assertTrue(notifiche.isEmpty(), "Uno studente senza notifiche deve restituire lista vuota");
    }

    // TEST LISTA NOTIFICA NON VUOTA DOPO INVIO
    @Test
    void testGetNotifiche_DopoInvioNotifica_ListaNonVuota() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente1);

        List<Notifica> notifiche = GestoreNotifica.getNotifiche();

        assertFalse(notifiche.isEmpty(), "Lo studente deve avere almeno una notifica");
        assertEquals(1, notifiche.size(), "Lo studente deve avere esattamente una notifica");
    }

    // TEST getNotifiche() - il contenuto della notifica è corretto
    @Test
    void testGetNotifiche_ContenutoNotificaCorretto() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente1);

        List<Notifica> notifiche = GestoreNotifica.getNotifiche();

        assertFalse(notifiche.isEmpty());
        assertTrue(notifiche.get(0).getMessaggio().contains(TITOLO_CORSO),
                "Il messaggio della notifica deve contenere il titolo del corso");
    }

    // TEST getNotifiche() - uno studente vede solo le proprie notifiche
    @Test
    void testGetNotifiche_StudentiVedonoSoloLeProprieNotifiche() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente1);
        List<Notifica> notificheS1 = GestoreNotifica.getNotifiche();

        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente2);
        List<Notifica> notificheS2 = GestoreNotifica.getNotifiche();

        assertEquals(1, notificheS1.size(), "Studente 1 deve vedere solo la propria notifica");
        assertEquals(1, notificheS2.size(), "Studente 2 deve vedere solo la propria notifica");

        assertNotEquals(notificheS1.get(0).getIdNotifica(), notificheS2.get(0).getIdNotifica(),
                "Le notifiche dei due studenti devono essere distinte");
    }

    // TEST getNotifiche() - studente non iscritto non ha notifiche
    @Test
    void testGetNotifiche_StudenteNonIscritto_ListaVuota() {
        gestoreNotifica.inviaNotifica(TITOLO_CORSO);
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studenteNonIscritto);

        List<Notifica> notifiche = GestoreNotifica.getNotifiche();

        assertTrue(notifiche.isEmpty(),
                "Uno studente non iscritto al corso non deve avere notifiche");
    }
}
