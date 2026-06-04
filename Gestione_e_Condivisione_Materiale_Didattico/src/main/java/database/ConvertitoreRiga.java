package database;
import jakarta.persistence.EntityManager;

public interface ConvertitoreRiga <T>{
    T converti(String[] campi, EntityManager em);

}
