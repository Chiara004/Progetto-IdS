package entity;

import control.SessionManager;
import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CorsoTest {

    private GestorePersistenza gestorePersistenza;
    private Corso corso;
    private Docente docente;

    // Dati fissi del corso di test
    private static final int    CODICE_CORSO  = 9001;
    private static final String TITOLO_CORSO  = "Ingegneria del Software";
    private static final String DESCR_CORSO   = "Corso di test";

    // Dati fissi del docente di test
    private static final String MATRICOLA_DOCENTE = "DOC_TEST1";

    // Dati fissi del materiale pre-esistente
    private static final String TITOLO_MAT_ESISTENTE = "Slide Introduttive";

    // Dati fissi della sezione pre-esistente
    private static final String TITOLO_SEZIONE = "Modulo 1";

    @BeforeEach
    void setUp() {
        gestorePersistenza = new GestorePersistenza();

        // Crea e salva il docente proprietario del corso
        docente = new Docente(MATRICOLA_DOCENTE, "Anna", "Bianchi",
                "a.bianchi@unina.it", "pwd", "Docente");
        gestorePersistenza.salva(docente);

        // Crea il corso e associa il docente
        corso = new Corso(CODICE_CORSO, TITOLO_CORSO, DESCR_CORSO, "2024/2025");
        corso.setDocente(docente);

        // Aggiunge una sezione al corso
        Sezione sezione = new Sezione(TITOLO_SEZIONE);
        corso.getSezioni().add(sezione);

        // Aggiunge un materiale pre-esistente (per i test di omonimia e modifica)
        corso.inserisciMateriale(
                TITOLO_MAT_ESISTENTE,
                "Descrizione iniziale",
                "PUBBLICATO",
                "/files/slide_intro.pdf",
                "null",
                "SLIDE"
        );

        gestorePersistenza.salva(corso);
    }

    @AfterEach
    void tearDown() {
        SessionManager.getInstance().logout();
        gestorePersistenza.elimina(Corso.class, CODICE_CORSO);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE);
    }

    //INSERIMENTO MATERIALE VALIDO SENZA SEZIONE
    @Test
    void testInserisciMateriale_DatiValidi_SenzaSezione() {
        boolean esito = corso.inserisciMateriale(
                "Dispensa Capitolo 1",
                "Prima dispensa",
                "PUBBLICATO",
                "/files/disp1.pdf",
                "null",
                "DISPENSE"
        );
        assertTrue(esito, "L'inserimento con dati validi deve restituire true");
        assertNotNull(corso.getMaterialeDidatticoPerTitolo("Dispensa Capitolo 1"),
                "Il materiale deve essere presente nel corso dopo l'inserimento");
    }

    //INSERIMENTO MATERIALE VALIDO CON SEZIONE
    @Test
    void testInserisciMateriale_DatiValidi_ConSezione() {
        boolean esito = corso.inserisciMateriale(
                "Esercizi Modulo 1",
                "Esercizi della prima sezione",
                "PUBBLICATO",
                "/files/esercizi1.pdf",
                TITOLO_SEZIONE,
                "ESERCIZI"
        );
        MaterialeDidattico inserito = corso.getMaterialeDidatticoPerTitolo("Esercizi Modulo 1");

        assertTrue(esito, "L'inserimento con sezione valida deve restituire true");
        assertNotNull(inserito, "Il materiale deve essere nel corso");
        assertNotNull(inserito.getSezione(), "Il materiale deve avere la sezione assegnata");
        assertEquals(TITOLO_SEZIONE, inserito.getSezione().getTitolo(),
                "La sezione assegnata deve corrispondere a quella richiesta");
    }

    //INSERIMENTO MATERIALE INVALIDO PERCHE TITOLO DUPLICATO
    @Test
    void testInserisciMateriale_TitoloOmonimo_StessoCaso() {
        boolean esito = corso.inserisciMateriale(
                TITOLO_MAT_ESISTENTE,
                "Altra descrizione",
                "PUBBLICATO",
                "/files/altro.pdf",
                "null",
                "SLIDE"
        );

        assertFalse(esito, "Un titolo già esistente (stesso case) deve essere rifiutato");
    }

    //INSERIMENTO INVALIDO PERCHE TITOLO NON VALIDO (OMINIMO CON CASE DIVERSO
    @Test
    void testInserisciMateriale_TitoloOmonimo_CaseDiverso() {
        boolean esito = corso.inserisciMateriale(
                TITOLO_MAT_ESISTENTE.toLowerCase(),
                "Altra descrizione",
                "PUBBLICATO",
                "/files/altro.pdf",
                "null",
                "SLIDE"
        );
        assertFalse(esito, "Un titolo omonimo (case diverso) deve essere rifiutato per equalsIgnoreCase");
    }

    //INSERIMENTO VALIDO DOPO TITOLO NON VALIDO
    @Test
    void testInserisciMateriale_TitoloNuovoDopoOmonimiaRifiutata() {
        corso.inserisciMateriale(TITOLO_MAT_ESISTENTE, "desc", "PUBBLICO", "/f.pdf", "null", "SLIDE");
        boolean esito = corso.inserisciMateriale(
                "Titolo Completamente Nuovo",
                "desc",
                "PUBBLICATO",
                "/files/nuovo.pdf",
                "null",
                "SLIDE"
        );

        assertTrue(esito, "Dopo un rifiuto per omonimia, un titolo diverso deve essere accettato");
    }

    //TEST MODIFICA MATERIALE VALIDO
    @Test
    void testModificaMateriale_DatiValidi() {
        MaterialeDidattico esistente = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(esistente, "Il materiale di setup deve esistere");
        String idStr = String.valueOf(esistente.getIdMateriale());

        boolean esito = corso.modificaMateriale(
                idStr,
                "Slide Aggiornate",
                "Nuova descrizione",
                "NON_PUBBLICATO",
                "/files/slide_v2.pdf",
                "null",
                "SLIDE"
        );

        assertTrue(esito, "La modifica con dati validi deve restituire true");
        MaterialeDidattico aggiornato = corso.getMaterialeDidatticoPerId(esistente.getIdMateriale());
        assertEquals("Slide Aggiornate", aggiornato.getTitolo(),
                "Il titolo deve essere aggiornato");
        assertEquals("Nuova descrizione", aggiornato.getDescrizione(),
                "La descrizione deve essere aggiornata");
    }

    //TEST MODIFICA MATERIALE INVALIDO PERCHE TITOLO DUPLICATO
    @Test
    void testModificaMateriale_TitoloOmonimoAltroMateriale() {
        corso.inserisciMateriale("Secondo Materiale", "desc", "PUBBLICATO", "/f2.pdf", "null", "SLIDE");
        MaterialeDidattico secondo = corso.getMaterialeDidatticoPerTitolo("Secondo Materiale");
        assertNotNull(secondo);
        String idStr = String.valueOf(secondo.getIdMateriale());

        boolean esito = corso.modificaMateriale(
                idStr,
                TITOLO_MAT_ESISTENTE,
                "desc",
                "PUBBLICATO",
                "/f2.pdf",
                "null",
                "SLIDE"
        );

        assertFalse(esito, "Non si può rinominare un materiale con un titolo già usato da un altro");
    }

    //TEST MODIFICA MATERIALE VALIDO (NON CAMBIARE IL TITOLO NON E' OMINIMIA)
    @Test
    void testModificaMateriale_StessoTitoloStessoMateriale() {
        MaterialeDidattico esistente = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(esistente);
        String idStr = String.valueOf(esistente.getIdMateriale());

        boolean esito = corso.modificaMateriale(
                idStr,
                TITOLO_MAT_ESISTENTE,   // stesso titolo — non è omonimia con un ALTRO materiale
                "Descrizione aggiornata",
                "PUBBLICATO",
                "/files/slide_intro_v2.pdf",
                "null",
                "SLIDE"
        );

        assertTrue(esito, "Mantenere lo stesso titolo sullo stesso materiale non è omonimia e deve passare");
    }

    //TEST MODIFICA MATERIALE CON SEZIONE VALIDO
    @Test
    void testModificaMateriale_ConSezione() {
        // Arrange
        MaterialeDidattico esistente = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(esistente);
        String idStr = String.valueOf(esistente.getIdMateriale());

        // Act: assegno la sezione durante la modifica
        boolean esito = corso.modificaMateriale(
                idStr,
                TITOLO_MAT_ESISTENTE,
                "desc",
                "PUBBLICATO",
                "/files/slide_intro.pdf",
                TITOLO_SEZIONE,
                "SLIDE"
        );
        MaterialeDidattico aggiornato = corso.getMaterialeDidatticoPerId(esistente.getIdMateriale());

        // Assert
        assertTrue(esito, "La modifica con sezione valida deve passare");
        assertNotNull(aggiornato.getSezione(), "Il materiale deve avere la sezione dopo la modifica");
        assertEquals(TITOLO_SEZIONE, aggiornato.getSezione().getTitolo());
    }

    //TEST RIMOZIONE MATERIALE VALIDO
    @Test
    void testRimuoviMateriale_EsistenteVienRimosso() {
        // Arrange
        MaterialeDidattico esistente = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(esistente, "Il materiale deve esistere prima della rimozione");

        // Act
        corso.rimuoviMateriale(esistente);

        // Assert
        assertNull(corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE),
                "Il materiale deve essere assente dopo la rimozione");
    }

    //TEST RIMOZIONE MATERIALE
    @Test
    void testRimuoviMateriale_AltriMaterialiNonInfluenzati() {
        // Arrange: inserisco un secondo materiale, poi rimuovo solo il primo
        corso.inserisciMateriale("Materiale Da Tenere", "desc", "PUBBLICATO", "/f.pdf", "null", "SLIDE");
        MaterialeDidattico daTenere  = corso.getMaterialeDidatticoPerTitolo("Materiale Da Tenere");
        MaterialeDidattico daRimuovere = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);

        // Act
        corso.rimuoviMateriale(daRimuovere);

        // Assert
        assertNotNull(corso.getMaterialeDidatticoPerTitolo("Materiale Da Tenere"),
                "Gli altri materiali non devono essere rimossi");
        assertNull(corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE),
                "Solo il materiale rimosso deve sparire");
    }

    //TEST TROVA MATERIALE PER TITOLO
    @Test
    void testGetMaterialePerTitolo_Trovato() {
        // Act
        MaterialeDidattico trovato = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);

        // Assert
        assertNotNull(trovato, "Deve trovare il materiale con il titolo esatto");
        assertEquals(TITOLO_MAT_ESISTENTE, trovato.getTitolo());
    }

    //TEST TROVA MATERIALE PER TITOLO NON TROVATO
    @Test
    void testGetMaterialePerTitolo_NonTrovato() {
        // Act
        MaterialeDidattico trovato = corso.getMaterialeDidatticoPerTitolo("Titolo Inesistente");

        // Assert
        assertNull(trovato, "Deve restituire null se nessun materiale ha quel titolo");
    }

    //TEST TROVA MATERIALE PER TITOLO CON CASE DIVERSO
    @Test
    void testGetMaterialePerTitolo_CaseSensitive() {
        // getMaterialeDidatticoPerTitolo usa equals (case-sensitive), a differenza di inserisciMateriale
        // Act
        MaterialeDidattico trovato = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE.toLowerCase());

        // Assert
        assertNull(trovato, "La ricerca per titolo è case-sensitive: il lowercase non deve trovare il materiale");
    }

    //TEST TROVA MATERIALE PER ID
    @Test
    void testGetMaterialePerId_Trovato() {
        // Arrange
        MaterialeDidattico esistente = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        assertNotNull(esistente);
        int id = esistente.getIdMateriale();

        // Act
        MaterialeDidattico trovato = corso.getMaterialeDidatticoPerId(id);

        // Assert
        assertNotNull(trovato, "Deve trovare il materiale con l'id corretto");
        assertEquals(id, trovato.getIdMateriale());
    }

    //TEST TROVA MATERIALE PER ID NON TROVATO
    @Test
    void testGetMaterialePerId_NonTrovato() {
        // Act
        MaterialeDidattico trovato = corso.getMaterialeDidatticoPerId(-999);

        // Assert
        assertNull(trovato, "Deve restituire null per un id inesistente");
    }

    //TEST TROVA SEZIONE PER TITOLO
    @Test
    void testGetSezionePerTitolo_Trovata() {
        // Act
        Sezione trovata = corso.getSezionePerTitolo(TITOLO_SEZIONE);

        // Assert
        assertNotNull(trovata, "Deve trovare la sezione con il titolo corretto");
        assertEquals(TITOLO_SEZIONE, trovata.getTitolo());
    }

    //TEST TROVA SEZIONE PER TITOLO NON TROVATA
    @Test
    void testGetSezionePerTitolo_NonTrovata() {
        // Act
        Sezione trovata = corso.getSezionePerTitolo("Sezione Inesistente");

        // Assert
        assertNull(trovata, "Deve restituire null per una sezione con titolo non presente");
    }

    //TEST TROVA SEZIONE PER TITOLO CON CASE DIVERSO
    @Test
    void testGetSezionePerTitolo_CaseSensitive() {
        // getSezionePerTitolo usa equals, quindi il case conta
        Sezione trovata = corso.getSezionePerTitolo(TITOLO_SEZIONE.toLowerCase());

        assertNull(trovata, "La ricerca sezione è case-sensitive: il lowercase non deve trovare la sezione");
    }

    //TEST EQUALS E HASCODE
    @Test
    void testEqualsEHashCode() {
        Corso c1 = new Corso(100, "Math", "Desc", "2024");
        Corso c2 = new Corso(100, "Storia", "Diversa", "2025");
        Corso c3 = new Corso(200, "Math", "Desc", "2024");

        assertEquals(c1, c2, "Due corsi con lo stesso codice devono essere uguali");
        assertNotEquals(c1, c3, "Due corsi con codice diverso non devono essere uguali");
        assertEquals(c1.hashCode(), c2.hashCode(), "L'hashcode deve essere uguale se i codici sono uguali");
    }
}