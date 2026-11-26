package br.com.user.communs;

import com.br.azevedo.utils.HashUtils;

public final class Constantes {
    private Constantes(){}

    public static final String AVATAR_BLANK = "avatar/avatar-blank.jpg";

    public static String sha256(String input) {
        return HashUtils.sha256(input);
    }
}
