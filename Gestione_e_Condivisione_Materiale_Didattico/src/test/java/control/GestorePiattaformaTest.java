package control;

import control.filtro.*;
import database.GestorePersistenza;
import entity.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GestorePiattaformaTest {
    private GestorePersistenza gestorePersistenza;
    private Docente docente;
    private Corso corso;

    private static final String MATRICOLA_DOCENTE = "DOC_PIATT1";
    private static final String EMAIL_DOCENTE = "docente.piattaforma@unina.it";
    private static final int    CODICE_CORSO= 9400;
    private static final String TITOLO_CORSO= "Corso Piattaforma Test";
    private static final String TITOLO_SEZIONE = "Sezione Test";

    @BeforeEach
    void setUpDB() {
        gestorePersistenza = new GestorePersistenza();
        String passwordCriptata = BCrypt.hashpw("password123", BCrypt.gensalt());
        docente = new Docente(MATRICOLA_DOCENTE, "Anna", "Bianchi",
                EMAIL_DOCENTE, passwordCriptata, "Docente");
        gestorePersistenza.salva(docente);

        corso = new Corso(CODICE_CORSO, TITOLO_CORSO, "desc", "2024/2025");
        corso.setDocente(docente);

        Sezione sezione = new Sezione(TITOLO_SEZIONE);
        sezione.setCorso(corso);
        gestorePersistenza.salva(sezione);
        corso.getSezioni().add(sezione);

        // Materiale PUBBLICATO con sezione
        corso.inserisciMateriale("Slide Pubblicate", "desc pub",
                "PUBBLICATO", "/pub.pdf", TITOLO_SEZIONE, "SLIDE");

        // Materiale NON_PUBBLICATO con sezione
        corso.inserisciMateriale("Slide Bozza", "desc bozza",
                "NON_PUBBLICATO", "/bozza.pdf", TITOLO_SEZIONE, "SLIDE");

        // Materiale PUBBLICATO categoria DISPENSE con sezione
        corso.inserisciMateriale("Dispensa Pubblicata", "desc dispensa",
                "PUBBLICATO", "/dispensa.pdf", TITOLO_SEZIONE, "DISPENSE");

        gestorePersistenza.salva(corso);

        SessionManager.getInstance().setUtenteLoggato(docente);
    }

    @AfterEach
    void tearDownDB() {
        gestorePersistenza.elimina(Corso.class,  CODICE_CORSO);
        gestorePersistenza.elimina(Utente.class, MATRICOLA_DOCENTE);
        SessionManager.getInstance().logout();
    }

    //TEST MECCANISMI DI FILTRAGGIO
    @Test
    void testStringToStatoFiltro_Categoria() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("categoria");
        assertInstanceOf(FiltroCategoria.class, filtro,
                "'categoria' deve restituire un FiltroCategoria");
    }

    @Test
    void testStringToStatoFiltro_Descrizione() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("descrizione");
        assertInstanceOf(FiltroDescrizione.class, filtro,
                "'descrizione' deve restituire un FiltroDescrizione");
    }

    @Test
    void testStringToStatoFiltro_Pubblicato() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("pubblicato");
        assertInstanceOf(FiltroPubblicato.class, filtro,
                "'pubblicato' deve restituire un FiltroPubblicato");
    }

    @Test
    void testStringToStatoFiltro_Titolo() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("titolo");
        assertInstanceOf(FiltroTitolo.class, filtro,
                "'titolo' deve restituire un FiltroTitolo");
    }

    @Test
    void testStringToStatoFiltro_ValoreNonRiconosciuto_FiltroNullo() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("valore_sconosciuto");
        assertInstanceOf(FiltroNullo.class, filtro,
                "Un valore non riconosciuto deve restituire un FiltroNullo");
    }

    @Test
    void testStringToStatoFiltro_Null_FiltroNullo() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro(null);
        assertInstanceOf(FiltroNullo.class, filtro,
                "null deve restituire un FiltroNullo senza lanciare eccezioni");
    }

    @Test
    void testStringToStatoFiltro_StringaVuota_FiltroNullo() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("");
        assertInstanceOf(FiltroNullo.class, filtro,
                "Una stringa vuota deve restituire un FiltroNullo");
    }

    @Test
    void testStringToStatoFiltro_StringaConSoliSpazi_FiltroNullo() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("   ");
        assertInstanceOf(FiltroNullo.class, filtro,
                "Una stringa di soli spazi deve restituire un FiltroNullo");
    }

    @Test
    void testStringToStatoFiltro_CaseMaiuscole_Accettato() {
        // toLowerCase nella switch garantisce che il case non conti
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("CATEGORIA");
        assertInstanceOf(FiltroCategoria.class, filtro,
                "Il confronto deve essere case-insensitive: 'CATEGORIA' deve dare FiltroCategoria");
    }

    @Test
    void testStringToStatoFiltro_CaseMisto_Accettato() {
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("Titolo");
        assertInstanceOf(FiltroTitolo.class, filtro,
                "Il confronto deve essere case-insensitive: 'Titolo' deve dare FiltroTitolo");
    }

    @Test
    void testStringToStatoFiltro_ConSpaziBordo_Accettato() {
        // trim() nella switch garantisce che gli spazi laterali non contino
        StrategyFiltro filtro = GestorePiattaforma.StringToStatoFiltro("  titolo  ");
        assertInstanceOf(FiltroTitolo.class, filtro,
                "Gli spazi iniziali/finali devono essere ignorati grazie al trim()");
    }

    private MaterialeDidattico creaMateriale(String titolo, String descrizione,
                                             Categoria categoria, Visibilita visibilita,
                                             String titoloSezione) {
        Sezione sezione = new Sezione(titoloSezione);
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, descrizione, LocalDate.of(2024, 1, 1),
                "/files/test.pdf", categoria, visibilita);
        m.setSezione(sezione);
        return m;
    }

    @Test
    void testMaterialeInRighe_SetVuoto_ListaVuota() {
        List<String[]> righe = GestorePiattaforma.materialeInRighe(new HashSet<>());

        assertNotNull(righe, "Il risultato non deve essere null");
        assertTrue(righe.isEmpty(), "Un set vuoto deve produrre una lista vuota");
    }

    @Test
    void testMaterialeInRighe_UnMateriale_UnaRiga() {
        Set<MaterialeDidattico> set = new HashSet<>();
        set.add(creaMateriale("Slide 1", "desc", Categoria.SLIDE, Visibilita.PUBBLICATO, "Modulo 1"));

        List<String[]> righe = GestorePiattaforma.materialeInRighe(set);

        assertEquals(1, righe.size(), "Un set con un elemento deve produrre una lista con una riga");
    }

    @Test
    void testMaterialeInRighe_PiuMateriali_RigheCorrispondenti() {
        Set<MaterialeDidattico> set = new HashSet<>();
        set.add(creaMateriale("Slide 1", "desc1", Categoria.SLIDE,    Visibilita.PUBBLICATO, "Modulo 1"));
        set.add(creaMateriale("Slide 2", "desc2", Categoria.DISPENSE, Visibilita.NON_PUBBLICATO,      "Modulo 2"));

        List<String[]> righe = GestorePiattaforma.materialeInRighe(set);

        assertEquals(2, righe.size(), "Un set con due elementi deve produrre due righe");
    }

    @Test
    void testMaterialeInRighe_Docente_SetteColonneEContenutoCorretto() {
        Set<MaterialeDidattico> set = new HashSet<>();
        set.add(creaMateriale("Slide Intro", "Prima slide", Categoria.SLIDE, Visibilita.PUBBLICATO, "Modulo 1"));
        String[] riga = GestorePiattaforma.materialeInRighe(set).get(0);
        assertEquals(7, riga.length, "Per il docente la riga deve avere esattamente 7 colonne");
        assertEquals("Slide Intro", riga[0], "Colonna 0: titolo");
        assertEquals(Categoria.SLIDE.toString(), riga[1], "Colonna 1: categoria");
        assertEquals("Prima slide", riga[2], "Colonna 2: descrizione");
        assertEquals("2024-01-01", riga[3], "Colonna 3: dataPubblicazione");
        assertEquals("Modulo 1", riga[4], "Colonna 4: sezione");
        assertEquals(Visibilita.PUBBLICATO.toString(), riga[5], "Colonna 5: visibilità (SOLO DOCENTE)");
        assertEquals("⋮", riga[6], "Colonna 6: azioni");
    }

    @Test
    void testMaterialeInRighe_Studente_SeiColonneEContenutoCorretto() {
        Studente studente = new Studente("N46001234", "Mario", "Rossi", "mario@studenti.unina.it", "pwd", "Studente");
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente);

        Set<MaterialeDidattico> set = new HashSet<>();
        set.add(creaMateriale("Dispensa 1", "Descrizione studio", Categoria.DISPENSE, Visibilita.PUBBLICATO, "Modulo 2"));

        String[] riga = GestorePiattaforma.materialeInRighe(set).get(0);

        assertEquals(6, riga.length, "Per lo studente la riga deve avere esattamente 6 colonne (senza visibilità)");
        assertEquals("Dispensa 1", riga[0], "Colonna 0: titolo");
        assertEquals(Categoria.DISPENSE.toString(), riga[1], "Colonna 1: categoria");
        assertEquals("Descrizione studio", riga[2], "Colonna 2: descrizione");
        assertEquals("2024-01-01", riga[3], "Colonna 3: dataPubblicazione");
        assertEquals("Modulo 2", riga[4], "Colonna 4: sezione");
        assertEquals("⋮", riga[5], "Colonna 5: azioni");

        // Ripristiniamo la sessione con il docente per non sporcare eventuali test successivi
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(docente);
    }

    //VisualizzaMateriali senza filtro
    @Test
    void testVisualizzaMateriali_SenzaFiltro_RestituisceTutti() {
        List<String[]> righe = GestorePiattaforma.visualizzaMateriali(TITOLO_CORSO);

        assertEquals(3, righe.size(),
                "Senza filtro devono essere restituiti tutti e 3 i materiali");
    }

    //VisualizzaMateriali con filtro titolo
    @Test
    void testVisualizzaMateriali_FiltroTitolo_TrovaCorrispondente() {
        List<String[]> righe = GestorePiattaforma.visualizzaMateriali(
                TITOLO_CORSO, "Slide Pubblicate", "titolo");

        assertEquals(1, righe.size(),
                "Il filtro per titolo esatto deve restituire esattamente un risultato");
        assertEquals("Slide Pubblicate", righe.get(0)[0]);
    }

    //VISUALIZZAZIONE MATERIALI CON FILTRO TITOLO SENZA CORRISPONDENZE
    @Test
    void testVisualizzaMateriali_FiltroTitolo_NessunCorrispondente() {
        List<String[]> righe = GestorePiattaforma.visualizzaMateriali(
                TITOLO_CORSO, "Titolo Inesistente", "titolo");

        assertTrue(righe.isEmpty(),
                "Il filtro per titolo inesistente deve restituire una lista vuota");
    }

    //VisualizzaMateriali con filtro categoria
    @Test
    void testVisualizzaMateriali_FiltroCategoria_SoloDispense() {
        List<String[]> righe = GestorePiattaforma.visualizzaMateriali(
                TITOLO_CORSO, Categoria.DISPENSE, "categoria");

        assertEquals(1, righe.size(),
                "Il filtro per categoria DISPENSE deve restituire solo la dispensa");
        assertEquals("Dispensa Pubblicata", righe.get(0)[0]);
    }

    // VisualizzaMateriali con filtro nullo/null
    @Test
    void testVisualizzaMateriali_FiltroNull_RestituisceTutti() {
        List<String[]> righe = GestorePiattaforma.visualizzaMateriali(
                TITOLO_CORSO, null, null);

        assertEquals(3, righe.size(),
                "Un filtro null deve restituire tutti i materiali");
    }


    @Test
    void testVisualizzaMaterialiPubblicati_BozzaNonRestituita() {
        List<String[]> righe = GestorePiattaforma.visualizzaMaterialiPubblicati(TITOLO_CORSO);

        boolean bozzaPresente = false;

        for (String[] r : righe) {
            if (r[0].equals("Slide Bozza")) {
                bozzaPresente = true;
                break; // Interrompe il ciclo appena trova la prima occorrenza
            }
        }
        assertFalse(bozzaPresente,
                "Un materiale in BOZZA non deve mai comparire tra i pubblicati");
    }

    // VisualizzaMaterialiPubblicati con filtro categoria
    @Test
    void testVisualizzaMaterialiPubblicati_FiltroCategoria_SoloDispensPubblicate() {
        List<String[]> righe = GestorePiattaforma.visualizzaMaterialiPubblicati(
                TITOLO_CORSO, Categoria.DISPENSE, "categoria");

        assertEquals(1, righe.size(),
                "Filtro categoria DISPENSE + pubblicato deve restituire solo la dispensa pubblicata");
        assertEquals("Dispensa Pubblicata", righe.get(0)[0]);
    }

    @Test
    void testVisualizzaMaterialiPubblicati_FiltroCategoria_NessunaBozzaPassaIlDoppioFiltro() {
        List<String[]> righe = GestorePiattaforma.visualizzaMaterialiPubblicati(
                TITOLO_CORSO, Categoria.SLIDE, "categoria");

        boolean bozzaPresente = false;

        for (String[] r : righe) {
            if (r[0].equals("Slide Bozza")) {
                bozzaPresente = true;
                break; // Interrompe il ciclo appena trova la prima occorrenza
            }
        };
        assertFalse(bozzaPresente,
                "Una bozza non deve passare il doppio filtro categoria + pubblicato");
    }
}