package control;

import control.filtro.*;
import entity.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class GestorePiattaforma {
    public static final int LOGIN_FALLITO = 0;
    public static final int LOGIN_SUCCESS_STUDENTE = 1;
    public static final int LOGIN_SUCCESS_DOCENTE = 2;
    public static final int REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE = GestoreUtente.REGISTRAZIONE_FALLITA_EMAIL_ESISTENTE;
    public static final int REGISTRAZIONE_FALLITA_DOMINIO_ERRATO  = GestoreUtente.REGISTRAZIONE_FALLITA_DOMINIO_ERRATO;
    public static final int REGISTRAZIONE_AVVENUTA= GestoreUtente.REGISTRAZIONE_AVVENUTA;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA = GestoreUtente.REGISTRAZIONE_FALLITA_MATRICOLA_ERRATA;
    public static final int REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE = GestoreUtente.REGISTRAZIONE_FALLITA_MATRICOLA_ESISTENTE;

    public static StatoFiltro StringToStatoFiltro(String tipologia){
        // Protezione contro i valori nulli o vuoti
        if (tipologia == null || tipologia.trim().isEmpty()) {
            return new FiltroNullo();
        }

        StatoFiltro filtro;
        switch(tipologia.trim().toLowerCase()) // toLowerCase ci mette al sicuro da distrazioni
        {
            case "categoria":
                filtro = new FiltroCategoria(); break;
            case "descrizione":
                filtro = new FiltroDescrizione(); break;
            case "pubblicato":
                filtro = new FiltroPubblicato(); break;
            case "titolo":
                filtro = new FiltroTitolo(); break;
            default:
                filtro = new FiltroNullo();
        }
        return filtro;
    }

    public static List<String[]> materialeInRighe(Set<MaterialeDidattico> materialiDidattici){
        List<String[]> righe = new ArrayList<>();
        for (MaterialeDidattico materialeDidattico : materialiDidattici) {
            String[] riga = new String[]{
                    materialeDidattico.getTitolo(),
                    materialeDidattico.getCategoria().toString(),
                    materialeDidattico.getDescrizione(),
                    materialeDidattico.getDataPubblicazione().toString(),
                    materialeDidattico.getSezione().getTitolo(),
                    materialeDidattico.getVisibilita().toString(),
                    "⋮"
            };
            righe.add(riga);
        }
        return righe;

    }

    public static List<String[]> visualizzaMateriali(String corso){
        return  visualizzaMateriali(corso,null,null);
    }
    public static List<String[]> visualizzaMateriali(String corso, Object campo, String tipologia){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Filtro filtro = new Filtro();

        Set<MaterialeDidattico> materialiOriginali = gestoreCorso.recuperaMateriali(utenteLoggato, corso);
        Set<MaterialeDidattico> materialiDidattici = new java.util.HashSet<>(materialiOriginali);

        filtro.setStato(StringToStatoFiltro(tipologia));
        filtro.filtra(materialiDidattici, campo);
        return materialeInRighe(materialiDidattici);
    }

    public static List<String[]> visualizzaMaterialiPubblicati(String corso){
        return visualizzaMaterialiPubblicati(corso,null,null);
    }

    public static List<String[]> visualizzaMaterialiPubblicati(String corso, Object campo, String tipologia){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Filtro filtro = new Filtro();

        // RECUPERA E COPIA IL SET per evitare danni al DB
        Set<MaterialeDidattico> materialiOriginali = gestoreCorso.recuperaMateriali(utenteLoggato, corso);
        Set<MaterialeDidattico> materialiDidattici = new java.util.HashSet<>(materialiOriginali);

        List<String[]> righe = new ArrayList<>();

        filtro.setStato(StringToStatoFiltro(tipologia));
        filtro.filtra(materialiDidattici, campo);
        filtro.setStato(new FiltroPubblicato());
        filtro.filtra(materialiDidattici, null);
        return materialeInRighe(materialiDidattici);
    }

    public static String getIdMateriale(String corso, String titolo){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        int idMateriale = gestoreCorso.getIdMateriale(utenteLoggato, corso,titolo);
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

    public static String[] getSezioni(String corso){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        Set<Sezione> valoriSezione = gestoreCorso.getSezioni(utenteLoggato, corso);

        String[] valoriStringa = new String[valoriSezione.size()+1];

        int i = 1;
        valoriStringa[0] = "Seleziona una sezione";
        for (Sezione sezione : valoriSezione) {
            valoriStringa[i] = sezione.getTitolo();
            i++;
        }

        return  valoriStringa;
    }

    public static String getPercorsoFile(String corso, String titolo){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        return gestoreCorso.getPercorsoFile(utenteLoggato, corso, titolo);
    }

    public static boolean eliminaMateriale(String corso, String titolo){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        return gestoreCorso.rimuoviMateriale(utenteLoggato, corso, titolo);
    }

    public static boolean inserisciMateriale(String corso, String titolo, String descrizione,
                                             String categoria, String visibilita, File fileScelto, String sezione){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        boolean esito = gestoreCorso.inserisciMateriale(utenteLoggato, corso, titolo, descrizione,
                categoria, visibilita,fileScelto,sezione);
        if(esito && visibilita.equals("PUBBLICATO")){
            GestoreNotifica gestoreNotifica = new GestoreNotifica();
            gestoreNotifica.inviaNotifica((Docente) utenteLoggato, corso);
        }

        return esito;

    }


    public static boolean modificaMateriale(String corso, String idMateriale, String titolo, String descrizione,
                                            String categoria, String visibilita, File fileScelto, String sezione){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        boolean esito =gestoreCorso.modificaMateriale(utenteLoggato, corso, idMateriale, titolo, descrizione,
                categoria, visibilita,fileScelto,sezione);

        if(esito && visibilita.equals("PUBBLICATO")){
            GestoreNotifica gestoreNotifica = new GestoreNotifica();
            gestoreNotifica.inviaNotifica((Docente) utenteLoggato, corso);
        }

        return esito;

    }

    public static boolean apriMateriale(String corso, String titolo){
        GestoreCorso gestoreCorso = new GestoreCorso();
        Utente utenteLoggato = SessionManager.getInstance().getUtenteLoggato();
        return gestoreCorso.apriMateriale(utenteLoggato, corso, titolo);
    }

    public static int inserimentoDatiUtente(String email, String matricola, String nome, String cognome, String password, boolean isStudente) {
        GestoreUtente gestoreUtente = new GestoreUtente();
        return gestoreUtente.inserimentoDatiUtente(email, matricola, nome, cognome, password, isStudente);
    }

    public static int inserimentoCredenziali(String email, String password) {
        GestoreUtente gestoreUtente = new GestoreUtente();
        Utente utente = gestoreUtente.inserimentoCredenziali(email, password);
        if (utente == null) {
            return GestorePiattaforma.LOGIN_FALLITO;
        }

        SessionManager.getInstance().setUtenteLoggato(utente);

        // Viene restituito alla GUI il codice corretto per aprire il frame giusto
        if (utente instanceof Studente) {
            return GestorePiattaforma.LOGIN_SUCCESS_STUDENTE;
        } else if (utente instanceof Docente) {
            return GestorePiattaforma.LOGIN_SUCCESS_DOCENTE;
        }

        return GestorePiattaforma.LOGIN_FALLITO;
    }

    public static List<String[]> visualizzaElencoCorsi(String emailUtente){
        GestoreIscrizione gestoreIscrizione=new GestoreIscrizione();
        GestoreCorso gestoreCorso=new GestoreCorso();
        Collection<Corso> elencoCorsi;

        if(SessionManager.getInstance().getUtenteLoggato()instanceof Studente){
            elencoCorsi=gestoreIscrizione.visualizzaElencoCorsi(emailUtente);
        }
        else{
            Docente d = (Docente) SessionManager.getInstance().getUtenteLoggato();
            elencoCorsi = gestoreCorso.getCorsiPerDocente(d);
        }

        List<String[]> righe = new ArrayList<>();
        if(elencoCorsi.isEmpty()){
            String[] riga = new String[]{
                    "Non ci sono corsi",
                    "",
                    "",
                    "",
                    ""
            };
            righe.add(riga);
        }
        else{
            for (Corso corso : elencoCorsi) {
                String[] riga = new String[]{
                        String.valueOf(corso.getCodice()),
                        corso.getTitolo(),
                        corso.getDescrizione(),
                        corso.getAnnoAccademico(),
                        "⋮"
                };
                righe.add(riga);
            }
        }
        return righe;
    }

    public static String[] visualizzaNotificheStudente(){
        GestoreNotifica gestoreNotifica = new GestoreNotifica();
        Studente studente = (Studente) SessionManager.getInstance().getUtenteLoggato();
        List<Notifica> notifiche =  gestoreNotifica.getNotifiche(studente);
        String[] righe = new String[notifiche.size()];

        int i = 0;

        for (Notifica notifica : notifiche) {
            righe[i] = notifica.getMessaggio();
            i++;
        }

        return righe;
        }
}
