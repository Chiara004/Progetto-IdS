package database;

import entity.Utente;
import jakarta.persistence.EntityManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Properties;
import entity.*;

public class Popolamento {
    private final CSVImporter importer;


    public Popolamento(EntityManager em) {
        this.importer = new CSVImporter(em);
    }

    public void popolaDatabase() {
        System.out.println("--- Avvio popolamento database da CSV ---");

        // Tabella Utente
        importer.importaSeTabellaVuota("utenti.csv", "Utente", new ConvertitoreRiga<Utente>() {
            @Override
            public Utente converti(String[] campi, EntityManager em) {
                try {
                    if(campi[5].trim().equals("Docente")){
                        Utente u = new Docente();
                        inserisci(u, campi);
                        return u;
                    }
                    else{
                        Utente u = new Studente();
                        inserisci(u, campi);
                        return u;
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    return null;
                }
            }
        });

        //Tabella Corso
        importer.importaSeTabellaVuota("corsi.csv", "Corso", new ConvertitoreRiga<Corso>() {
            @Override
            public Corso converti(String[] campi, EntityManager em) {
                try {
                    Corso c = new Corso();
                    Docente d = em.find(Docente.class, campi[4].trim());
                    c.setCodice(Integer.parseInt(campi[0].trim()));
                    c.setAnnoAccademico(campi[1].trim());
                    c.setDescrizione(campi[2].trim());
                    c.setTitolo(campi[3].trim());
                    c.setDocente(d);

                    return c;
                } catch (Exception e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    return null;
                }
            }
        });

        //Tabella Sezione
        importer.importaSeTabellaVuota("sezioni.csv", "Sezione", new ConvertitoreRiga<Sezione>() {
            @Override
            public Sezione converti(String[] campi, EntityManager em) {
                try {
                    Sezione s = new Sezione();
                    Corso c = em.find(Corso.class, Integer.parseInt(campi[2].trim()));
                    s.setTitolo(campi[1].trim());
                    s.setCorso(c);

                    return s;
                } catch (Exception e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    return null;
                }
            }
        });

        //Tabella Notifiche
        importer.importaSeTabellaVuota("notifiche.csv", "Notifica", new ConvertitoreRiga<Notifica>() {
            @Override
            public Notifica converti(String[] campi, EntityManager em) {
                try {
                    Notifica n = new Notifica();
                    Studente s = em.find(Studente.class, campi[2].trim());
                    n.setMessaggio(campi[1].trim());
                    n.setStudente(s);

                    return n;
                } catch (Exception e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    return null;
                }
            }
        });

        //Tabella MaterialeDidattico
        importer.importaSeTabellaVuota("materialeDidattico.csv", "MaterialeDidattico", new ConvertitoreRiga<MaterialeDidattico>() {
            @Override
            public MaterialeDidattico converti(String[] campi, EntityManager em) {
                try {
                    MaterialeDidattico m= new MaterialeDidattico();
                    Corso c = em.find(Corso.class, Integer.parseInt(campi[7].trim()));
                    Sezione s = em.find(Sezione.class, Integer.parseInt(campi[8].trim()));
                    m.setCategoria(Categoria.valueOf(campi[1].trim()));
                    m.setDataPubblicazione(LocalDate.parse(campi[2].trim()));
                    m.setDescrizione(campi[3].trim());
                    m.setPercorsoFile(campi[4].trim());
                    m.setTitolo(campi[5].trim());
                    m.setVisibilita(Visibilita.valueOf(campi[6].trim()));
                    m.setCorso(c);
                    m.setSezione(s);

                    return m;
                } catch (Exception e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    return null;
                }
            }
        });

        //Tabella CorsoStudente
        importer.importaSeTabellaVuota("corso_studente.csv", "Corso_studente", new ConvertitoreRiga<Corso>() {
            @Override
            public Corso converti(String[] campi, EntityManager em) {
                try {
                    Corso c = em.find(Corso.class, Integer.parseInt(campi[0].trim()));
                    Studente s = em.find(Studente.class, campi[1].trim());

                    c.getStudenti().add(s);
                    s.getCorsi().add(c);

                    return c;
                } catch (Exception e) {
                    System.err.println("  Riga malformata, saltata: " + String.join(";", campi));
                    e.printStackTrace();
                    return null;
                }
            }
        });

        salvataggioMateriali();
        System.out.println("--- Popolamento completato ---");
    }
    private void inserisci(Utente u, String...campi){
        u.setIdUtente(campi[0].trim());
        u.setCognome(campi[1].trim());
        u.setEmailIstituzionale(campi[2].trim());
        u.setNome(campi[3].trim());

        String passwordInChiaro = campi[4].trim();
        // Se nel CSV la password è vuota, usa la matricola come password provvisoria
        if (passwordInChiaro.isEmpty()) {
            passwordInChiaro = campi[0].trim();
        }
        // Password cifrata
        String passwordCifrata = org.mindrot.jbcrypt.BCrypt.hashpw(passwordInChiaro, org.mindrot.jbcrypt.BCrypt.gensalt());

        u.setPassword(passwordCifrata);

        u.setRuolo(campi[5].trim());
    }

    public void salvataggioMateriali() {
        try {
            GestoreFile gestore = new GestoreFile();
            String cartellaDestinazione = gestore.getPercorsoBaseMateriali();
            Path destinazione = Paths.get(cartellaDestinazione);
            Files.createDirectories(destinazione); // crea la cartella se non esiste

            // Legge la lista dei file da un indice testuale
            URL indice = getClass().getClassLoader().getResource("materiali_didattici/indice.txt");
            if (indice == null) {
                System.out.println("Nessun indice.txt trovato in materiali_didattici/ — skip.");
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(indice.openStream()))) {

                String nomeFile;
                int copiati = 0, saltati = 0;

                while ((nomeFile = br.readLine()) != null) {
                    nomeFile = nomeFile.trim();
                    if (nomeFile.isEmpty() || nomeFile.startsWith("#")) continue;

                    Path fileDestinazione = destinazione.resolve(nomeFile);

                    if (Files.exists(fileDestinazione)) {
                        saltati++;
                        continue; // non sovrascrivere file già presenti
                    }

                    try (InputStream input = getClass().getClassLoader()
                            .getResourceAsStream("materiali_didattici/" + nomeFile)) {

                        if (input == null) {
                            System.err.println("File non trovato nelle risorse: " + nomeFile);
                            continue;
                        }

                        Files.copy(input, fileDestinazione);
                        System.out.println("Copiato: " + nomeFile);
                        copiati++;
                    }
                }

                System.out.printf("Materiali: %d copiati, %d già presenti.%n", copiati, saltati);
            }

        } catch (IOException e) {
            System.err.println("Errore durante l'inizializzazione materiali: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
