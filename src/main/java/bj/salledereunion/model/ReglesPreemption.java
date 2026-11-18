package bj.salledereunion.model;

public record ReglesPreemption(int delaiNiveauMax, int delaiAutres, int seuilLiberation) {
    public static final int DELAI_NIVEAU_MAX_DEFAUT = 60;
    public static final int DELAI_AUTRES_DEFAUT = 30;
    public static final int SEUIL_LIBERATION_DEFAUT = 60;

    public ReglesPreemption {
        verifierValeur(delaiNiveauMax);
        verifierValeur(delaiAutres);
        verifierValeur(seuilLiberation);
    }

    private static void verifierValeur(int valeur) {
        if (valeur < 1 || valeur > 1440) {
            throw new IllegalArgumentException(
                    "Les délais doivent être compris entre 1 et 1 440 minutes.");
        }
    }
}
