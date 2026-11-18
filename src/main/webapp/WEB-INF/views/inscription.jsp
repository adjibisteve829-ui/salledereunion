<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>Créer un compte | SalleRéunion</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="login-page">
    <main class="login-card">
        <a class="brand login-brand" href="<%= request.getContextPath() %>/login">Salle<span>Reunion</span></a>
        <p class="eyebrow">NOUVEAU CLIENT</p>
        <h1>Créer votre compte</h1>
        <p class="muted">Inscrivez-vous pour consulter les salles et gérer vos réservations.</p>
        <% if (request.getAttribute("erreur") != null) { %>
            <div class="alert alert-error" role="alert"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div>
        <% } %>
        <form action="<%= request.getContextPath() %>/inscription" method="post" class="form-stack">
            <input type="hidden" name="csrf" value="<%= session.getAttribute("inscriptionCsrfToken") %>">
            <label for="nom">Nom complet</label>
            <input id="nom" name="nom" maxlength="160" autocomplete="name" required
                   value="<%= HtmlUtil.echapper((String) request.getAttribute("nomSaisi")) %>">
            <label for="email">Adresse e-mail</label>
            <input id="email" name="email" type="email" maxlength="254" autocomplete="email" required
                   value="<%= HtmlUtil.echapper((String) request.getAttribute("emailSaisi")) %>">
            <label for="motDePasse">Mot de passe (12 caractères minimum)</label>
            <input id="motDePasse" name="motDePasse" type="password" minlength="12" maxlength="200"
                   autocomplete="new-password" required>
            <label for="confirmation">Confirmer le mot de passe</label>
            <input id="confirmation" name="confirmation" type="password" minlength="12" maxlength="200"
                   autocomplete="new-password" required>
            <button class="button button-primary button-wide" type="submit">Créer mon compte</button>
        </form>
        <p class="auth-switch">Vous avez déjà un compte ? <a href="<%= request.getContextPath() %>/login">Se connecter</a></p>
    </main>
</body>
</html>
