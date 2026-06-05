package entity;

import control.SessionManager;
import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreCorsoTest {

    private GestorePersistenza gestorePersistenza;
    private GestoreCorso       gestoreCorso;

    // Docente loggato
    private static final String MATRICOLA_DOCENTE = "DOC_COR01";
    private static final String EMAIL_DOCENTE     = "docente.corso@unina.it";

    // Secondo docente (per test isolamento)
    private static final String MATRICOLA_DOCENTE2 = "DOC_COR02";
    private static final String EMAIL_DOCENTE2     = "docente2.corso@unina.it";

    // Corso principale
    private static final int    CODICE_CORSO  = 9200;
    private static final String TITOLO_CORSO  = "Corso GestoreCorso Test";

    // Corso del secondo docente (stesso titolo, docente diverso)
    private static final int    CODICE_CORSO2 = 9201;

    // Sezione pre-esistente
    private static final String TITOLO_SEZIONE = "Sezione Alpha";

    // Materiale pre-esistente
    private static final String TITOLO_MAT_ESISTENTE  = "Slide Introduttive";
    private static final String PERCORSO_MAT_ESISTENTE = "/files/slide_intro.pdf";

    private Docente docente;
    private Docente docente2;
    private Corso corso;
    private Corso corso2;

    @BeforeEach
    void setUp() {
        gestorePersistenza = new GestorePersistenza();

        // Crea e salva i docenti
        String passwordCriptata = BCrypt.hashpw("password123", BCrypt.gensalt());
        docente  = new Docente(MATRICOLA_DOCENTE,  "Anna",  "Bianchi", EMAIL_DOCENTE,  passwordCriptata, "Docente");
        String passwordCriptata2 = BCrypt.hashpw("password123", BCrypt.gensalt());
        docente2 = new Docente(MATRICOLA_DOCENTE2, "Paolo", "Neri",    EMAIL_DOCENTE2, passwordCriptata2, "Docente");
        gestorePersistenza.salva(docente);
        gestorePersistenza.salva(docente2);

        // Crea il corso principale con una sezione e un materiale pre-esistente
        corso = new Corso(CODICE_CORSO, TITOLO_CORSO, "Descrizione test", "2024/2025");
        corso.setDocente(docente);

        Sezione sezione = new Sezione(TITOLO_SEZIONE);
        corso.getSezioni().add(sezione);

        corso.inserisciMateriale(
                TITOLO_MAT_ESISTENTE,
                "Descrizione iniziale",
                "PUBBLICATO",
                PERCORSO_MAT_ESISTENTE,
                "null",
                "SLIDE"
        );
        gestorePersistenza.salva(corso);

        // Crea un corso omonimo intestato al secondo docente
        corso2 = new Corso(CODICE_CORSO2, TITOLO_CORSO, "Corso del secondo docente", "2024/2025");
        corso2.setDocente(docente2);
        gestorePersistenza.salva(corso2);

        // Imposta il docente principale come utente loggato,
        SessionManager.getInstance().setUtenteLoggato(docente);
        gestoreCorso = new GestoreCorso();
    }

    @AfterEach
    void tearDown() {
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO2);
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE2);
        SessionManager.getInstance().logout();
    }

    //TEST RECUPERA MATERIALI
    @Test
    void testRecuperaMateriali_RestituisceIMaterialiDelCorso() {
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        assertNotNull(materiali, "Il set dei materiali non deve essere null");
        assertFalse(materiali.isEmpty(), "Il corso ha un materiale: il set non deve essere vuoto");
    }

    //TEST RECUPERA MATERIALI CON UNO PREESISTENTE
    @Test
    void testRecuperaMateriali_ContieneIlMaterialePreesistente() {
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);

        boolean trovato = materiali.stream()
                .anyMatch(m -> m.getTitolo().equals(TITOLO_MAT_ESISTENTE));
        assertTrue(trovato, "Il set deve contenere il materiale pre-esistente");
    }

    //TEST RECUPERA MATERIALI ISOLATO
    @Test
    void testRecuperaMateriali_IsolatoPerDocenteLoggato() {
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);

        // i materiali devono essere solo quelli del corso del docente loggato
        // corso2 è vuoto, quindi se arrivassero materiali da lì il conteggio sarebbe sbagliato
        assertEquals(1, materiali.size(),
                "Devono essere restituiti solo i materiali del corso del docente loggato");
    }

    //TEST GET SEZIONE
    @Test
    void testGetSezioni_RestituisceLeSezioniDelCorso() {
        Set<Sezione> sezioni = gestoreCorso.getSezioni(TITOLO_CORSO);
        assertNotNull(sezioni, "Il set delle sezioni non deve essere null");
        assertFalse(sezioni.isEmpty(), "Il corso ha una sezione: il set non deve essere vuoto");
    }

    //TEST GET SEZIONE CON UNA PREESISTENTE
    @Test
    void testGetSezioni_ContieneSezionePreesistente() {
        Set<Sezione> sezioni = gestoreCorso.getSezioni(TITOLO_CORSO);
        boolean trovata = sezioni.stream()
                .anyMatch(s -> s.getTitolo().equals(TITOLO_SEZIONE));
        assertTrue(trovata, "Il set deve contenere la sezione pre-esistente");
    }

    //TEST GET SEZIONE ISOLATO
    @Test
    void testGetSezioni_IsolatoPerDocenteLoggato() {
        //corso2 è dello stesso titolo ma di docente2, non deve interferire
        Set<Sezione> sezioni = gestoreCorso.getSezioni(TITOLO_CORSO);

        assertEquals(1, sezioni.size(),
                "Devono essere restituite solo le sezioni del corso del docente loggato");
    }

    //TEST GET PERCORSO FILE
    @Test
    void testGetPercorsoFile_RestituisceIlPercorsoCorretto() {
        String percorso = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertEquals(PERCORSO_MAT_ESISTENTE, percorso,
                "Il percorso restituito deve corrispondere a quello salvato nel materiale");
    }

    //TEST GET ID MATERIALE
    @Test
    void testGetIdMateriale_RestituisceIdPositivo() {
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertTrue(id > 0, "L'id del materiale generato dal DB deve essere positivo");
    }

    //TEST COERENZA ID MATERIALE
    @Test
    void testGetIdMateriale_CoerenzaConRecuperaMateriali() {
        // Arrange: recupera l'id direttamente dal corso per confronto
        MaterialeDidattico materialeAtteso = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(materialeAtteso);
        int idRestituito = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        assertEquals(materialeAtteso.getIdMateriale(), idRestituito,
                "L'id restituito deve coincidere con quello del materiale nel corso");
    }

    // =========================================================
    // TEST — inserisciMateriale (senza GestoreFile: percorso passato direttamente)
    // =========================================================

    @Test
    void testInserisciMateriale_TitoloNuovo_ReturnTrue() {
        // Arrange: passiamo null come File — il metodo chiamerà gestoreFile.salvaFile(null)
        // Nota: questo test è valido solo se GestoreFile.salvaFile(null) restituisce
        // un percorso placeholder (es. null o stringa vuota) senza lanciare eccezioni.
        // Se lancia eccezione, questo test va annotato con @Disabled fino all'introduzione del mock.

        // Act
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                "Nuovo Materiale",
                "Descrizione nuova",
                "DISPENSE",
                "PUBBLICATO",
                null,   // file fisico non testato
                "null"
        );

        // Assert
        assertTrue(esito, "L'inserimento con titolo nuovo deve restituire true");
    }

    @Test
    void testInserisciMateriale_TitoloOmonimo_ReturnFalse() {
        // Act: titolo già presente nel corso
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                TITOLO_MAT_ESISTENTE,   // omonimo
                "Altra descrizione",
                "SLIDE",
                "PUBBLICO",
                null,
                "null"
        );

        // Assert
        assertFalse(esito, "L'inserimento con titolo già esistente deve restituire false");
    }

    @Test
    void testInserisciMateriale_TitoloOmonimoCaseDiverso_ReturnFalse() {
        // La regola di omonimia usa equalsIgnoreCase
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                TITOLO_MAT_ESISTENTE.toLowerCase(),
                "Altra descrizione",
                "SLIDE",
                "PUBBLICO",
                null,
                "null"
        );

        assertFalse(esito, "L'inserimento con titolo omonimo (case diverso) deve restituire false");
    }

    @Test
    void testInserisciMateriale_MaterialeVisibileDopoInserimento() {
        // Act
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Materiale Verificato", "desc",
                "DISPENSA", "PUBBLICO", null, "null"
        );

        // Assert: il materiale deve essere recuperabile tramite recuperaMateriali
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean trovato = materiali.stream()
                .anyMatch(m -> m.getTitolo().equals("Materiale Verificato"));
        assertTrue(trovato, "Il materiale inserito deve essere presente nel corso dopo l'inserimento");
    }

    // =========================================================
    // TEST — rimuoviMateriale (senza GestoreFile: eliminaFileFisico non testato)
    // =========================================================

    @Test
    void testRimuoviMateriale_TitoloEsistente_ReturnTrue() {
        // Act
        boolean esito = gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Assert
        assertTrue(esito, "La rimozione di un materiale esistente deve restituire true");
    }

    @Test
    void testRimuoviMateriale_TitoloInesistente_ReturnFalse() {
        // Act
        boolean esito = gestoreCorso.rimuoviMateriale(TITOLO_CORSO, "Titolo Inesistente");

        // Assert
        assertFalse(esito, "La rimozione di un materiale inesistente deve restituire false");
    }

    @Test
    void testRimuoviMateriale_MaterialeNonPiuRecuperabile() {
        // Act
        gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Assert: dopo la rimozione il materiale non deve più apparire nel corso
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean ancoraPresente = materiali.stream()
                .anyMatch(m -> m.getTitolo().equals(TITOLO_MAT_ESISTENTE));
        assertFalse(ancoraPresente, "Il materiale rimosso non deve essere più presente nel corso");
    }

    @Test
    void testRimuoviMateriale_AltriMaterialiNonInfluenzati() {
        // Arrange: inserisco un secondo materiale, poi rimuovo solo il primo
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Materiale Da Tenere", "desc",
                "DISPENSA", "PUBBLICO", null, "null"
        );

        // Act
        gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Assert: il secondo materiale deve sopravvivere
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean tenutoPresente = materiali.stream()
                .anyMatch(m -> m.getTitolo().equals("Materiale Da Tenere"));
        assertTrue(tenutoPresente, "Gli altri materiali non devono essere rimossi");
    }

    // =========================================================
    // TEST — modificaMateriale (senza GestoreFile: file=null → mantiene percorso attuale)
    // =========================================================

    @Test
    void testModificaMateriale_DatiValidi_ReturnTrue() {
        // Arrange
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Act: file null → usa il percorso già presente
        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO,
                String.valueOf(id),
                "Slide Aggiornate",
                "Nuova descrizione",
                "SLIDE",
                "PRIVATO",
                null,   // nessun file nuovo
                "null"
        );

        // Assert
        assertTrue(esito, "La modifica con dati validi deve restituire true");
    }

    @Test
    void testModificaMateriale_TitoloOmonimoAltroMateriale_ReturnFalse() {
        // Arrange: inserisco un secondo materiale, poi provo a rinominarlo con il titolo del primo
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Secondo Materiale", "desc",
                "SLIDE", "PUBBLICO", null, "null"
        );
        int idSecondo = gestoreCorso.getIdMateriale(TITOLO_CORSO, "Secondo Materiale");

        // Act
        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO,
                String.valueOf(idSecondo),
                TITOLO_MAT_ESISTENTE,   // omonimo del primo
                "desc",
                "SLIDE",
                "PUBBLICO",
                null,
                "null"
        );

        // Assert
        assertFalse(esito, "Non si può rinominare un materiale con il titolo già usato da un altro");
    }

    @Test
    void testModificaMateriale_StessoTitoloStessoMateriale_ReturnTrue() {
        // Arrange: modifico mantenendo lo stesso titolo — non è omonimia
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Act
        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO,
                String.valueOf(id),
                TITOLO_MAT_ESISTENTE,   // stesso titolo, stesso materiale
                "Descrizione aggiornata",
                "SLIDE",
                "PUBBLICO",
                null,
                "null"
        );

        // Assert
        assertTrue(esito, "Mantenere il proprio titolo non è omonimia e deve passare");
    }

    @Test
    void testModificaMateriale_PercorsoInvariatoSenzaNuovoFile() {
        // Arrange
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // Act: modifica senza nuovo file
        gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(id),
                TITOLO_MAT_ESISTENTE, "desc aggiornata",
                "SLIDE", "PUBBLICO", null, "null"
        );

        // Assert: il percorso non deve essere cambiato
        String percorso = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertEquals(PERCORSO_MAT_ESISTENTE, percorso,
                "Senza un nuovo file il percorso deve rimanere invariato");
    }
}