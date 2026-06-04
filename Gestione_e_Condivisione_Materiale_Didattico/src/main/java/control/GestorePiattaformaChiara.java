package control;
import entity.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class GestorePiattaformaChiara {

    public GestorePiattaformaChiara() {
    }

    public static String getIdMateriale(String emailUtente, String corso, String titolo){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        int idMateriale = gestoreCorso.getIdMateriale(emailUtente,corso,titolo);
        return String.valueOf(idMateriale);
    }

    public static String[] getCategorieMateriali(){
        Categoria[] valoriEnum = Categoria.values();

        String[] valoriStringa = new String[valoriEnum.length+1];
        valoriStringa[0] = "Seleziona la categoria";

        for (int i = 0; i < valoriEnum.length; i++) {
            valoriStringa[i+1] = valoriEnum[i].toString();
        }
        return  valoriStringa;
    }

    public static String[] getVisibilita(){
        Visibilita[] valoriEnum = Visibilita.values();

        String[] valoriStringa = new String[valoriEnum.length+1];

        valoriStringa[0] = "Seleziona la visibilità";
        for (int i = 0; i < valoriEnum.length; i++) {
            valoriStringa[i+1] = valoriEnum[i].toString();
        }
        return  valoriStringa;
    }

    public static String[] getSezioni(String emailUtente, String corso){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        Set<Sezione> valoriSezione = gestoreCorso.getSezioni(emailUtente, corso);

        String[] valoriStringa = new String[valoriSezione.size()+1];

        int i = 1;
        valoriStringa[0] = "Seleziona una sezione";
        for (Sezione sezione : valoriSezione) {
            valoriStringa[i] = sezione.getTitolo();
            i++;
        }

        return  valoriStringa;
    }

    public static String getPercorsoFile(String emailUtente, String corso, String titolo){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        return gestoreCorso.getPercorsoFile(emailUtente, corso, titolo);
    }

    public static List<String[]> VisualizzaMateriali(String emailUtente, String corso){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();

        //Recupera i materiali didattici dal corso
        Set<MaterialeDidattico> materialiDidattici =gestoreCorso.recuperaMateriali(emailUtente, corso);

        //la lista da restituire alla GUI.
        //Ogni elemento della lista rappresenta una riga della JTable.
        List<String[]> righe = new ArrayList<>();

        //Converte ogni oggetto Materiale didattico in un array di String.
        for (MaterialeDidattico materialeDidattico : materialiDidattici) {

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

        return righe;
    }

    public static boolean eliminaMateriale(String emailUtente, String corso, String titolo){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        return gestoreCorso.rimuoviMateriale(emailUtente, corso, titolo);
    }

    public static boolean inserisciMateriale(String emailUtente, String corso, String titolo, String descrizione,
                                             String categoria, String visibilita, File fileScelto, String sezione){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        boolean esito = gestoreCorso.inserisciMateriale(emailUtente, corso, titolo, descrizione,
                categoria, visibilita,fileScelto,sezione);
        if(esito && visibilita.equals("PUBBLICATO")){
            GestoreNotifica gestoreNotifica = new GestoreNotifica();
            gestoreNotifica.inviaNotifica(emailUtente, corso);
        }

        return esito;

    }


    public static boolean modificaMateriale(String emailUtente, String corso, String idMateriale, String titolo, String descrizione,
                                             String categoria, String visibilita, File fileScelto, String sezione){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        boolean esito =gestoreCorso.modificaMateriale(emailUtente, corso, idMateriale, titolo, descrizione,
                categoria, visibilita,fileScelto,sezione);

        if(esito && visibilita.equals("PUBBLICATO")){
            GestoreNotifica gestoreNotifica = new GestoreNotifica();
            gestoreNotifica.inviaNotifica(emailUtente, corso);
        }

        return esito;

    }

    public static boolean apriMateriale(String emailUtente, String corso, String titolo){
        GestoreCorsoChiara gestoreCorso = new GestoreCorsoChiara();
        return gestoreCorso.apriMateriale(emailUtente, corso, titolo);
    }


}
