package bj.salledereunion.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.io.Console;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {
    private static final int ITERATIONS = 210_000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hacher(char[] motDePasse) throws GeneralSecurityException {
        byte[] sel = new byte[16];
        RANDOM.nextBytes(sel);
        byte[] hachage = derive(motDePasse, sel);
        return "pbkdf2$" + ITERATIONS + "$" + Base64.getEncoder().encodeToString(sel)
                + "$" + Base64.getEncoder().encodeToString(hachage);
    }

    public static boolean verifier(char[] motDePasse, String valeurStockee)
            throws GeneralSecurityException {
        if (valeurStockee == null) {
            return false;
        }
        String[] parties = valeurStockee.split("\\$", -1);
        if (parties.length != 4 || !"pbkdf2".equals(parties[0])) {
            return false;
        }
        try {
            int iterations = Integer.parseInt(parties[1]);
            if (iterations < 100_000 || iterations > 1_000_000) {
                return false;
            }
            byte[] sel = Base64.getDecoder().decode(parties[2]);
            byte[] attendu = Base64.getDecoder().decode(parties[3]);
            return MessageDigest.isEqual(attendu, derive(motDePasse, sel, iterations));
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private static byte[] derive(char[] motDePasse, byte[] sel) throws GeneralSecurityException {
        return derive(motDePasse, sel, ITERATIONS);
    }

    private static byte[] derive(char[] motDePasse, byte[] sel, int iterations)
            throws GeneralSecurityException {
        PBEKeySpec specification = new PBEKeySpec(motDePasse, sel, iterations, KEY_LENGTH);
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(specification).getEncoded();
        } finally {
            specification.clearPassword();
        }
    }

    public static void main(String[] arguments) throws GeneralSecurityException {
        Console console = System.console();
        if (console == null) {
            throw new IllegalStateException("Lancez cette commande depuis un terminal interactif.");
        }
        char[] secret = console.readPassword("Mot de passe initial : ");
        if (secret == null || secret.length < 12) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 12 caractères.");
        }
        try {
            System.out.println(hacher(secret));
        } finally {
            java.util.Arrays.fill(secret, '\0');
        }
    }
}
