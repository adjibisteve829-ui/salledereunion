package bj.salledereunion.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class PersistenceUtil {
    private static volatile EntityManagerFactory factory;

    private PersistenceUtil() {
    }

    private static EntityManagerFactory factory() {
        if (factory == null || !factory.isOpen()) {
            synchronized (PersistenceUtil.class) {
                if (factory == null || !factory.isOpen()) {
                    factory = creerFactory();
                }
            }
        }
        return factory;
    }

    private static EntityManagerFactory creerFactory() {
        Map<String, Object> proprietes = new HashMap<>();
        try {
            var configuration = DatabaseConfiguration.charger();
            proprietes.put("jakarta.persistence.jdbc.url", configuration.getProperty("db.url"));
            proprietes.put("jakarta.persistence.jdbc.user", configuration.getProperty("db.user"));
            proprietes.put("jakarta.persistence.jdbc.password",
                    configuration.getProperty("db.password"));
        } catch (java.sql.SQLException erreur) {
            throw new IllegalStateException("Configuration PostgreSQL indisponible.", erreur);
        }
        return Persistence.createEntityManagerFactory("salledereunion", proprietes);
    }

    public static <T> T executer(Function<EntityManager, T> operation) {
        EntityManager entityManager = factory().createEntityManager();
        try {
            return operation.apply(entityManager);
        } finally {
            entityManager.close();
        }
    }

    public static <T> T executerTransaction(Function<EntityManager, T> operation) {
        return executer(entityManager -> {
            var transaction = entityManager.getTransaction();
            transaction.begin();
            try {
                T resultat = operation.apply(entityManager);
                transaction.commit();
                return resultat;
            } catch (RuntimeException erreur) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw erreur;
            }
        });
    }

    public static void fermer() {
        synchronized (PersistenceUtil.class) {
            if (factory != null && factory.isOpen()) {
                factory.close();
            }
            factory = null;
        }
    }
}
