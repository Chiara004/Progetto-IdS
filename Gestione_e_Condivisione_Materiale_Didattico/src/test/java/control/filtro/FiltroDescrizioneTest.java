package control.filtro;

import entity.Categoria;
import entity.MaterialeDidattico;
import entity.Sezione;
import entity.Visibilita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FiltroDescrizioneTest {

    private FiltroDescrizione filtroDescrizione;
    private Set<MaterialeDidattico> materiali;

    // Descrizioni fisse usate nei test
    private static final String DESC_A = "Introduzione ai concetti base della programmazione";
    private static final String DESC_B = "Algoritmi di ordinamento e ricerca";
    private static final String DESC_C = "Fondamenti delle basi di dati relazionali";

    @BeforeEach
    void setUp() {
        filtroDescrizione = new FiltroDescrizione();
        materiali = new HashSet<>();
        materiali.add(creaMateriale("Materiale A", DESC_A));
        materiali.add(creaMateriale("Materiale B", DESC_B));
        materiali.add(creaMateriale("Materiale C", DESC_C));

    }

    private MaterialeDidattico creaMateriale(String titolo, String descrizione) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, descrizione, LocalDate.now(), "/file.pdf",
                Categoria.SLIDE, Visibilita.PUBBLICATO);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }


    //TEST FILTRO PAROLA CHIAVE
    @Test
    void testFiltra_ParolaChiaveEsatta_TrovaCorrispondente() {
        // "programmazione" è presente solo in DESC_A
        filtroDescrizione.filtra(materiali, "programmazione");

        assertEquals(1, materiali.size(),
                "La ricerca per 'programmazione' deve restituire esattamente un materiale");
        assertEquals("Materiale A", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CON PIU CORRISPONDENTI
    @Test
    void testFiltra_ParolaChiaveCondivisa_TrovaPiuCorrispondenti() {
        filtroDescrizione.filtra(materiali, "dati");

        assertEquals(1, materiali.size(),
                "La ricerca per 'dati' deve restituire iL materiali che la contiene");
        assertTrue(materiali.stream().anyMatch(m -> m.getTitolo().equals("Materiale C")));
    }

    //TEST FILTRO CON PAROLA NON PRESENTE
    @Test
    void testFiltra_ParolaChiaveNonPresente_SetVuoto() {
        filtroDescrizione.filtra(materiali, "quantistica");

        assertTrue(materiali.isEmpty(),
                "Nessun materiale deve sopravvivere se la parola chiave non è in nessuna descrizione");
    }

    //TEST FILTRO CON PAROLA PARZIALE
    @Test
    void testFiltra_ParolaChiaveParziale_TrovaCorrispondente() {
        // "algori" è sottostringa di "Algoritmi"
        filtroDescrizione.filtra(materiali, "algori");

        assertEquals(1, materiali.size(),
                "La ricerca parziale 'algori' deve trovare il materiale con 'Algoritmi' nella descrizione");
        assertEquals("Materiale B", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CASE SENSITIVE
    @Test
    void testFiltra_QueryTuttaMaiuscole_TrovaLostesso() {
        filtroDescrizione.filtra(materiali, "PROGRAMMAZIONE");

        assertEquals(1, materiali.size(),
                "La ricerca in maiuscolo deve trovare lo stesso risultato del minuscolo");
        assertEquals("Materiale A", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CASE SENSITIVE MISTO
    @Test
    void testFiltra_QueryCaseMisto_TrovaLostesso() {
        filtroDescrizione.filtra(materiali, "AlGoRiTmI");

        assertEquals(1, materiali.size(),
                "La ricerca con case misto deve trovare lo stesso risultato");
        assertEquals("Materiale B", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CASE SENSITIVE
    @Test
    void testFiltra_DescrizioneConMaiuscole_TrovataConQueryMinuscola() {
        filtroDescrizione.filtra(materiali, "fondamenti");

        assertEquals(1, materiali.size(),
                "Una query minuscola deve trovare una descrizione con maiuscole");
        assertEquals("Materiale C", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO PAROLA PRESENTE SOLO NEL TITOLO E NON SULLA DESCRIZIONE
    @Test
    void testFiltra_ParolaPresente_SoloNelTitolo_NonNellaDescrizione_RimossoDalFiltro() {
        // Arrange: materiale con "Speciale" nel titolo ma NON nella descrizione
        MaterialeDidattico materialeConTitoloSpeciale = creaMateriale(
                "Titolo Speciale", "descrizione generica senza corrispondenza");
        materiali.add(materialeConTitoloSpeciale);

        // Act: cerco "Speciale" — presente nel titolo ma non nella descrizione
        filtroDescrizione.filtra(materiali, "Speciale");

        // Assert: il materiale deve essere rimosso perché il filtro agisce sulla descrizione
        assertFalse(materiali.stream().anyMatch(m -> m.getTitolo().equals("Titolo Speciale")),
                "Il FiltroDescrizione deve filtrare sulla descrizione, non sul titolo");
    }

    //TEST FILTRO MATERIALE CON DESCRIZIONE NULL
    @Test
    void testFiltra_MaterialeConDescrizioneNull_VienRimosso() {
        // Arrange: aggiunge un materiale con descrizione null
        MaterialeDidattico materialeNullDesc = creaMateriale("Materiale Null", null);
        materiali.add(materialeNullDesc);

        // Act
        filtroDescrizione.filtra(materiali, "programmazione");

        // Assert: il materiale con descrizione null non deve sopravvivere al filtro
        assertFalse(materiali.stream().anyMatch(m -> m.getTitolo().equals("Materiale Null")),
                "Un materiale con descrizione null deve essere rimosso dal filtro");
    }

    //TEST SU SET VUOTO
    @Test
    void testFiltra_SetVuoto_NessunEccezione() {
        Set<MaterialeDidattico> setVuoto = new HashSet<>();
        try {
            filtroDescrizione.filtra(setVuoto, "programmazione");
        } catch (Exception e) {
            fail("Il filtro su un set vuoto non deve lanciare eccezioni. Invece ha lanciato: " + e.getClass().getSimpleName());
        }
        assertTrue(setVuoto.isEmpty(), "Il set vuoto deve rimanere vuoto dopo il filtro");
    }

    //TEST STESSO FILTRO DUE VOLTE
    @Test
    void testFiltra_ApplicatoDueVolte_StessoRisultato() {
        filtroDescrizione.filtra(materiali, "programmazione");
        int dimensioneDopoPrima = materiali.size();
        filtroDescrizione.filtra(materiali, "programmazione");
        int dimensioneDopoSeconda = materiali.size();
        assertEquals(dimensioneDopoPrima, dimensioneDopoSeconda,
                "Applicare lo stesso filtro due volte deve dare lo stesso risultato (idempotenza)");
    }


    //TEST FILTRO CON PLACEHOLDER
    @Test
    void testFiltra_CampoPlaceholder_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();

        filtroDescrizione.filtra(materiali, "Inserisci parola chiave ...");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Il placeholder della GUI non deve applicare nessun filtro");
    }
}