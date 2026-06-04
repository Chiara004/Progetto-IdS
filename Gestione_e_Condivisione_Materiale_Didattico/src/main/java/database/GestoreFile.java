package database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class GestoreFile {
    public GestoreFile(){

    }

    public String getPercorsoBaseMateriali() {
        Properties prop = new Properties();
        // Cerca il file nella cartella resources
        try (java.io.InputStream input = getClass().getClassLoader().getResourceAsStream("url.properties")) {
            if (input == null) {
                System.err.println("Impossibile trovare url.properties");
                return null;
            }
            prop.load(input);

            return prop.getProperty("upload.dir");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean eliminaFileFisico(String nomeFile) {
        String percorsoBase = getPercorsoBaseMateriali();
        if (percorsoBase == null)
            return false;

        try {
            // Unisce il percorso base al nome del file
            Path pathDelFile = Paths.get(percorsoBase, nomeFile);

            // deleteIfExists elimina il file e restituisce true.
            // Se il file non c'era già, restituisce false senza lanciare eccezioni bloccanti.
            return Files.deleteIfExists(pathDelFile);
        } catch (Exception e) {
            System.err.println("Errore durante l'eliminazione del file: " + e.getMessage());
            return false;
        }
    }
}
