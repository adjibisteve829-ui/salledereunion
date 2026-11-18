<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List,java.time.LocalDateTime,bj.salledereunion.model.Salle,bj.salledereunion.model.Reservation,bj.salledereunion.model.StatutReservation,bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"><title>Réservations | SalleRéunion</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css"></head>
<body><div class="app-shell"><%@ include file="fragments/sidebar.jspf" %><main class="main-content">
    <header class="topbar"><div><p class="eyebrow">PLANNING</p><h1>Réservations</h1></div></header>
    <% if (request.getAttribute("erreur") != null) { %><div class="alert alert-error"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div><% } %>
    <% if ("1".equals(request.getParameter("success"))) { %><div class="alert alert-success">Votre demande a été enregistrée.</div><% } %>
    <% if ("1".equals(request.getParameter("annulee"))) { %><div class="alert alert-success">Votre réservation a été annulée.</div><% } %>
    <section class="content-card">
        <div class="section-heading"><div><p class="eyebrow">NOUVELLE DEMANDE</p><h2>Réserver un créneau</h2></div></div>
        <form action="<%= request.getContextPath() %>/reservations" method="post" class="reservation-form">
            <input type="hidden" name="csrf" value="<%= session.getAttribute("reservationCsrfToken") %>">
            <label for="salleId">Salle</label><select id="salleId" name="salleId" required>
                <% for (Salle salle : (List<Salle>) request.getAttribute("salles")) { %>
                    <option value="<%= salle.getId() %>" <%= String.valueOf(salle.getId()).equals(request.getParameter("salleId")) ? "selected" : "" %>><%= HtmlUtil.echapper(salle.getNom()) %> · <%= salle.getCapacite() %> places</option>
                <% } %>
            </select>
            <label for="dateDebut">Début</label><input id="dateDebut" name="dateDebut" type="datetime-local" required value="<%= HtmlUtil.echapper(request.getParameter("dateDebut")) %>">
            <label for="dateFin">Fin</label><input id="dateFin" name="dateFin" type="datetime-local" required value="<%= HtmlUtil.echapper(request.getParameter("dateFin")) %>">
            <label for="motif">Motif</label><textarea id="motif" name="motif" maxlength="500" rows="3" required></textarea>
            <button class="button button-primary" type="submit">Envoyer la demande</button>
        </form>
    </section>
    <section class="content-card">
        <div class="section-heading"><div><p class="eyebrow">MON PLANNING</p><h2>Mes réservations à venir</h2></div></div>
        <div class="table-wrap"><table><thead><tr><th>Salle</th><th>Début</th><th>Fin</th><th>Motif</th><th>Statut</th><th>Action</th></tr></thead><tbody>
        <% List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
           if (reservations.isEmpty()) { %><tr><td colspan="6">Vous n'avez aucune réservation à venir.</td></tr><% }
           for (Reservation reservation : reservations) { %>
            <tr><td><%= HtmlUtil.echapper(reservation.getSalle().getNom()) %></td>
                <td><%= reservation.getDateDebut().toString().replace('T', ' ') %></td>
                <td><%= reservation.getDateFin().toString().replace('T', ' ') %></td>
                <td><%= HtmlUtil.echapper(reservation.getMotif()) %></td>
                <td><span class="status-pill"><%= reservation.getStatut().name() %></span></td>
                <td><% if (reservation.getDateDebut().isAfter(LocalDateTime.now())
                        && (reservation.getStatut() == StatutReservation.CONFIRMEE
                        || reservation.getStatut() == StatutReservation.REDUITE)) { %>
                    <form action="<%= request.getContextPath() %>/reservations" method="post" onsubmit="return confirm('Annuler cette réservation ?');">
                        <input type="hidden" name="csrf" value="<%= session.getAttribute("reservationCsrfToken") %>">
                        <input type="hidden" name="action" value="annuler">
                        <input type="hidden" name="reservationId" value="<%= reservation.getId() %>">
                        <button class="button button-danger" type="submit">Annuler</button>
                    </form><% } %></td></tr>
        <% } %>
        </tbody></table></div>
    </section>
</main></div></body></html>
