package entity;

import control.SessionManager;
import database.GestorePersistenza;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class GestoreCorsoTest {

    private GestorePersistenza gestorePersistenza;
    private GestoreCorso       gestoreCorso;

    // Docente loggato
    private static final String MATRICOLA_DOCENTE  = "DOC_COR01";
    private static final String EMAIL_DOCENTE      = "docente.corso@unina.it";

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
    private static final String TITOLO_MAT_ESISTENTE   = "Slide Introduttive";
    private static final String NOME_FILE_MAT_ESISTENTE = "slide_intro_esistente.pdf";

    // Cartella temporanea usata da GestoreFile
    private static final String UPLOAD_DIR_TEST = "target/test-uploads";

    private Docente docente;
    private Docente docente2;
    private Corso corso;
    private Corso corso2;

    // File temporaneo su disco
    private File fileDiTest;

    @BeforeEach
    void setUp() throws IOException {
        gestorePersistenza = new GestorePersistenza();

        // Crea la cartella di upload per i test se non esiste
        Files.createDirectories(Paths.get(UPLOAD_DIR_TEST));

        // Crea un file temporaneo reale nel tmp di sistema (usato come "file scelto" nei test)
        fileDiTest = File.createTempFile("materiale_test_", ".pdf",
                new File(System.getProperty("java.io.tmpdir")));
        Files.writeString(fileDiTest.toPath(), "contenuto di test");

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
        gestorePersistenza.salva(corso);

        Sezione sezione = new Sezione(TITOLO_SEZIONE);
        sezione.setCorso(corso);
        gestorePersistenza.salva(sezione);
        corso.getSezioni().add(sezione);

        // Copia il file del materiale pre-esistente direttamente nella upload dir
        Path destinazioneEsistente = Paths.get(UPLOAD_DIR_TEST, NOME_FILE_MAT_ESISTENTE);
        Files.copy(fileDiTest.toPath(), destinazioneEsistente,
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        corso.inserisciMateriale(
                TITOLO_MAT_ESISTENTE,
                "Descrizione iniziale",
                "PUBBLICATO",
                NOME_FILE_MAT_ESISTENTE,
                "null",
                "SLIDE"
        );
        MaterialeDidattico materialeCreato = corso.getMaterialeDidatticoPerTitolo(TITOLO_MAT_ESISTENTE);
        gestorePersistenza.salva(materialeCreato);
        gestorePersistenza.aggiorna(corso);

        // Crea un corso omonimo intestato al secondo docente (per test isolamento)
        corso2 = new Corso(CODICE_CORSO2, TITOLO_CORSO, "Corso del secondo docente", "2024/2025");
        corso2.setDocente(docente2);
        gestorePersistenza.salva(corso2);

        // Imposta il docente loggato PRIMA di istanziare GestoreCorso
        SessionManager.getInstance().setUtenteLoggato(docente);
        gestoreCorso = new GestoreCorso();
    }

    @AfterEach
    void tearDown() throws IOException {
        // Elimina tutti i file rimasti nella upload dir di test
        File uploadDir = new File(UPLOAD_DIR_TEST);
        if (uploadDir.exists()) {
            for (File f : uploadDir.listFiles()) {
                f.delete();
            }
        }

        // Elimina il file temporaneo di sistema
        if (fileDiTest != null && fileDiTest.exists()) {
            fileDiTest.delete();
        }

        // Pulizia DB
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

        boolean trovato = false;
        for (MaterialeDidattico m : materiali) {
            if (TITOLO_MAT_ESISTENTE.equals(m.getTitolo())) {
                trovato = true;
                break;
            }
        }
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
        boolean trovata = false;
        for (Sezione s : sezioni) {
            if (TITOLO_SEZIONE.equals(s.getTitolo())) {
                trovata = true;
                break;
            }
        }
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
    void testGetPercorsoFile_RestituisceIlNomeFileCorretto() {
        String percorso = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        assertEquals(NOME_FILE_MAT_ESISTENTE, percorso,
                "Il percorso restituito deve corrispondere al nome del file salvato");
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

    //TEST INSERIMENTO NUOVO MATERIALE
    @Test
    void testInserisciMateriale_TitoloNuovo_ReturnTrue() {
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                "Nuovo Materiale",
                "Descrizione nuova",
                "DISPENSE",
                "PUBBLICATO",
                fileDiTest,
                "null"
        );

        assertTrue(esito, "L'inserimento con titolo nuovo deve restituire true");
    }

    //TEST INSERIMENTO MATERIALE CON FILE DI TEST
    @Test
    void testInserisciMateriale_FileFisicoCopiato() {
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Materiale Con File", "desc",
                "DISPENSE", "PUBBLICATO", fileDiTest, "null"
        );

        String percorso = gestoreCorso.getPercorsoFile(TITOLO_CORSO, "Materiale Con File");
        Path fileCopiato = Paths.get(UPLOAD_DIR_TEST, percorso);
        assertTrue(Files.exists(fileCopiato),
                "Il file fisico deve essere presente nella upload dir dopo l'inserimento");
    }

    //TEST INSERIMENTO MATERIALE NON VALIDO TITOLO OMONIMO
    @Test
    void testInserisciMateriale_TitoloOmonimo_ReturnFalse() {
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                TITOLO_MAT_ESISTENTE,   // omonimo
                "Altra descrizione",
                "SLIDE", "PUBBLICATO",
                fileDiTest, "null"
        );

        assertFalse(esito, "L'inserimento con titolo già esistente deve restituire false");
    }

    //TEST INSERIMENTO MATERIALE NON VALIDO TITOLO OMONIMO CASE DIVERSO
    @Test
    void testInserisciMateriale_TitoloOmonimoCaseDiverso_ReturnFalse() {
        boolean esito = gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                TITOLO_MAT_ESISTENTE.toLowerCase(),
                "Altra descrizione",
                "SLIDE", "PUBBLICATO",
                fileDiTest, "null"
        );

        assertFalse(esito, "L'inserimento con titolo omonimo (case diverso) deve restituire false");
    }

    //TEST INSERIMENTO MATERIALE (DEVE RISULTARE PRESENTE NEL CORSO)
    @Test
    void testInserisciMateriale_MaterialeVisibileDopoInserimento() {
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Materiale Verificato", "desc",
                "DISPENSE", "PUBBLICATO", fileDiTest, "null"
        );

        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean trovato = false;
        for (MaterialeDidattico m : materiali) {
            if ("Materiale Verificato".equals(m.getTitolo())) {
                trovato = true;
                break; // Interrompe il ciclo appena trova il materiale
            }
        }
        assertTrue(trovato, "Il materiale inserito deve essere presente nel corso dopo l'inserimento");
    }

    //TEST RIMOZIONE MATERIALE VALIDA
    @Test
    void testRimuoviMateriale_TitoloEsistente_ReturnTrue() {
        boolean esito = gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertTrue(esito, "La rimozione di un materiale esistente deve restituire true");
    }

    //TEST RIMOZIONE MATERIALE TITOLO NON PRESENTE
    @Test
    void testRimuoviMateriale_TitoloInesistente_ReturnFalse() {
        boolean esito = gestoreCorso.rimuoviMateriale(TITOLO_CORSO, "Titolo Inesistente");
        assertFalse(esito, "La rimozione di un materiale inesistente deve restituire false");
    }

    //TEST RIMOZIONE FILE FISICO
    @Test
    void testRimuoviMateriale_FileFisicoEliminato() {
        Path fileEsistente = Paths.get(UPLOAD_DIR_TEST, NOME_FILE_MAT_ESISTENTE);
        assertTrue(Files.exists(fileEsistente),
                "Il file fisico deve esistere prima della rimozione");
        gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertFalse(Files.exists(fileEsistente),
                "Il file fisico deve essere eliminato dalla upload dir dopo la rimozione");
    }
    //TEST RIMOZIONE FILE IN UNA SEZIONE
    @Test
    void testRimuoviMateriale_MaterialeInSezione_SezioneScollegate() throws IOException {
        File fileConSezione = File.createTempFile("materiale_sezione_", ".pdf",
                new File(System.getProperty("java.io.tmpdir")));
        Files.writeString(fileConSezione.toPath(), "contenuto con sezione");

        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO,
                "Materiale In Sezione",
                "desc",
                "DISPENSE",
                "PUBBLICATO",
                fileConSezione,
                TITOLO_SEZIONE
        );

        // Verifica che la sezione sia stata assegnata correttamente prima della rimozione
        Corso corsoDalDB = gestorePersistenza.cercaPrimoPerCampi(
                Corso.class, Map.of("titolo", TITOLO_CORSO, "docente", docente));
        MaterialeDidattico materialeConSezione = corsoDalDB.getMaterialeDidatticoPerTitolo("Materiale In Sezione");
        assertNotNull(materialeConSezione.getSezione(),
                "Il materiale deve avere la sezione assegnata prima della rimozione");

        boolean esito = gestoreCorso.rimuoviMateriale(TITOLO_CORSO, "Materiale In Sezione");

        //la rimozione deve avere successo
        assertTrue(esito, "La rimozione di un materiale con sezione deve restituire true");

        //il materiale non deve più essere presente nel corso
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean ancoraPresente = false;
        for (MaterialeDidattico m : materiali) {
            if ("Materiale In Sezione".equals(m.getTitolo())) {
                ancoraPresente = true;
                break;
            }
        }
        assertFalse(ancoraPresente, "Il materiale rimosso non deve essere più presente nel corso");

        //il file fisico deve essere stato eliminato
        Path fileCopiato = Paths.get(UPLOAD_DIR_TEST, fileConSezione.getName());
        assertFalse(Files.exists(fileCopiato),
                "Il file fisico deve essere eliminato dalla upload dir");

        // Assert 4: la sezione deve ancora esistere nel corso (non deve essere stata eliminata)
        Set<Sezione> sezioni = gestoreCorso.getSezioni(TITOLO_CORSO);
        boolean sezioneAncoraPresente = false;
        for (Sezione s : sezioni) {
            if (TITOLO_SEZIONE.equals(s.getTitolo())) {
                sezioneAncoraPresente = true;
                break;
            }
        }
        assertTrue(sezioneAncoraPresente,
                "La sezione deve sopravvivere alla rimozione del materiale che conteneva");

        // Cleanup extra
        fileConSezione.delete();
    }

    //TEST MATERIALE NON DEVE ESSERE PIU' PRESENTE
    @Test
    void testRimuoviMateriale_MaterialeNonPiuRecuperabile() {
        gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean ancoraPresente = false;
        for (MaterialeDidattico m : materiali) {
            if (TITOLO_MAT_ESISTENTE.equals(m.getTitolo())) {
                ancoraPresente = true;
                break;
            }
        }
        assertFalse(ancoraPresente, "Il materiale rimosso non deve essere più presente nel corso");
    }

    //TEST NON INFLUENZA SU ALTRI MATERIALI
    @Test
    void testRimuoviMateriale_AltriMaterialiNonInfluenzati() throws IOException {
        //crea un secondo file fisico per il secondo materiale
        File secondoFile = File.createTempFile("secondo_materiale_", ".pdf",
                new File(System.getProperty("java.io.tmpdir")));
        Files.writeString(secondoFile.toPath(), "secondo contenuto");
        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Materiale Da Tenere", "desc",
                "DISPENSE", "PUBBLICATO", secondoFile, "null"
        );
        gestoreCorso.rimuoviMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        Set<MaterialeDidattico> materiali = gestoreCorso.recuperaMateriali(TITOLO_CORSO);
        boolean tenutoPresente = false;
        for (MaterialeDidattico m : materiali) {
            if ("Materiale Da Tenere".equals(m.getTitolo())) {
                tenutoPresente = true;
                break;
            }
        }
        assertTrue(tenutoPresente, "Gli altri materiali non devono essere rimossi");

        // Cleanup extra
        secondoFile.delete();
    }

    //TEST MODIFICA MATERIALE SENZA AGGIUNGERE NUOVO FILE
    @Test
    void testModificaMateriale_SenzaNuovoFile_ReturnTrue() {
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        // file=null → mantiene il percorso corrente, non chiama salvaFile
        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(id),
                TITOLO_MAT_ESISTENTE, "Descrizione aggiornata",
                "SLIDE", "PUBBLICATO",
                null,
                "null"
        );

        assertTrue(esito, "La modifica senza nuovo file deve restituire true");
    }

    //TEST MODIFICA MATERIALE SENZA CAMBIARE IL FILE
    @Test
    void testModificaMateriale_SenzaNuovoFile_PercorsoInvariato() {
        String percorsoOriginale = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(id),
                TITOLO_MAT_ESISTENTE, "desc aggiornata",
                "SLIDE", "PUBBLICATO", null, "null"
        );

        String percorsoDopo = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertEquals(percorsoOriginale, percorsoDopo,
                "Senza nuovo file il percorso deve rimanere invariato");
    }

    //TEST MODIFICA MATERIALE CON NUOVO FILE
    @Test
    void testModificaMateriale_ConNuovoFile_FileFisicoCopiato() throws IOException {
        // Arrange: crea un file diverso da usare come aggiornamento
        File nuovoFile = File.createTempFile("materiale_nuovo_", ".pdf",
                new File(System.getProperty("java.io.tmpdir")));
        Files.writeString(nuovoFile.toPath(), "contenuto aggiornato");

        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(id),
                TITOLO_MAT_ESISTENTE, "desc",
                "SLIDE", "PUBBLICATO", nuovoFile, "null"
        );
        String percorsoFile = gestoreCorso.getPercorsoFile(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        Path nuovoFileCopiato = Paths.get(UPLOAD_DIR_TEST,percorsoFile);
        assertTrue(Files.exists(nuovoFileCopiato),
                "Il nuovo file fisico deve essere copiato nella upload dir dopo la modifica");

        // Cleanup extra
        nuovoFile.delete();
    }


    //TEST MODIFICA INVALIDA
    @Test
    void testModificaMateriale_TitoloOmonimoAltroMateriale_ReturnFalse() throws IOException {
        File secondoFile = File.createTempFile("secondo_", ".pdf",
                new File(System.getProperty("java.io.tmpdir")));
        Files.writeString(secondoFile.toPath(), "secondo contenuto");

        gestoreCorso.inserisciMateriale(
                TITOLO_CORSO, "Secondo Materiale", "desc",
                "SLIDE", "PUBBLICATO", secondoFile, "null"
        );
        int idSecondo = gestoreCorso.getIdMateriale(TITOLO_CORSO, "Secondo Materiale");

        // Act: provo a rinominarlo con il titolo già usato dal primo
        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(idSecondo),
                TITOLO_MAT_ESISTENTE,   // omonimo
                "desc", "SLIDE", "PUBBLICATO", null, "null"
        );

        assertFalse(esito, "Non si può rinominare con un titolo già usato da un altro materiale");

        // Cleanup extra
        secondoFile.delete();
    }

    //TEST MODIFICA MATERIALE STESSO TITOLO E STESSO FILE
    @Test
    void testModificaMateriale_StessoTitoloStessoMateriale_ReturnTrue() {
        int id = gestoreCorso.getIdMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);

        boolean esito = gestoreCorso.modificaMateriale(
                TITOLO_CORSO, String.valueOf(id),
                TITOLO_MAT_ESISTENTE,
                "Descrizione aggiornata",
                "SLIDE", "PUBBLICATO", null, "null"
        );

        assertTrue(esito, "Mantenere il proprio titolo non è omonimia e deve passare");
    }

    // TEST RECUPERA CORSO DOCENTE
    @Test
    void testRecuperaCorso_DocenteLoggato_CorsoTrovato() {
        Corso recuperato = gestoreCorso.recuperaCorso(TITOLO_CORSO);
        assertNotNull(recuperato, "Il corso deve essere recuperato correttamente dal DB per il docente");
        assertEquals(TITOLO_CORSO, recuperato.getTitolo(), "Il titolo del corso recuperato deve coincidere");
    }

    //TEST RECUPERA CORSO NON VALIDO
    @Test
    void testRecuperaCorso_DocenteLoggato_CorsoInesistente() {
        Corso recuperato = gestoreCorso.recuperaCorso("Titolo Assolutamente Inesistente");

        assertNull(recuperato, "Se il corso non esiste, la funzione deve restituire null");
    }

    //TEST RECUPERA CORSO CASO DI OMONIMIA
    @Test
    void testRecuperaCorso_DocenteLoggato_CorsoDiAltroDocente() {
        Corso recuperato = gestoreCorso.recuperaCorso(TITOLO_CORSO);
        // verifichiamo che restituisca il corso con CODICE_CORSO e non CODICE_CORSO2.
        assertEquals(CODICE_CORSO, recuperato.getCodice(), "Deve recuperare il corso del docente loggato, non l'omonimo di altri");
    }

    // TEST RECUPERA CORSO STUDENTE
    @Test
    void testRecuperaCorso_StudenteLoggato_IscrittoAlCorso() {
        String password= BCrypt.hashpw("password123", BCrypt.gensalt());
        String matricolaStudente = "N46007555";
        Studente studente = new Studente(matricolaStudente, "Luca", "Verdi", "luca.verdi@studenti.unina.it", password, "Studente");
        gestorePersistenza.salva(studente);
        studente.getCorsi().add(corso);
        gestorePersistenza.aggiorna(studente);
        corso.getStudenti().add(studente);
        gestorePersistenza.aggiorna(corso);
        // Cambia la sessione per simulare il login dello studente
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente);
        Corso recuperato = gestoreCorso.recuperaCorso(TITOLO_CORSO);
        assertNotNull(recuperato, "Il corso deve essere trovato se lo studente è regolarmente iscritto");
        assertEquals(TITOLO_CORSO, recuperato.getTitolo());
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(docente); // Ripristina docente
        gestorePersistenza.elimina(Utente.class, matricolaStudente);
    }

    //TEST RECUPERA CORSO STUDENTE NON ISCRITTO
    @Test
    void testRecuperaCorso_StudenteLoggato_NonIscrittoAlCorso() {
        String matricolaStudente = "N46008887";
        String password= BCrypt.hashpw("password123", BCrypt.gensalt());
        Studente studente = new Studente(matricolaStudente, "Marco", "Gialli", "marco.gialli@studenti.unina.it", password, "Studente");
        gestorePersistenza.salva(studente);

        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(studente);
        Corso recuperato = gestoreCorso.recuperaCorso(TITOLO_CORSO);
        assertNull(recuperato, "Se lo studente non ha il corso nella sua lista, deve restituire null");
        SessionManager.getInstance().logout();
        SessionManager.getInstance().setUtenteLoggato(docente);
        gestorePersistenza.elimina(Studente.class, matricolaStudente);
    }

    // TEST APRI MATERIALE ESISTENTE
    @Test
    void testApriMateriale_MaterialeEsistente_ReturnTrue() {
        boolean esito = gestoreCorso.apriMateriale(TITOLO_CORSO, TITOLO_MAT_ESISTENTE);
        assertTrue(esito, "L'apertura di un file fisico pre-esistente deve restituire true");
    }

    //TEST APRI MATERIALE NON TROVATO
    @Test
    void testApriMateriale_MaterialeNonTrovato_ReturnFalse() {
        boolean esito = gestoreCorso.apriMateriale(TITOLO_CORSO, "Titolo Materiale Falso");
        assertFalse(esito, "Se il materiale non esiste, apriMateriale deve restituire false");
    }
}
