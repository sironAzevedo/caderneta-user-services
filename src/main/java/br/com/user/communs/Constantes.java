package br.com.user.communs;

import com.br.azevedo.utils.HashUtils;

public final class Constantes {
    private Constantes(){}

    public static final String URL_AVATAR = "https://caderneta-service-image-prod.s3.us-east-1.amazonaws.com/";
    public static final String AVATAR_BLANK = "avatar/avatar-blank.jpg";
    public static final String PHOTO_DEFAULT = URL_AVATAR.concat(AVATAR_BLANK);

    public static String sha256(String input) {
        return HashUtils.sha256(input);
    }
}
