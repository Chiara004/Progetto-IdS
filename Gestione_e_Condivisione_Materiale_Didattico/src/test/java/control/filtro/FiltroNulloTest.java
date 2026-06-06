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

public class FiltroNulloTest {

    private FiltroNullo filtroNullo;
    @BeforeEach
    void setUp() {
        filtroNullo = new FiltroNullo();
    }

    private MaterialeDidattico creaMateriale(String titolo, Visibilita visibilita) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, "desc", LocalDate.now(), "/file.pdf",
                Categoria.SLIDE, visibilita);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }

    //TEST SET VUOTO
    @Test
    void testFiltra_SetVuoto_RimanePiuoto() {
        Set<MaterialeDidattico> materiali = new HashSet<>();
        filtroNullo.filtra(materiali, null);
        assertTrue(materiali.isEmpty(),
                "Il FiltroNullo non deve modificare un set vuoto");
    }

    //TEST FILTRO NULLO UN ELEMENTO
    @Test
    void testFiltra_UnElemento_NonVienRimosso() {
        Set<MaterialeDidattico> materiali = new HashSet<>();
        materiali.add(creaMateriale("Slide 1", Visibilita.PUBBLICATO));

        filtroNullo.filtra(materiali, null);

        assertEquals(1, materiali.size(),
                "Il FiltroNullo non deve rimuovere nessun elemento");
    }

    //TEST FILTRO NULLO PIU ELEMENTI
    @Test
    void testFiltra_PiuElementi_NessunoRimosso() {
        Set<MaterialeDidattico> materiali = new HashSet<>();
        materiali.add(creaMateriale("Slide 1", Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Slide 2",Visibilita.NON_PUBBLICATO));
        materiali.add(creaMateriale("Dispensa 1", Visibilita.PUBBLICATO));
        filtroNullo.filtra(materiali, null);
        assertEquals(3, materiali.size(),
                "Il FiltroNullo non deve rimuovere nessun elemento, indipendentemente dalla visibilità");
    }

    //TEST FILTRO NULLO SU SET VUOTO
    @Test
    void testFiltra_SetVuotoCampoNull_NessunEccezione() {
        try {
            filtroNullo.filtra(new HashSet<>(), null);
        } catch (Exception e) {
            fail("Il FiltroNullo non deve mai lanciare eccezioni. Invece ha lanciato: " + e.getClass().getSimpleName());
        }
    }

    //TEST FILTRO NULLO SU SET PIENO
    @Test
    void testFiltra_SetPieno_NessunEccezione() {
        Set<MaterialeDidattico> materiali = new HashSet<>();
        materiali.add(creaMateriale("Slide 1", Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Slide 2", Visibilita.NON_PUBBLICATO));

        try {
            filtroNullo.filtra(materiali, "campo");
        } catch (Exception e) {
            fail("Il FiltroNullo non deve mai lanciare eccezioni neanche con set pieno. Invece ha lanciato: " + e.getClass().getSimpleName());
        }
    }
}