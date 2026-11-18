<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,bj.salledereunion.model.Salle,bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"><title>Salles | SalleRéunion</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css"></head>
<body><div class="app-shell"><%@ include file="fragments/sidebar.jspf" %><main class="main-content">
    <% bj.salledereunion.model.Utilisateur utilisateur =
            (bj.salledereunion.model.Utilisateur) session.getAttribute("utilisateur"); %>
    <header class="topbar"><div><p class="eyebrow">ESPACES DE TRAVAIL</p><h1>Nos salles</h1></div>
        <% if (utilisateur.estAdministrateur()) { %><a class="button button-primary" href="<%= request.getContextPath() %>/admin#salles">Gérer les salles</a>
        <% } else { %><a class="button button-primary" href="<%= request.getContextPath() %>/reservations">Réserver une salle</a><% } %></header>
    <% if (request.getAttribute("erreur") != null) { %><div class="alert alert-error"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div><% } %>
    <section class="content-card">
        <div class="section-heading"><div><p class="eyebrow">DISPONIBILITÉS</p><h2>Rechercher un créneau</h2></div></div>
        <form action="<%= request.getContextPath() %>/salles" method="get" class="admin-form">
            <label>Début<input name="dateDebut" type="datetime-local" required value="<%= HtmlUtil.echapper((String) request.getAttribute("debutRecherche")) %>"></label>
            <label>Fin<input name="dateFin" type="datetime-local" required value="<%= HtmlUtil.echapper((String) request.getAttribute("finRecherche")) %>"></label>
            <button class="button button-primary" type="submit">Rechercher les salles libres</button>
        </form>
        <% if (request.getAttribute("sallesDisponibles") != null) {
            List<Salle> disponibles = (List<Salle>) request.getAttribute("sallesDisponibles"); %>
            <h3>Salles libres pour la période choisie</h3>
            <% if (disponibles.isEmpty()) { %><p class="empty-state">Aucune salle n'est libre sur cette période.</p><% } else { %>
                <div class="room-grid">
                <% for (Salle salle : disponibles) { %><article class="room-card">
                    <h3><%= HtmlUtil.echapper(salle.getNom()) %></h3><p><%= salle.getCapacite() %> personnes</p>
                    <p class="room-equipment"><%= HtmlUtil.echapper(salle.getEquipements()) %></p>
                    <% if (utilisateur.estAdministrateur()) { %><span class="muted">Réservation réservée aux comptes clients</span>
                    <% } else { %><a class="text-link" href="<%= request.getContextPath() %>/reservations?salleId=<%= salle.getId() %>&amp;dateDebut=<%= HtmlUtil.echapper((String) request.getAttribute("debutRecherche")) %>&amp;dateFin=<%= HtmlUtil.echapper((String) request.getAttribute("finRecherche")) %>">Réserver cette salle →</a><% } %>
                </article><% } %>
                </div>
            <% } %>
        <% } %>
    </section>
    <section class="room-grid">
        <% List<Salle> salles = (List<Salle>) request.getAttribute("salles");
           if (salles.isEmpty()) { %><p class="empty-state">Aucune salle n'est encore configurée.</p><% }
           for (Salle salle : salles) { %>
            <article class="room-card"><div class="room-card-top"><span class="room-icon">▦</span><span class="status-pill <%= salle.getStatut() == bj.salledereunion.model.StatutSalle.DISPONIBLE ? "status-available" : salle.getStatut() == bj.salledereunion.model.StatutSalle.RESERVEE ? "status-reserved" : "status-busy" %>"><%= salle.getStatut() == bj.salledereunion.model.StatutSalle.DISPONIBLE ? "Disponible" : salle.getStatut() == bj.salledereunion.model.StatutSalle.RESERVEE ? "Réservée" : "Occupée" %></span></div>
                <h2><%= HtmlUtil.echapper(salle.getNom()) %></h2><p><%= salle.getCapacite() %> personnes</p>
                <div class="room-equipment"><%= HtmlUtil.echapper(salle.getEquipements()) %></div>
                <a class="text-link" href="<%= request.getContextPath() %>/reservations">Voir les disponibilités →</a></article>
        <% } %>
    </section>
</main></div></body></html>
