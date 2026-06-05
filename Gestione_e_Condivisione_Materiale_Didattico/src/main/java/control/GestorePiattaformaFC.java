package control;

import entity.MaterialeDidattico;
import entity.Visibilita;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GestorePiattaformaFC {
    public static List<String[]> VisualizzaMateriali(String emailUtente, String corso){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();

        //Recupera i materiali didattici dal corso
        Set<MaterialeDidattico> materialiDidattici =gestoreCorso.recuperaMateriali(emailUtente, corso);

        //la lista da restituire alla GUI.
        //Ogni elemento della lista rappresenta una riga della JTable.
        List<String[]> righe = new ArrayList<>();

        //Converte ogni oggetto Materiale didattico in un array di String.
        for (MaterialeDidattico materialeDidattico : materialiDidattici) {

            if(materialeDidattico.getVisibilita()== Visibilita.PUBBLICATO) {
                String[] riga = new String[]{
                        materialeDidattico.getTitolo(),
                        materialeDidattico.getCategoria().toString(),
                        materialeDidattico.getDescrizione(),
                        materialeDidattico.getDataPubblicazione(),
                        materialeDidattico.getSezione().getTitolo(),
                        materialeDidattico.getVisibilita().toString(),
                        "⋮"
                };
                righe.add(riga);
            }

        }

        return righe;
    }
}
