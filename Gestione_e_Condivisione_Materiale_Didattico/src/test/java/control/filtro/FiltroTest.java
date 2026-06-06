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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FiltroTest {
    private FiltroTitolo filtroTitolo;
    private Set<MaterialeDidattico> materiali;
    private FiltroDescrizione filtroDescrizione;

    private MaterialeDidattico creaMateriale(String titolo, String descrizione, Categoria categoria, Visibilita visibilita) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, descrizione, LocalDate.now(), "/file.pdf",
                categoria, visibilita);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }

    @BeforeEach
    void setUp() {
        filtroTitolo = new FiltroTitolo();
        filtroDescrizione = new FiltroDescrizione();
        materiali = new HashSet<>();
        materiali.add(creaMateriale("Mat A", "desc", Categoria.SLIDE,Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Mat B", "desc", Categoria.DISPENSE, Visibilita.PUBBLICATO ));
        materiali.add(creaMateriale("Mat C", "desc", Categoria.SLIDE,Visibilita.PUBBLICATO));

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

        filtroDescrizione.filtra(materiali, "");

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

        filtroDescrizione.filtra(materiali, "Inserisci parola chiave ...");

        assertEquals(dimensioneOriginale, materiali.size(),
                "Il placeholder della GUI non deve applicare nessun filtro");
    }
}
