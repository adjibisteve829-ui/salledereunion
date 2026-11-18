package bj.salledereunion.util;

public final class HtmlUtil {
    private HtmlUtil() {
    }

    public static String echapper(String valeur) {
        if (valeur == null) {
            return "";
        }
        return valeur.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
