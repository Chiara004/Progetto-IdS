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


public class FiltroCategoriaTest {
    private FiltroCategoria filtro;
    private Set<MaterialeDidattico> materiali;

    private MaterialeDidattico creaMateriale(String titolo, String descrizione, Categoria categoria, Visibilita visibilita) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, descrizione, LocalDate.now(), "/file.pdf",
                categoria, visibilita);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }

    @BeforeEach
    void setUp() {
        filtro = new FiltroCategoria();
        materiali = new HashSet<>();
        materiali.add(creaMateriale("Mat A", "desc", Categoria.SLIDE,Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Mat B", "desc", Categoria.DISPENSE, Visibilita.PUBBLICATO ));
        materiali.add(creaMateriale("Mat C", "desc", Categoria.SLIDE,Visibilita.PUBBLICATO));

    }

    //TEST FILTRO A BUON FINE
    @Test
    void testFiltra_CategoriaEsatta_TrovaSoloCorrispondenti() {
        filtro.filtra(materiali, Categoria.SLIDE);
        assertEquals(2, materiali.size(),
                "Il filtro per SLIDE deve restituire i 2 materiali con quella categoria");
        for (MaterialeDidattico m : materiali) {
            assertEquals(Categoria.SLIDE, m.getCategoria());
        }
    }

    //TEST FILREO A BUON FINE
    @Test
    void testFiltra_CategoriaMinoritaria_TrovaUno() {
        filtro.filtra(materiali, Categoria.DISPENSE);
        assertEquals(1, materiali.size(),
                "Il filtro per DISPENSE deve restituire solo 1 materiale");
        assertEquals(Categoria.DISPENSE, materiali.iterator().next().getCategoria());
    }

    //TEST FILTRO CON CATEGORIA NON TROVATA
    @Test
    void testFiltra_CategoriaAssente_SetVuoto() {
        // Nessun materiale ha categoria ESERCIZI
        filtro.filtra(materiali, Categoria.ESERCIZI);
        assertTrue(materiali.isEmpty(),
                "Se nessun materiale ha quella categoria il set deve essere vuoto");
    }

    //TEST FILTRO CON CATEGORIA NULLA
    @Test
    void testFiltra_CategoriaNull_MaterialeRimosso() {
        materiali.add(creaMateriale("Mat D", "desc", null, Visibilita.PUBBLICATO));
        filtro.filtra(materiali, Categoria.SLIDE);
        boolean contieneCategoriaNull = false;
        for (MaterialeDidattico m : materiali) {
            if (m.getCategoria() == null) {
                contieneCategoriaNull = true;
                break; // Interrompe il ciclo non appena trova una violazione
            }
        }

        assertFalse(contieneCategoriaNull, "Un materiale con categoria null deve essere rimosso");
    }

    //TEST FILTRO CON CASE SENSITIVE
    @Test
    void testFiltra_CaseInsensitive_TrovaConStringaMaiuscola() {
        filtro.filtra(materiali, "SLIDE");
        assertEquals(2, materiali.size(),
                "La ricerca per stringa 'SLIDE' deve trovare i materiali SLIDE");
    }
    //TEST CAMPO NULL
    @Test
    void testFiltra_CampoNull_NessunaMateriale_Rimossa() {
        int dimensioneOriginale = materiali.size();
        filtro.filtra(materiali, null);
        assertEquals(dimensioneOriginale, materiali.size(),
                "Con campo null il filtro non deve rimuovere nessun materiale");
    }

    //TEST FILTRO CON CAMPO VUOTO
    @Test
    void testFiltra_CampoStringaVuota_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();

        filtro.filtra(materiali, "");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Con stringa vuota il filtro non deve rimuovere nessun materiale");
    }

    //TEST CAMPO CON SOLO SPAZI
    @Test
    void testFiltra_CampoSoliSpazi_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();
        filtro.filtra(materiali, "   ");
        assertEquals(dimensioneOriginale, materiali.size(),
                "Con stringa di soli spazi il filtro non deve rimuovere nessun materiale");
    }


    //TEST FILTRO CON PLACEHOLDER
    @Test
    void testFiltra_CampoPlaceholder_NessunMaterialeRimosso() {
        int dimensioneOriginale = materiali.size();

        filtro.filtra(materiali, "Inserisci parola chiave ...");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Il placeholder della GUI non deve applicare nessun filtro");
    }
}
