package eseguibile;
import database.JpaUtil;
import jakarta.persistence.EntityManager;

public class MainApplicazione {

    public static void main(String[] args) {
        System.out.println("=== AVVIO GENERAZIONE DATABASE E TABELLE ===");

        try {
            // 1. Otteniamo l'istanza Singleton di JpaUtil.
            // Questo leggerà secret.properties e inizializzerà l'EntityManagerFactory.
            JpaUtil jpaUtil = JpaUtil.getInstance();

            // 2. Apriamo un EntityManager.
            // Questa azione forza Hibernate a connettersi al database e ad applicare
            // la strategia di generazione delle tabelle basata sulle tue classi Entity.
            EntityManager em = jpaUtil.getEntityManager();

            System.out.println("Connessione stabilita con successo al database!");
            System.out.println("Verifica e creazione delle tabelle completata.");

            // 3. Chiudiamo l'EntityManager appena usato (buona norma)
            em.close();

            // 4. Chiudiamo la factory prima che il programma termini
            jpaUtil.chiudi();
            System.out.println("=== PROCESSO COMPLETATO E RISORSE RILASCIATE ===");

        } catch (Exception e) {
            System.err.println("Si è verificato un errore critico durante l'inizializzazione:");
            e.printStackTrace();
        }
    }
}
