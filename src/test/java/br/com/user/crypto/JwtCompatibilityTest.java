package br.com.user.crypto;

import com.br.azevedo.security.models.jwt.AppEntity;
import com.br.azevedo.security.models.jwt.TokenMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtCompatibilityTest {
    @Test
    void bibliotecaDeSegurancaDeveLerTokenComOJjwtEmpacotado() {
        String segredoDeTeste = "segredo-sintetico-para-teste-local-apenas";
        var assinatura = Keys.hmacShaKeyFor(segredoDeTeste.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .claim("type", "application")
                .claim("authApp", Map.of("clientId", "teste"))
                .signWith(assinatura)
                .compact();

        assertThat(TokenMapper.get(token, segredoDeTeste)).isInstanceOf(AppEntity.class);
    }
}
