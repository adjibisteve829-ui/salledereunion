package bj.salledereunion.dao;

import bj.salledereunion.model.ReglesPreemption;
import bj.salledereunion.util.PersistenceUtil;
import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.Map;

public class ReglesPreemptionDao {
    private static final Map<String, Integer> VALEURS_DEFAUT = Map.of(
            "preemption.delai_niveau_max", ReglesPreemption.DELAI_NIVEAU_MAX_DEFAUT,
            "preemption.delai_autres", ReglesPreemption.DELAI_AUTRES_DEFAUT,
            "preemption.seuil_liberation", ReglesPreemption.SEUIL_LIBERATION_DEFAUT);

    public ReglesPreemption lire() {
        return PersistenceUtil.executer(ReglesPreemptionDao::lire);
    }

    public void enregistrer(ReglesPreemption regles) {
        PersistenceUtil.executerTransaction(entityManager -> {
            Map<String, Integer> valeurs = Map.of(
                    "preemption.delai_niveau_max", regles.delaiNiveauMax(),
                    "preemption.delai_autres", regles.delaiAutres(),
                    "preemption.seuil_liberation", regles.seuilLiberation());
            valeurs.forEach((cle, valeur) -> entityManager.createNativeQuery(
                    "INSERT INTO parametre_application (cle, valeur) VALUES (:cle, :valeur) "
                            + "ON CONFLICT (cle) DO UPDATE SET valeur = EXCLUDED.valeur")
                    .setParameter("cle", cle)
                    .setParameter("valeur", Integer.toString(valeur))
                    .executeUpdate());
            return null;
        });
    }

    public static ReglesPreemption lire(EntityManager entityManager) {
        Map<String, Integer> valeurs = new HashMap<>(VALEURS_DEFAUT);
        for (Object element : entityManager.createNativeQuery(
                "SELECT cle, valeur FROM parametre_application").getResultList()) {
            Object[] ligne = (Object[]) element;
            String cle = (String) ligne[0];
            if (VALEURS_DEFAUT.containsKey(cle)) {
                try {
                    valeurs.put(cle, Integer.parseInt((String) ligne[1]));
                } catch (NumberFormatException erreur) {
                    throw new IllegalStateException(
                            "La valeur du paramètre de préemption " + cle + " est invalide.",
                            erreur);
                }
            }
        }
        return new ReglesPreemption(
                valeurs.get("preemption.delai_niveau_max"),
                valeurs.get("preemption.delai_autres"),
                valeurs.get("preemption.seuil_liberation"));
    }
}
