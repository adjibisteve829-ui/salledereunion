package bj.salledereunion.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfiguration {
    private DatabaseConfiguration() {
    }

    public static Properties charger() throws SQLException {
        Properties proprietes = depuisEnvironnement();
        if (proprietes == null) {
            proprietes = depuisFichier();
        }

        String url = proprietes.getProperty("db.url");
        String utilisateur = proprietes.getProperty("db.user");
        String motDePasse = proprietes.getProperty("db.password");
        if (url == null || url.isBlank() || utilisateur == null || utilisateur.isBlank()
                || motDePasse == null) {
            throw new SQLException("La configuration PostgreSQL doit définir db.url, db.user "
                    + "et db.password (ou DB_URL/DB_HOST, DB_PORT, DB_NAME, DB_USER et "
                    + "DB_PASSWORD dans l'environnement).");
        }
        return proprietes;
    }

    private static Properties depuisEnvironnement() throws SQLException {
        String url = System.getenv("DB_URL");
        String hote = System.getenv("DB_HOST");
        String port = System.getenv("DB_PORT");
        String base = System.getenv("DB_NAME");
        String utilisateur = System.getenv("DB_USER");
        String motDePasse = System.getenv("DB_PASSWORD");

        if (url == null && hote == null && port == null && base == null
                && utilisateur == null && motDePasse == null) {
            return null;
        }

        if (url == null || url.isBlank()) {
            if (hote == null || hote.isBlank() || base == null || base.isBlank()) {
                throw new SQLException("Définir DB_URL ou DB_HOST et DB_NAME.");
            }
            if (port == null || port.isBlank()) {
                port = "5432";
            }
            url = "jdbc:postgresql://" + hote + ":" + port + "/" + base;
        }

        Properties proprietes = new Properties();
        proprietes.setProperty("db.url", url);
        if (utilisateur != null) {
            proprietes.setProperty("db.user", utilisateur);
        }
        if (motDePasse != null) {
            proprietes.setProperty("db.password", motDePasse);
        }
        return proprietes;
    }

    private static Properties depuisFichier() throws SQLException {
        Path fichier = Path.of(System.getProperty("user.home"),
                ".salledereunion", "db.properties");
        Properties proprietes = new Properties();
        try (InputStream entree = Files.newInputStream(fichier)) {
            proprietes.load(entree);
        } catch (IOException erreur) {
            throw new SQLException(
                    "Impossible de lire la configuration PostgreSQL : " + fichier, erreur);
        }
        return proprietes;
    }

    public static Connection obtenirConnexion() throws SQLException {
        Properties proprietes = charger();
        return DriverManager.getConnection(
                proprietes.getProperty("db.url"),
                proprietes.getProperty("db.user"),
                proprietes.getProperty("db.password"));
    }
}
