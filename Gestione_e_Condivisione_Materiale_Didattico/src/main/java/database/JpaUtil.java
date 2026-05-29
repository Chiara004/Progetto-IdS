package database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaUtil {

     //Istanza unica di JpaUtil.(pattern Singleton)
    private static JpaUtil instance;

    /*
     * EntityManagerFactory condivisa.
     *
     * La factory viene creata una sola volta, perché è un oggetto
     * costoso da inizializzare: legge la persistence unit dal file
     * persistence.xml e prepara Hibernate per comunicare con il database.
     */
    private EntityManagerFactory emf;

    //costruttore privato
    private JpaUtil() {
        /*
         * Creiamo la EntityManagerFactory usando la persistence unit
         * definita nel file persistence.xml.
         *
         * Il nome "boatyardPU" deve coincidere con:
         *
         * <persistence-unit name="boatyardPU">
         */
        emf = Persistence.createEntityManagerFactory("boatyardPU");
    }


     //Punto di accesso globale all'unica istanza di JpaUtil.
    public static JpaUtil getInstance() {
        if (instance == null) {
            instance = new JpaUtil();
        }

        return instance;
    }

    /*
     * Crea un nuovo EntityManager.
     *
     * Attenzione: l'EntityManager non è Singleton.
     * Ogni operazione di persistenza deve usare un proprio EntityManager,
     * perché l'EntityManager mantiene lo stato della singola sessione
     * di lavoro con il database.
     */
    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    /*
     * Chiude la EntityManagerFactory.
     *
     * Questo metodo va chiamato alla fine dell'applicazione,
     * quando non sono più necessarie operazioni di persistenza.
     */
    public void chiudi() {
        emf.close();
    }
}
