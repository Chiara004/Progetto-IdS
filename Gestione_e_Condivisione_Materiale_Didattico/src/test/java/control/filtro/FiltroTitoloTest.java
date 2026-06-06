package control.filtro;

import entity.Categoria;
import entity.MaterialeDidattico;
import entity.Sezione;
import entity.Visibilita;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FiltroTitoloTest {

    private FiltroTitolo filtroTitolo;
    private Set<MaterialeDidattico> materiali;

    // Titoli fissi usati nei test
    private static final String TITOLO_A = "Introduzione alla Programmazione";
    private static final String TITOLO_B = "Algoritmi e Strutture Dati";
    private static final String TITOLO_C = "Basi di Dati";

    @BeforeEach
    void setUp() {
        filtroTitolo = new FiltroTitolo();

        // Set con 3 materiali — uno per ogni titolo fisso
        materiali = new HashSet<>();
        materiali.add(creaMateriale(TITOLO_A));
        materiali.add(creaMateriale(TITOLO_B));
        materiali.add(creaMateriale(TITOLO_C));
    }

    /** Crea un MaterialeDidattico minimale con il titolo specificato. */
    private MaterialeDidattico creaMateriale(String titolo) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, "desc", LocalDate.now(), "/file.pdf",
                Categoria.SLIDE, Visibilita.PUBBLICATO);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }


    //TEST FILTRO A BUON FINE
    @Test
    void testFiltra_ParolaChiaveEsatta_TrovaCorrispondente() {
        filtroTitolo.filtra(materiali, "Introduzione");
        assertEquals(1, materiali.size(),
                "La ricerca per 'Introduzione' deve restituire esattamente un materiale");
        assertEquals(TITOLO_A, materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CON PIU CORRISPONDENTI
    @Test
    void testFiltra_ParolaChiaveCondivisa_TrovaPiuCorrispondenti() {
        filtroTitolo.filtra(materiali, "Dati");

        assertEquals(2, materiali.size(),
                "La ricerca per 'Dati' deve restituire i 2 materiali che la contengono");
        List<String> titoli = new ArrayList<>();
        for (MaterialeDidattico m : materiali) {
            titoli.add(m.getTitolo());
        }

        assertTrue(titoli.contains(TITOLO_B));
        assertTrue(titoli.contains(TITOLO_C));
    }

    //TEST FILTRO CON PAROLA CHE NON ESISTE
    @Test
    void testFiltra_ParolaChiaveNonPresente_SetVuoto() {
        filtroTitolo.filtra(materiali, "Termodinamica");
        assertTrue(materiali.isEmpty(),
                "Nessun materiale deve sopravvivere se la parola chiave non è in nessun titolo");
    }

    //TEST FILTRO CON PAROLA PARZIALE
    @Test
    void testFiltra_ParolaChiaveParziale_TrovaCorrispondente() {
        filtroTitolo.filtra(materiali, "algo"); // sottostringa di "Algoritmi"
        assertEquals(1, materiali.size(),
                "La ricerca parziale 'algo' deve trovare 'Algoritmi e Strutture Dati'");
        assertEquals(TITOLO_B, materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CASE SENSITIVE
    @Test
    void testFiltra_QueryTuttaMaiuscole_TrovaLostesso() {
        filtroTitolo.filtra(materiali, "INTRODUZIONE");

        assertEquals(1, materiali.size(),
                "La ricerca in maiuscolo deve trovare lo stesso risultato del minuscolo");
        assertEquals(TITOLO_A, materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO CASE SENSITIVE MISTO
    @Test
    void testFiltra_QueryCaseMisto_TrovaLostesso() {
        filtroTitolo.filtra(materiali, "InTrOdUzIoNe");

        assertEquals(1, materiali.size(),
                "La ricerca con case misto deve trovare lo stesso risultato");
        assertEquals(TITOLO_A, materiali.iterator().next().getTitolo());
    }

    @Test
    void testFiltra_TitoloConMaiuscole_TrovatoConQueryMinuscola() {
        filtroTitolo.filtra(materiali, "algoritmi");

        assertEquals(1, materiali.size(),
                "Una query minuscola deve trovare un titolo con maiuscole");
        assertEquals(TITOLO_B, materiali.iterator().next().getTitolo());
    }

    //TEST SU SET VUOTO
    @Test
    void testFiltra_SetVuoto_NessunEccezione() {
        Set<MaterialeDidattico> setVuoto = new HashSet<>();
        try {
            filtroTitolo.filtra(setVuoto, "Dati");
        } catch (Exception e) {
            fail("Il filtro su un set vuoto non deve lanciare eccezioni. Invece ha lanciato: " + e.getClass().getSimpleName());
        }
        assertTrue(setVuoto.isEmpty(), "Il set vuoto deve rimanere vuoto dopo il filtro");
    }

    //TEST STESSO FILTRO DUE VOLTE
    @Test
    void testFiltra_ApplicatoDueVolte_StessoRisultato() {
        filtroTitolo.filtra(materiali, "Dati");
        int dimensioneDopoPrimaApplicazione = materiali.size();

        filtroTitolo.filtra(materiali, "Dati");
        int dimensioneDopotSecondaApplicazione = materiali.size();
        assertEquals(dimensioneDopoPrimaApplicazione, dimensioneDopotSecondaApplicazione,
                "Applicare lo stesso filtro due volte deve dare lo stesso risultato (idempotenza)");
    }

    //TEST CAMPO NULL
    @Test
    void testFiltra_CampoNull_NessunaMateriale_Rimossa() {
        int dimensioneOriginale = materiali.size();
        filtroTitolo.filtra(materiali, null);
        assertEquals(dimensioneOriginale, materiali.size(),
                "Con campo null il filtro non deve rimuovere nessun materiale");
    }

    //TEST FILTRO CON CAMPO VUOTO
    @Test
    void testFiltra_CampoStringaVuota_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();

        filtroTitolo.filtra(materiali, "");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Con stringa vuota il filtro non deve rimuovere nessun materiale");
    }

    //TEST CAMPO CON SOLO SPAZI
    @Test
    void testFiltra_CampoSoliSpazi_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();
        filtroTitolo.filtra(materiali, "   ");
        assertEquals(dimensioneOriginale, materiali.size(),
                "Con stringa di soli spazi il filtro non deve rimuovere nessun materiale");
    }


    //TEST FILTRO CON PLACEHOLDER
    @Test
    void testFiltra_CampoPlaceholder_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();

        filtroTitolo.filtra(materiali, "Inserisci parola chiave ...");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Il placeholder della GUI non deve applicare nessun filtro");
    }
}