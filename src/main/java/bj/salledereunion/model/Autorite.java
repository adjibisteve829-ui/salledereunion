package bj.salledereunion.model;

public final class Autorite extends Utilisateur {
    public Autorite(long id, String nom, String email, int niveauHierarchique) {
        super(id, nom, email, niveauHierarchique, Role.AUTORITE);
    }
}
