package br.com.user.communs;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Constantes {
    private Constantes(){}

    public static final String URL_AVATAR = "https://caderneta-service.s3.sa-east-1.amazonaws.com/";
    public static final String AVATAR_BLANK = "avatar/avatar-blank.jpg";
    public static final String PHOTO_DEFAULT = URL_AVATAR.concat(AVATAR_BLANK);

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash SHA-256", e);
        }
    }
}
