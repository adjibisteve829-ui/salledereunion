package bj.salledereunion.security;

import bj.salledereunion.model.Utilisateur;
import java.security.Principal;

public final class UtilisateurPrincipal implements Principal {
    private final Utilisateur utilisateur;

    public UtilisateurPrincipal(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    @Override
    public String getName() {
        return utilisateur.getEmail();
    }

    @Override
    public boolean equals(Object autre) {
        return autre instanceof UtilisateurPrincipal principal
                && getName().equals(principal.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }
}
