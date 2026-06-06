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

public class FiltroPubblicatoTest {

    private FiltroPubblicato filtroPubblicato;
    private Set<MaterialeDidattico> materiali;

    @BeforeEach
    void setUp() {
        filtroPubblicato = new FiltroPubblicato();
        materiali = new HashSet<>();
    }

    private MaterialeDidattico creaMateriale(String titolo, Visibilita visibilita) {
        MaterialeDidattico m = new MaterialeDidattico(
                titolo, "desc", LocalDate.now(), "/file.pdf",
                Categoria.SLIDE, visibilita);
        m.setSezione(new Sezione("Sezione Test"));
        return m;
    }


    //TEST FILTRO MATERIALI NON PUBBLICATI
    @Test
    void testFiltra_MaterialeNonPubblicato_VienRimosso() {
        materiali.add(creaMateriale("Materiale Non Pubblicato", Visibilita.NON_PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        assertTrue(materiali.isEmpty(),
                "Un materiale NON_PUBBLICATO deve essere rimosso dal filtro");
    }

    //TEST FILTRO PIU MATERIALI NON PUBBLICATI
    @Test
    void testFiltra_PiuMaterialiNonPubblicati_TuttiRimossi() {
        materiali.add(creaMateriale("Non Pubblicato 1", Visibilita.NON_PUBBLICATO));
        materiali.add(creaMateriale("Non Pubblicato 2", Visibilita.NON_PUBBLICATO));
        materiali.add(creaMateriale("Non Pubblicato 3", Visibilita.NON_PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        assertTrue(materiali.isEmpty(),
                "Tutti i materiali NON_PUBBLICATO devono essere rimossi");
    }

    //TEST FILTRO MATERIALI PUBBLICATI
    @Test
    void testFiltra_MaterialePubblicato_NonVienRimosso() {
        materiali.add(creaMateriale("Materiale Pubblicato", Visibilita.PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        assertEquals(1, materiali.size(),
                "Un materiale PUBBLICATO non deve essere rimosso dal filtro");
        assertEquals("Materiale Pubblicato", materiali.iterator().next().getTitolo());
    }

    //TEST FILTRO PIU MATERIALI PUBBLICATI
    @Test
    void testFiltra_PiuMaterialiPubblicati_NessunoRimosso() {
        materiali.add(creaMateriale("Pubblicato 1", Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Pubblicato 2", Visibilita.PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        assertEquals(2, materiali.size(),
                "Tutti i materiali PUBBLICATO devono sopravvivere al filtro");
    }

    //TEST FILTRO SET MISTO
    @Test
    void testFiltra_SetMisto_SoloPublicatiSopravvivono() {
        materiali.add(creaMateriale("Pubblicato A", Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Non Pubblicato B",Visibilita.NON_PUBBLICATO));
        materiali.add(creaMateriale("Pubblicato C", Visibilita.PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        assertEquals(2, materiali.size(),
                "Solo i materiali PUBBLICATO devono sopravvivere");
        boolean tuttiPubblicati = true;
        for (MaterialeDidattico m : materiali) {
            if (m.getVisibilita() != Visibilita.PUBBLICATO) {
                tuttiPubblicati = false;
                break; // Ottimizzazione: ci fermiamo al primo elemento non valido
            }
        }

        assertTrue(tuttiPubblicati, "Tutti i materiali rimasti devono avere visibilità PUBBLICATO");
    }

    //TEST IGNORA PARAMETRO CAMPO
    @Test
    void testFiltra_CampoIgnorato_ComportamentoInvariato() {
        materiali.add(creaMateriale("Pubblicato", Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Non Pubblicato", Visibilita.NON_PUBBLICATO));

        Set<MaterialeDidattico> copia = new HashSet<>();
        copia.add(creaMateriale("Pubblicato", Visibilita.PUBBLICATO));
        copia.add(creaMateriale("Non Pubblicato", Visibilita.NON_PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        filtroPubblicato.filtra(copia, "valore_ignorato");
        assertEquals(materiali.size(), copia.size(),
                "Il parametro campo deve essere ignorato: il risultato non deve cambiare");
    }

    //TEST STESSO FILTRO DUE VOLTE
    @Test
    void testFiltra_ApplicatoDueVolte_StessoRisultato() {
        materiali.add(creaMateriale("Pubblicato",     Visibilita.PUBBLICATO));
        materiali.add(creaMateriale("Non Pubblicato", Visibilita.NON_PUBBLICATO));
        filtroPubblicato.filtra(materiali, null);
        int dimensioneDopoPrima = materiali.size();
        filtroPubblicato.filtra(materiali, null);
        int dimensioneDopoSeconda = materiali.size();
        assertEquals(dimensioneDopoPrima, dimensioneDopoSeconda,
                "Applicare il filtro due volte deve dare lo stesso risultato (idempotenza)");
    }
}