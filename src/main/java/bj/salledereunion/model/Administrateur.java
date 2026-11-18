package bj.salledereunion.model;

public final class Administrateur extends Utilisateur {
    public Administrateur(long id, String nom, String email, int niveauHierarchique) {
        super(id, nom, email, niveauHierarchique, Role.ADMINISTRATEUR);
    }
}
