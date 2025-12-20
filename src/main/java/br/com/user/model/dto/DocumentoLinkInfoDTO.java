package br.com.user.model.dto;

import java.time.LocalDateTime;

public record DocumentoLinkInfoDTO(
        LocalDateTime dataExpiracao,
        boolean isExpirate) {
}
