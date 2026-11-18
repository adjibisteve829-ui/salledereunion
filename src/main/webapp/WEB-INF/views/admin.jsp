<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.time.format.DateTimeFormatter,bj.salledereunion.model.Utilisateur,bj.salledereunion.model.Salle,bj.salledereunion.model.Reservation,bj.salledereunion.model.Role,bj.salledereunion.model.StatutReservation,bj.salledereunion.model.ReglesPreemption,bj.salledereunion.util.HtmlUtil" %>
<%
    String csrf = (String) session.getAttribute("adminCsrfToken");
    Utilisateur utilisateurModifie = (Utilisateur) request.getAttribute("utilisateurModifie");
    Salle salleModifiee = (Salle) request.getAttribute("salleModifiee");
    Reservation reservationModifiee = (Reservation) request.getAttribute("reservationModifiee");
    List<Utilisateur> utilisateurs = (List<Utilisateur>) request.getAttribute("utilisateurs");
    List<Salle> salles = (List<Salle>) request.getAttribute("salles");
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    ReglesPreemption regles = (ReglesPreemption) request.getAttribute("regles");
    DateTimeFormatter dateLocale = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
%>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"><title>Administration | SalleRéunion</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css"></head>
<body><div class="app-shell"><%@ include file="fragments/sidebar.jspf" %><main class="main-content">
    <header class="topbar"><div><p class="eyebrow">ESPACE ADMINISTRATEUR</p><h1>Administration</h1></div></header>
    <% if (request.getAttribute("erreur") != null) { %><div class="alert alert-error"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div><% } %>
    <% if ("1".equals(request.getParameter("success"))) { %><div class="alert alert-success">Les modifications ont été enregistrées.</div><% } %>
    <nav class="admin-tabs" aria-label="Sections d'administration">
        <a href="#utilisateurs">Utilisateurs</a><a href="#salles">Salles</a>
        <a href="#reservations">Réservations</a><a href="#regles">Paramètres métier</a>
    </nav>

    <section class="content-card" id="utilisateurs">
        <div class="section-heading"><div><p class="eyebrow">COMPTES</p><h2>Gestion des utilisateurs</h2></div></div>
        <% if (utilisateurModifie != null) { %>
        <h3>Modifier le compte de <%= HtmlUtil.echapper(utilisateurModifie.getNom()) %></h3>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="modifierUtilisateur"><input type="hidden" name="id" value="<%= utilisateurModifie.getId() %>">
            <label>Nom<input name="nom" maxlength="160" required value="<%= HtmlUtil.echapper(utilisateurModifie.getNom()) %>"></label>
            <label>E-mail<input name="email" type="email" maxlength="254" required value="<%= HtmlUtil.echapper(utilisateurModifie.getEmail()) %>"></label>
            <label>Nouveau mot de passe<input name="motDePasse" type="password" minlength="12" maxlength="200" autocomplete="new-password" placeholder="Laisser vide pour conserver"></label>
            <label>Rôle<select name="role" required><% for (Role role : Role.values()) { %><option value="<%= role.name() %>" <%= utilisateurModifie.getRole() == role ? "selected" : "" %>><%= role.name() %></option><% } %></select></label>
            <label>Niveau hiérarchique<input name="niveau" type="number" min="0" required value="<%= utilisateurModifie.getNiveauHierarchique() %>"></label>
            <button class="button button-primary" type="submit">Enregistrer le compte</button><a class="text-link" href="<%= request.getContextPath() %>/admin#utilisateurs">Annuler</a>
        </form>
        <% } else { %>
        <h3>Créer un utilisateur</h3>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="creerUtilisateur">
            <label>Nom<input name="nom" maxlength="160" required></label>
            <label>E-mail<input name="email" type="email" maxlength="254" required></label>
            <label>Mot de passe<input name="motDePasse" type="password" minlength="12" maxlength="200" autocomplete="new-password" required></label>
            <label>Rôle<select name="role" required><% for (Role role : Role.values()) { %><option value="<%= role.name() %>"><%= role.name() %></option><% } %></select></label>
            <label>Niveau hiérarchique<input name="niveau" type="number" min="0" value="0" required></label>
            <button class="button button-primary" type="submit">Créer le compte</button>
        </form>
        <% } %>
        <div class="table-wrap"><table><thead><tr><th>Nom</th><th>E-mail</th><th>Rôle</th><th>Niveau</th><th>Actions</th></tr></thead><tbody>
        <% for (Utilisateur compte : utilisateurs) { %>
            <tr><td><%= HtmlUtil.echapper(compte.getNom()) %></td><td><%= HtmlUtil.echapper(compte.getEmail()) %></td>
                <td><span class="status-pill"><%= compte.getRole().name() %></span></td><td><%= compte.getNiveauHierarchique() %></td>
                <td class="admin-actions"><a class="text-link" href="<%= request.getContextPath() %>/admin?modifierUtilisateur=<%= compte.getId() %>#utilisateurs">Modifier</a>
                    <form action="<%= request.getContextPath() %>/admin" method="post" onsubmit="return confirm('Supprimer ce compte ?');">
                        <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="supprimerUtilisateur"><input type="hidden" name="id" value="<%= compte.getId() %>">
                        <button class="button button-danger" type="submit">Supprimer</button>
                    </form></td></tr>
        <% } if (utilisateurs.isEmpty()) { %><tr><td colspan="5">Aucun utilisateur enregistré.</td></tr><% } %>
        </tbody></table></div>
    </section>

    <section class="content-card" id="salles">
        <div class="section-heading"><div><p class="eyebrow">ESPACES</p><h2>Gestion des salles</h2></div></div>
        <% if (salleModifiee != null) { %>
        <h3>Modifier <%= HtmlUtil.echapper(salleModifiee.getNom()) %></h3>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="modifierSalle"><input type="hidden" name="id" value="<%= salleModifiee.getId() %>">
            <label>Nom<input name="nom" maxlength="120" required value="<%= HtmlUtil.echapper(salleModifiee.getNom()) %>"></label>
            <label>Capacité<input name="capacite" type="number" min="1" required value="<%= salleModifiee.getCapacite() %>"></label>
            <label>Équipements<textarea name="equipements" maxlength="2000" rows="2"><%= HtmlUtil.echapper(salleModifiee.getEquipements()) %></textarea></label>
            <button class="button button-primary" type="submit">Enregistrer la salle</button><a class="text-link" href="<%= request.getContextPath() %>/admin#salles">Annuler</a>
        </form>
        <% } else { %>
        <h3>Créer une salle</h3>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="creerSalle">
            <label>Nom<input name="nom" maxlength="120" required></label>
            <label>Capacité<input name="capacite" type="number" min="1" required></label>
            <label>Équipements<textarea name="equipements" maxlength="2000" rows="2"></textarea></label>
            <button class="button button-primary" type="submit">Créer la salle</button>
        </form>
        <% } %>
        <div class="table-wrap"><table><thead><tr><th>Salle</th><th>Capacité</th><th>Équipements</th><th>Statut</th><th>Actions</th></tr></thead><tbody>
        <% for (Salle salle : salles) { %>
            <tr><td><%= HtmlUtil.echapper(salle.getNom()) %></td><td><%= salle.getCapacite() %></td>
                <td><%= HtmlUtil.echapper(salle.getEquipements()) %></td><td><%= salle.getStatut() == bj.salledereunion.model.StatutSalle.RESERVEE ? "Réservée" : salle.getStatut() == bj.salledereunion.model.StatutSalle.OCCUPEE ? "Occupée" : "Disponible" %></td>
                <td class="admin-actions"><a class="text-link" href="<%= request.getContextPath() %>/admin?modifierSalle=<%= salle.getId() %>#salles">Modifier</a>
                    <form action="<%= request.getContextPath() %>/admin" method="post" onsubmit="return confirm('Supprimer cette salle ?');">
                        <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="supprimerSalle"><input type="hidden" name="id" value="<%= salle.getId() %>">
                        <button class="button button-danger" type="submit">Supprimer</button>
                    </form></td></tr>
        <% } if (salles.isEmpty()) { %><tr><td colspan="5">Aucune salle configurée.</td></tr><% } %>
        </tbody></table></div>
    </section>

    <section class="content-card" id="reservations">
        <div class="section-heading"><div><p class="eyebrow">PLANNING CLIENT</p><h2>Réservations des clients</h2></div></div>
        <p class="muted">Les réservations des comptes clients et autorités apparaissent ici. Les comptes administrateur ne réservent pas de salle.</p>
        <% if (reservationModifiee != null) { %>
        <h3>Modifier la réservation #<%= reservationModifiee.getId() %></h3>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="modifierReservation"><input type="hidden" name="id" value="<%= reservationModifiee.getId() %>">
            <label>Salle<select name="salleId" required><% for (Salle salle : salles) { %><option value="<%= salle.getId() %>" <%= reservationModifiee.getSalleId() == salle.getId() ? "selected" : "" %>><%= HtmlUtil.echapper(salle.getNom()) %></option><% } %></select></label>
            <label>Utilisateur<select name="utilisateurId" required><% for (Utilisateur compte : utilisateurs) { if (compte.getRole() != Role.ADMINISTRATEUR) { %><option value="<%= compte.getId() %>" <%= reservationModifiee.getUtilisateurId() == compte.getId() ? "selected" : "" %>><%= HtmlUtil.echapper(compte.getNom()) %> · <%= HtmlUtil.echapper(compte.getEmail()) %></option><% } } %></select></label>
            <label>Début<input name="dateDebut" type="datetime-local" required value="<%= reservationModifiee.getDateDebut().format(dateLocale) %>"></label>
            <label>Fin<input name="dateFin" type="datetime-local" required value="<%= reservationModifiee.getDateFin().format(dateLocale) %>"></label>
            <label>Motif<textarea name="motif" maxlength="500" required rows="2"><%= HtmlUtil.echapper(reservationModifiee.getMotif()) %></textarea></label>
            <button class="button button-primary" type="submit">Enregistrer la réservation</button><a class="text-link" href="<%= request.getContextPath() %>/admin#reservations">Annuler</a>
        </form>
        <% } %>
        <div class="table-wrap"><table><thead><tr><th>Salle</th><th>Utilisateur</th><th>Début</th><th>Fin</th><th>Motif</th><th>Statut</th><th>Actions</th></tr></thead><tbody>
        <% for (Reservation reservation : reservations) { %>
            <tr><td><%= HtmlUtil.echapper(reservation.getSalle().getNom()) %></td><td><%= HtmlUtil.echapper(reservation.getUtilisateur().getNom()) %></td>
                <td><%= reservation.getDateDebut().toString().replace('T', ' ') %></td><td><%= reservation.getDateFin().toString().replace('T', ' ') %></td>
                <td><%= HtmlUtil.echapper(reservation.getMotif()) %></td><td><span class="status-pill"><%= reservation.getStatut().name() %></span></td>
                <td class="admin-actions"><% if (reservation.getStatut() != StatutReservation.ANNULEE && reservation.getStatut() != StatutReservation.LIBEREE) { %>
                    <a class="text-link" href="<%= request.getContextPath() %>/admin?modifierReservation=<%= reservation.getId() %>#reservations">Modifier</a>
                    <form action="<%= request.getContextPath() %>/admin" method="post" onsubmit="return confirm('Annuler cette réservation ?');">
                        <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="annulerReservation"><input type="hidden" name="id" value="<%= reservation.getId() %>">
                        <button class="button button-danger" type="submit">Annuler</button>
                    </form><% } %></td></tr>
        <% } if (reservations.isEmpty()) { %><tr><td colspan="7">Aucune réservation enregistrée.</td></tr><% } %>
        </tbody></table></div>
        <p class="muted">Les réservations annulées restent conservées pour préserver l'historique.</p>
    </section>

    <section class="content-card" id="regles">
        <div class="section-heading"><div><p class="eyebrow">RÈGLES MÉTIER</p><h2>Paramètres de préemption</h2></div></div>
        <form action="<%= request.getContextPath() %>/admin" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= csrf %>"><input type="hidden" name="action" value="modifierRegles">
            <label>Délai pour l'autorité de niveau maximal (minutes)<input name="delaiNiveauMax" type="number" min="1" max="1440" required value="<%= regles.delaiNiveauMax() %>"></label>
            <label>Délai pour les autres autorités (minutes)<input name="delaiAutres" type="number" min="1" max="1440" required value="<%= regles.delaiAutres() %>"></label>
            <label>Seuil de libération (minutes)<input name="seuilLiberation" type="number" min="1" max="1440" required value="<%= regles.seuilLiberation() %>"></label>
            <button class="button button-primary" type="submit">Enregistrer les paramètres</button>
        </form>
        <p class="muted">La modification des délais ne change pas les notifications déjà envoyées.</p>
    </section>
</main></div></body></html>
