<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="bj.salledereunion.model.Utilisateur,bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no"><title>Paramètres | SalleRéunion</title><link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css"></head>
<body><div class="app-shell"><%@ include file="fragments/sidebar.jspf" %><main class="main-content">
    <header class="topbar"><div><p class="eyebrow">VOTRE COMPTE</p><h1>Paramètres</h1></div></header>
    <% if (request.getAttribute("erreur") != null) { %><div class="alert alert-error"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div><% } %>
    <% if ("1".equals(request.getParameter("success"))) { %><div class="alert alert-success">Votre profil a été mis à jour.</div><% } %>
    <% Utilisateur utilisateur = (Utilisateur) session.getAttribute("utilisateur"); %>
    <section class="content-card profile-card"><p class="eyebrow">PROFIL</p><h2>Informations du compte</h2>
        <form action="<%= request.getContextPath() %>/parametres" method="post" class="admin-form">
            <input type="hidden" name="csrf" value="<%= session.getAttribute("parametresCsrfToken") %>">
            <label>Nom<input name="nom" maxlength="160" required value="<%= HtmlUtil.echapper(utilisateur.getNom()) %>"></label>
            <label>Adresse e-mail<input name="email" type="email" maxlength="254" required value="<%= HtmlUtil.echapper(utilisateur.getEmail()) %>"></label>
            <label>Nouveau mot de passe<input name="motDePasse" type="password" minlength="12" maxlength="200" autocomplete="new-password" placeholder="Laisser vide pour conserver"></label>
            <button class="button button-primary" type="submit">Enregistrer mon profil</button>
        </form>
        <dl class="profile-list">
            <dt>Rôle</dt><dd><%= utilisateur.getRole().name() %></dd>
            <dt>Niveau hiérarchique</dt><dd><%= utilisateur.getNiveauHierarchique() %></dd></dl>
    </section>
</main></div></body></html>
