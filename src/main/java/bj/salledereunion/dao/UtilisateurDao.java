package bj.salledereunion.dao;

import bj.salledereunion.model.Role;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.util.DatabaseConfiguration;
import bj.salledereunion.util.PasswordUtil;
import bj.salledereunion.util.PersistenceUtil;
import java.security.GeneralSecurityException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.List;

public class UtilisateurDao {
    public Utilisateur authentifier(String email, char[] motDePasse)
            throws GeneralSecurityException, SQLException {
        String sql = "SELECT id, nom, email, mot_de_passe, niveau_hierarchique, role "
                + "FROM utilisateur WHERE lower(email) = lower(?) LIMIT 1";
        try (Connection connexion = DatabaseConfiguration.obtenirConnexion();
                PreparedStatement requete = connexion.prepareStatement(sql)) {
            requete.setString(1, email);
            try (ResultSet resultat = requete.executeQuery()) {
                if (!resultat.next()
                        || !PasswordUtil.verifier(motDePasse, resultat.getString("mot_de_passe"))) {
                    return null;
                }
                return new Utilisateur(
                        resultat.getLong("id"),
                        resultat.getString("nom"),
                        resultat.getString("email"),
                        resultat.getInt("niveau_hierarchique"),
                        Role.valueOf(resultat.getString("role")));
            }
        }
    }

    public List<Utilisateur> lister() {
        return PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT u FROM Utilisateur u ORDER BY u.nom", Utilisateur.class)
                .getResultList());
    }

    public Utilisateur trouver(long id) {
        return PersistenceUtil.executer(entityManager -> entityManager.find(Utilisateur.class, id));
    }

    public void inscrire(String nom, String email, char[] motDePasse) {
        String hash;
        try {
            hash = PasswordUtil.hacher(motDePasse);
        } catch (GeneralSecurityException erreur) {
            throw new IllegalStateException("Impossible de sécuriser le mot de passe.", erreur);
        }
        PersistenceUtil.executerTransaction(entityManager -> {
            Long comptes = entityManager.createQuery(
                    "SELECT count(u) FROM Utilisateur u WHERE lower(u.email) = :email",
                    Long.class)
                    .setParameter("email", email.toLowerCase(Locale.ROOT))
                    .getSingleResult();
            if (comptes > 0) {
                throw new IllegalArgumentException(
                        "Cette adresse e-mail est déjà associée à un compte.");
            }
            entityManager.persist(new Utilisateur(nom, email, hash, 0, Role.UTILISATEUR));
            return null;
        });
    }

    public void creer(String nom, String email, String motDePasse,
            int niveauHierarchique, Role role) {
        String hash = hacher(motDePasse);
        PersistenceUtil.executerTransaction(entityManager -> {
            verifierEmailDisponible(entityManager, email, 0);
            entityManager.persist(new Utilisateur(nom, email, hash, niveauHierarchique, role));
            return null;
        });
    }

    public void modifier(long id, String nom, String email, String motDePasse,
            int niveauHierarchique, Role role) {
        String hash = null;
        if (motDePasse != null && !motDePasse.isBlank()) {
            hash = hacher(motDePasse);
        }
        String nouveauHash = hash;
        PersistenceUtil.executerTransaction(entityManager -> {
            Utilisateur utilisateur = entityManager.find(Utilisateur.class, id);
            if (utilisateur == null) {
                throw new IllegalArgumentException("Cet utilisateur n'existe pas.");
            }
            verifierEmailDisponible(entityManager, email, id);
            if (utilisateur.getRole() == Role.ADMINISTRATEUR
                    && role != Role.ADMINISTRATEUR) {
                Long administrateurs = entityManager.createQuery(
                        "SELECT count(u) FROM Utilisateur u WHERE u.role = :role",
                        Long.class).setParameter("role", Role.ADMINISTRATEUR).getSingleResult();
                if (administrateurs <= 1) {
                    throw new IllegalArgumentException(
                            "Le dernier compte administrateur doit rester administrateur.");
                }
            }
            utilisateur.setNom(nom);
            utilisateur.setEmail(email);
            utilisateur.setNiveauHierarchique(niveauHierarchique);
            utilisateur.setRole(role);
            if (nouveauHash != null) {
                utilisateur.setMotDePasse(nouveauHash);
            }
            return null;
        });
    }

    public void modifierProfil(long id, String nom, String email, String motDePasse) {
        String hash = motDePasse == null || motDePasse.isBlank() ? null : hacher(motDePasse);
        PersistenceUtil.executerTransaction(entityManager -> {
            Utilisateur utilisateur = entityManager.find(Utilisateur.class, id);
            if (utilisateur == null) {
                throw new IllegalArgumentException("Ce compte n'existe plus.");
            }
            verifierEmailDisponible(entityManager, email, id);
            utilisateur.setNom(nom);
            utilisateur.setEmail(email);
            if (hash != null) {
                utilisateur.setMotDePasse(hash);
            }
            return null;
        });
    }

    public void supprimer(long id, long administrateurId) {
        PersistenceUtil.executerTransaction(entityManager -> {
            if (id == administrateurId) {
                throw new IllegalArgumentException(
                        "Vous ne pouvez pas supprimer votre propre compte.");
            }
            Utilisateur utilisateur = entityManager.find(Utilisateur.class, id);
            if (utilisateur == null) {
                throw new IllegalArgumentException("Cet utilisateur n'existe pas.");
            }
            Long references = entityManager.createQuery(
                    "SELECT count(r) FROM Reservation r WHERE r.utilisateur.id = :id",
                    Long.class).setParameter("id", id).getSingleResult();
            Long notifications = entityManager.createQuery(
                    "SELECT count(n) FROM Notification n WHERE n.destinataire.id = :id",
                    Long.class).setParameter("id", id).getSingleResult();
            if (references > 0 || notifications > 0) {
                throw new IllegalArgumentException(
                        "Cet utilisateur est lié à des réservations ou notifications et ne peut pas être supprimé.");
            }
            if (utilisateur.getRole() == Role.ADMINISTRATEUR) {
                Long administrateurs = entityManager.createQuery(
                        "SELECT count(u) FROM Utilisateur u WHERE u.role = :role",
                        Long.class).setParameter("role", Role.ADMINISTRATEUR).getSingleResult();
                if (administrateurs <= 1) {
                    throw new IllegalArgumentException(
                            "Le dernier compte administrateur ne peut pas être supprimé.");
                }
            }
            entityManager.remove(utilisateur);
            return null;
        });
    }

    public int niveauHierarchiqueMaximum() {
        Long niveau = PersistenceUtil.executer(entityManager -> entityManager.createQuery(
                "SELECT coalesce(max(u.niveauHierarchique), 0) FROM Utilisateur u "
                        + "WHERE u.role = :role", Long.class)
                .setParameter("role", Role.AUTORITE)
                .getSingleResult());
        return niveau.intValue();
    }

    private static String hacher(String motDePasse) {
        try {
            return PasswordUtil.hacher(motDePasse.toCharArray());
        } catch (GeneralSecurityException erreur) {
            throw new IllegalStateException("Impossible de sécuriser le mot de passe.", erreur);
        }
    }

    private static void verifierEmailDisponible(
            jakarta.persistence.EntityManager entityManager, String email, long idExclu) {
        Long correspondances = entityManager.createQuery(
                "SELECT count(u) FROM Utilisateur u WHERE lower(u.email) = lower(:email) "
                        + "AND u.id <> :id", Long.class)
                .setParameter("email", email)
                .setParameter("id", idExclu)
                .getSingleResult();
        if (correspondances > 0) {
            throw new IllegalArgumentException("Cette adresse e-mail est déjà utilisée.");
        }
    }
}
