package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;


public class CSVImporter {
    private final EntityManager em;

    public CSVImporter(EntityManager em) {
        this.em = em;
    }

    /**
     * Legge un CSV dal classpath (src/main/resources/data/)
     * e restituisce le righe come lista di array di stringhe.
     */
    public List<String[]> leggiCsv(String nomeFile) throws IOException {
        List<String[]> righe = new ArrayList<>();

        URL url = getClass().getClassLoader().getResource("data/" + nomeFile);
        if (url == null) {
            System.out.println("File non trovato nel classpath: data/" + nomeFile);
            return righe;
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {

            String linea;
            boolean primaRiga = true;
            while ((linea = br.readLine()) != null) {
                if (primaRiga) { // salta l'intestazione
                    primaRiga = false;
                    continue;
                }
                if (!linea.isBlank()) {
                    righe.add(linea.split(";", -1)); // usa ";" come separatore
                }
            }
        }
        return righe;
    }


    public <T> void importaSeTabellaVuota(String nomeFile,
                                          String nomeEntita,
                                          ConvertitoreRiga<T> convertitore) {
        try {
            Long count = 0L;
            if(nomeEntita.equals("Corso_studente")){
                count = ((Number) em.createNativeQuery("SELECT count(*) FROM corso_studente").getSingleResult()).longValue();
            } else {
                count = em.createQuery("SELECT COUNT(e) FROM " + nomeEntita + " e", Long.class).getSingleResult();
            }

            if (count > 0) {
                System.out.println("Tabella " + nomeEntita + " già popolata (" + count + " righe)");
                return;
            }

            List<String[]> righe = leggiCsv(nomeFile);
            if (righe.isEmpty()) {
                System.out.println("Nessuna riga da importare per: " + nomeFile);
                return;
            }

            EntityTransaction tx = em.getTransaction();
            tx.begin();
            try {
                int contatore = 0;
                for (String[] campi : righe) {
                    T entita = convertitore.converti(campi, em);

                    if (entita != null) {
                        em.merge(entita);
                        contatore++;
                    }
                }
                tx.commit();
                System.out.println("Importati " + contatore + " record in " + nomeEntita);
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                System.err.println("Errore durante import di " + nomeFile + ": " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("Errore generico per " + nomeEntita + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
