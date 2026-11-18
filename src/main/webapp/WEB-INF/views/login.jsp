<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="bj.salledereunion.util.HtmlUtil" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <title>Connexion | SalleRéunion</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="login-page">
    <main class="login-card">
        <a class="brand login-brand" href="<%= request.getContextPath() %>/login">Salle<span>Reunion</span></a>
        <p class="eyebrow">ESPACE PROFESSIONNEL</p>
        <h1>Ravi de vous revoir</h1>
        <p class="muted">Connectez-vous pour gérer vos salles et réservations.</p>
        <% if ("1".equals(request.getParameter("inscription"))) { %>
            <div class="alert alert-success" role="status">Votre compte est créé. Vous pouvez maintenant vous connecter.</div>
        <% } %>
        <% if (request.getAttribute("erreur") != null) { %>
            <div class="alert alert-error" role="alert"><%= HtmlUtil.echapper((String) request.getAttribute("erreur")) %></div>
        <% } %>
        <form action="<%= request.getContextPath() %>/login" method="post" class="form-stack">
            <label for="email">Adresse e-mail</label>
            <input id="email" name="email" type="email" autocomplete="username" required>
            <label for="motDePasse">Mot de passe</label>
            <input id="motDePasse" name="motDePasse" type="password" autocomplete="current-password" required>
            <button class="button button-primary button-wide" type="submit">Se connecter</button>
        </form>
        <p class="auth-switch">Pas encore de compte ? <a href="<%= request.getContextPath() %>/inscription">Créer un compte</a></p>
    </main>
</body>
</html>
