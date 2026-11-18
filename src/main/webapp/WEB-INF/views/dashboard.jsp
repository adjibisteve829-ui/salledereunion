<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.time.LocalDateTime,bj.salledereunion.model.Notification,bj.salledereunion.model.Reservation,bj.salledereunion.model.Utilisateur,bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>Dashboard | SalleRéunion</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body>
<div class="app-shell">
    <%@ include file="fragments/sidebar.jspf" %>
    <main class="main-content">
        <header class="topbar">
            <div><p class="eyebrow">VOTRE ESPACE</p><h1>Dashboard</h1></div>
            <% Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur"); %>
            <div class="user-chip"><span class="avatar"><%= HtmlUtil.echapper(utilisateur.getNom().substring(0, 1).toUpperCase()) %></span>
                <span><strong><%= HtmlUtil.echapper(utilisateur.getNom()) %></strong><small><%= utilisateur.getRole().name() %></small></span></div>
        </header>
        <section class="welcome-panel">
            <div><p class="eyebrow">GESTION DES ESPACES</p><h2>Bonjour, <%= HtmlUtil.echapper(utilisateur.getNom()) %></h2>
                <p>Retrouvez en un coup d'œil l'activité de vos salles de réunion.</p></div>
            <% if (utilisateur.estAdministrateur()) { %>
                <a class="button button-light" href="<%= request.getContextPath() %>/admin#reservations">Gérer les réservations clients <span aria-hidden="true">→</span></a>
            <% } else { %>
                <a class="button button-light" href="<%= request.getContextPath() %>/reservations">Nouvelle réservation <span aria-hidden="true">→</span></a>
            <% } %>
        </section>
        <section class="stats-grid" aria-label="Indicateurs">
            <article class="stat-card"><span class="stat-icon">▦</span><span class="stat-label">Salles disponibles</span>
                <strong><%= request.getAttribute("nombreSalles") %></strong><small>espaces enregistrés</small></article>
            <article class="stat-card"><span class="stat-icon">◷</span><span class="stat-label">Réservations en cours</span>
                <strong><%= request.getAttribute("reservationsActives") %></strong><small>à l'instant</small></article>
            <article class="stat-card"><span class="stat-icon">◷</span><span class="stat-label"><%= utilisateur.estAdministrateur() ? "Réservations clients à venir" : "Mes réservations à venir" %></span>
                <strong><%= request.getAttribute("nombreMesReservations") %></strong><small>créneaux réservés</small></article>
        </section>
        <% if (!utilisateur.estAdministrateur()) { %><section class="content-card">
            <div class="section-heading"><div><p class="eyebrow">MON PLANNING</p><h2>Prochaines réservations</h2></div>
                <a class="text-link" href="<%= request.getContextPath() %>/reservations">Voir toutes mes réservations</a></div>
            <% List<Reservation> mesReservations = (List<Reservation>) request.getAttribute("mesReservations");
               if (mesReservations.isEmpty()) { %>
                <p class="empty-state">Vous n'avez pas encore de réservation. <a class="text-link" href="<%= request.getContextPath() %>/reservations">Réserver une salle</a></p>
            <% } else { %>
                <div class="table-wrap"><table><thead><tr><th>Salle</th><th>Début</th><th>Fin</th><th>Statut</th></tr></thead><tbody>
                <% for (Reservation reservation : mesReservations) { %>
                    <tr><td><%= HtmlUtil.echapper(reservation.getSalle().getNom()) %></td>
                        <td><%= reservation.getDateDebut().toString().replace('T', ' ') %></td>
                        <td><%= reservation.getDateFin().toString().replace('T', ' ') %></td>
                        <td><span class="status-pill"><%= reservation.getStatut().name() %></span></td></tr>
                <% } %>
                </tbody></table></div>
            <% } %>
        </section><% } %>
        <section class="content-card">
            <div class="section-heading"><div><p class="eyebrow">CENTRE DE MESSAGES</p><h2>Notifications récentes</h2></div></div>
            <% List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
               if (notifications.isEmpty()) { %>
                <p class="empty-state">Vous n'avez aucune notification pour le moment.</p>
            <% } else { %>
                <div class="notification-list">
                <% for (Notification notification : notifications) { %>
                    <article class="notification-row"><span class="notification-dot"></span>
                        <div><p><%= HtmlUtil.echapper(notification.getMessage()) %></p>
                            <small>Délai : <%= notification.getDelaiMinutes() %> min · <%= notification.getDateEnvoi().toString().replace('T', ' ') %></small></div>
                    </article>
                <% } %>
                </div>
            <% } %>
        </section>
    </main>
</div>
</body>
</html>
