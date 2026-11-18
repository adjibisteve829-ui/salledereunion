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
        Path fichier = Path.of(System.getProperty("user.home"),
                ".salledereunion", "db.properties");
        Properties proprietes = new Properties();
        try (InputStream entree = Files.newInputStream(fichier)) {
            proprietes.load(entree);
        } catch (IOException erreur) {
            throw new SQLException(
                    "Impossible de lire la configuration PostgreSQL : " + fichier, erreur);
        }

        String url = proprietes.getProperty("db.url");
        String utilisateur = proprietes.getProperty("db.user");
        String motDePasse = proprietes.getProperty("db.password");
        if (url == null || url.isBlank() || utilisateur == null || utilisateur.isBlank()
                || motDePasse == null) {
            throw new SQLException(
                    "La configuration PostgreSQL doit définir db.url, db.user et db.password : "
                    + fichier);
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
