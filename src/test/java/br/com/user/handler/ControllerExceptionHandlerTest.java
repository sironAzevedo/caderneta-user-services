package br.com.user.handler;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerExceptionHandlerTest {

    @Test
    void shouldUseHttp500InInternalErrorBody() {
        var error = new ControllerExceptionHandler().internalError(
                new RuntimeException("Falha interna"), new MockHttpServletRequest());

        assertEquals(500, error.getStatus());
    }
}
