package eseguibile;
import boundary.LoginForm;
import database.JpaUtil;
import jakarta.persistence.EntityManager;
import database.PopolamentoDatabase;
import boundary.LoginForm.*;
import javax.swing.*;

public class MainApp {

    public static void main(String[] args) {
        System.out.println("=== AVVIO APPLICAZIONE ===");

        try {
            // 1. Singleton JpaUtil
            JpaUtil jpaUtil = JpaUtil.getInstance();

            // 2. EntityManager per il seeding
            EntityManager em = jpaUtil.getEntityManager();
            System.out.println("Connessione al database stabilita.");

            // 3. Popolamento da CSV (solo se tabelle vuote)
            PopolamentoDatabase dataset = new PopolamentoDatabase(em);
            dataset.popolaDatabase();

            // 4. Chiudiamo l'EntityManager del seeding
            em.close();

            System.out.println("Database pronto.");

            // 5. Avvio GUI sul thread di Swing
            SwingUtilities.invokeLater(() -> {
                LoginForm mainFrame = new LoginForm();
                mainFrame.apriLoginForm();
            });

            System.out.println("=== DATABASE INIZIALIZZATO E GUI AVVIATA ===");

            // La factory deve restare aperta per tutta la vita dell'applicazione.
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Chiusura risorse JPA...");
                jpaUtil.chiudi();
            }));

        } catch (Exception e) {
            System.err.println("✗ Errore critico durante l'avvio:");
            e.printStackTrace();
        }
    }
}
