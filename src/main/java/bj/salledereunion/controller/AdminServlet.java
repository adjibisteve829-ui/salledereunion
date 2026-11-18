package bj.salledereunion.controller;

import bj.salledereunion.dao.ReservationDao;
import bj.salledereunion.dao.ReglesPreemptionDao;
import bj.salledereunion.dao.SalleDao;
import bj.salledereunion.dao.UtilisateurDao;
import bj.salledereunion.model.ReglesPreemption;
import bj.salledereunion.model.Role;
import bj.salledereunion.model.Utilisateur;
import bj.salledereunion.service.ReservationService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(AdminServlet.class.getName());
    private static final String JETON_CSRF = "adminCsrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            chargerPage(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Impossible de charger la page d'administration.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible de charger l'administration.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!jetonValide(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Le formulaire a expiré. Rechargez la page.");
            return;
        }
        try {
            switch (obligatoire(request, "action")) {
                case "creerUtilisateur" -> creerUtilisateur(request);
                case "modifierUtilisateur" -> modifierUtilisateur(request);
                case "supprimerUtilisateur" -> supprimerUtilisateur(request);
                case "creerSalle" -> creerSalle(request);
                case "modifierSalle" -> modifierSalle(request);
                case "supprimerSalle" -> new SalleDao().supprimer(nombre(request, "id"));
                case "modifierReservation" -> modifierReservation(request);
                case "annulerReservation" ->
                    new ReservationService().annulerPourAdmin(nombre(request, "id"));
                case "modifierRegles" -> modifierRegles(request);
                default -> throw new IllegalArgumentException("Action d'administration inconnue.");
            }
            response.sendRedirect(request.getContextPath() + "/admin?success=1");
        } catch (IllegalArgumentException exception) {
            request.setAttribute("erreur", exception.getMessage());
            try {
                chargerPage(request, response);
            } catch (RuntimeException erreur) {
                LOGGER.log(Level.SEVERE, "Impossible de réafficher l'administration.", erreur);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Impossible de recharger l'administration.");
            }
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Échec d'une opération d'administration.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Impossible d'enregistrer les modifications.");
        }
    }

    private void chargerPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UtilisateurDao utilisateurs = new UtilisateurDao();
        SalleDao salles = new SalleDao();
        new ReservationDao().actualiserStatutsSalles();
        request.setAttribute("utilisateurs", utilisateurs.lister());
        request.setAttribute("salles", salles.lister());
        request.setAttribute("reservations", new ReservationDao().listerPourAdministration());
        request.setAttribute("regles", new ReglesPreemptionDao().lire());
        request.setAttribute("utilisateurModifie",
                trouver(utilisateurs, request.getParameter("modifierUtilisateur")));
        request.setAttribute("salleModifiee",
                trouverSalle(salles, request.getParameter("modifierSalle")));
        request.setAttribute("reservationModifiee",
                trouverReservation(request.getParameter("modifierReservation")));
        if (request.getSession().getAttribute(JETON_CSRF) == null) {
            byte[] aleatoire = new byte[32];
            RANDOM.nextBytes(aleatoire);
            request.getSession().setAttribute(JETON_CSRF,
                    Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire));
        }
        request.getRequestDispatcher("/WEB-INF/views/admin.jsp").forward(request, response);
    }

    private Utilisateur trouver(UtilisateurDao dao, String valeur) {
        return valeur == null ? null : dao.trouver(lireId(valeur));
    }

    private bj.salledereunion.model.Salle trouverSalle(SalleDao dao, String valeur) {
        return valeur == null ? null : dao.trouver(lireId(valeur));
    }

    private bj.salledereunion.model.Reservation trouverReservation(String valeur) {
        if (valeur == null) {
            return null;
        }
        long id = lireId(valeur);
        return new ReservationDao().listerPourAdministration().stream()
                .filter(reservation -> reservation.getId() == id)
                .findFirst().orElseThrow(() ->
                        new IllegalArgumentException("Cette réservation n'existe pas."));
    }

    private void creerUtilisateur(HttpServletRequest request) {
        String nom = texte(request, "nom", 160);
        String email = email(request);
        String motDePasse = motDePasse(request, true);
        Role role = role(request);
        int niveau = niveau(request);
        new UtilisateurDao().creer(nom, email, motDePasse, niveau, role);
    }

    private void modifierUtilisateur(HttpServletRequest request) {
        long id = nombre(request, "id");
        String nom = texte(request, "nom", 160);
        String email = email(request);
        String motDePasse = motDePasse(request, false);
        Role role = role(request);
        int niveau = niveau(request);
        Utilisateur administrateur = (Utilisateur) request.getSession(false)
                .getAttribute("utilisateur");
        if (id == administrateur.getId() && role != Role.ADMINISTRATEUR) {
            throw new IllegalArgumentException(
                    "Vous ne pouvez pas retirer votre propre rôle administrateur.");
        }
        new UtilisateurDao().modifier(id, nom, email, motDePasse, niveau, role);
        if (id == administrateur.getId()) {
            request.getSession(false).setAttribute("utilisateur",
                    new Utilisateur(id, nom, email, niveau, role));
        }
    }

    private void supprimerUtilisateur(HttpServletRequest request) {
        Utilisateur administrateur = (Utilisateur) request.getSession(false)
                .getAttribute("utilisateur");
        new UtilisateurDao().supprimer(nombre(request, "id"), administrateur.getId());
    }

    private void creerSalle(HttpServletRequest request) {
        new SalleDao().creer(texte(request, "nom", 120), capacite(request),
                texte(request, "equipements", 2000, true));
    }

    private void modifierSalle(HttpServletRequest request) {
        new SalleDao().modifier(nombre(request, "id"), texte(request, "nom", 120),
                capacite(request), texte(request, "equipements", 2000, true));
    }

    private void modifierReservation(HttpServletRequest request) {
        new ReservationService().modifierPourAdmin(nombre(request, "id"),
                nombre(request, "salleId"), nombre(request, "utilisateurId"),
                date(request, "dateDebut"), date(request, "dateFin"),
                texte(request, "motif", 500));
    }

    private void modifierRegles(HttpServletRequest request) {
        try {
            new ReglesPreemptionDao().enregistrer(new ReglesPreemption(
                    Integer.parseInt(obligatoire(request, "delaiNiveauMax")),
                    Integer.parseInt(obligatoire(request, "delaiAutres")),
                    Integer.parseInt(obligatoire(request, "seuilLiberation"))));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Les délais doivent être des nombres entiers.");
        }
    }

    private boolean jetonValide(HttpServletRequest request) {
        Object attendu = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute(JETON_CSRF);
        String fourni = request.getParameter("csrf");
        return attendu instanceof String valeur && fourni != null
                && MessageDigest.isEqual(valeur.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                        fourni.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    private static String obligatoire(HttpServletRequest request, String nom) {
        String valeur = request.getParameter(nom);
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException("Le champ « " + nom + " » est obligatoire.");
        }
        return valeur.trim();
    }

    private static String texte(HttpServletRequest request, String nom, int maximum) {
        return texte(request, nom, maximum, false);
    }

    private static String texte(HttpServletRequest request, String nom, int maximum,
            boolean autoriserVide) {
        String valeur = request.getParameter(nom);
        valeur = valeur == null ? "" : valeur.trim();
        if ((!autoriserVide && valeur.isEmpty()) || valeur.length() > maximum) {
            throw new IllegalArgumentException("Vérifiez le champ « " + nom + " ».");
        }
        return valeur;
    }

    private static String email(HttpServletRequest request) {
        String email = texte(request, "email", 254);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Veuillez saisir une adresse e-mail valide.");
        }
        return email;
    }

    private static String motDePasse(HttpServletRequest request, boolean obligatoire) {
        String motDePasse = request.getParameter("motDePasse");
        motDePasse = motDePasse == null ? "" : motDePasse;
        if ((obligatoire || !motDePasse.isEmpty()) && motDePasse.length() < 12) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 12 caractères.");
        }
        if (motDePasse.length() > 200) {
            throw new IllegalArgumentException("Le mot de passe est trop long.");
        }
        return motDePasse;
    }

    private static Role role(HttpServletRequest request) {
        try {
            return Role.valueOf(obligatoire(request, "role"));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Le rôle sélectionné est invalide.");
        }
    }

    private static int niveau(HttpServletRequest request) {
        try {
            int niveau = Integer.parseInt(obligatoire(request, "niveau"));
            if (niveau < 0) {
                throw new NumberFormatException();
            }
            return niveau;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Le niveau hiérarchique doit être un entier positif ou nul.");
        }
    }

    private static int capacite(HttpServletRequest request) {
        try {
            int capacite = Integer.parseInt(obligatoire(request, "capacite"));
            if (capacite <= 0) {
                throw new NumberFormatException();
            }
            return capacite;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("La capacité doit être un entier supérieur à zéro.");
        }
    }

    private static long nombre(HttpServletRequest request, String nom) {
        return lireId(obligatoire(request, nom));
    }

    private static long lireId(String valeur) {
        try {
            long id = Long.parseLong(valeur);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("L'identifiant demandé est invalide.");
        }
    }

    private static LocalDateTime date(HttpServletRequest request, String nom) {
        try {
            return LocalDateTime.parse(obligatoire(request, nom));
        } catch (java.time.format.DateTimeParseException exception) {
            throw new IllegalArgumentException("La date de réservation est invalide.");
        }
    }
}
