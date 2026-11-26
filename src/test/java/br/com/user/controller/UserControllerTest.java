package br.com.user.controller;

import br.com.user.handler.ControllerExceptionHandler;
import br.com.user.service.IUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final IUserService service = mock(IUserService.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new UserController(service))
                .setControllerAdvice(new ControllerExceptionHandler())
                .build();
    }

    @Test
    void shouldRejectGoogleLoginWithoutEmail() throws Exception {
        mvc.perform(post("/v1/user/google").contentType(APPLICATION_JSON)
                .content("{\"name\":\"Fulano\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectInvalidEmailInPatchPath() throws Exception {
        mvc.perform(patch("/v1/user/not-an-email").contentType(APPLICATION_JSON)
                .content("{\"name\":\"Novo nome\"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test
    void shouldKeepPartialUpdateWithoutEmailInBody() throws Exception {
        mvc.perform(patch("/v1/user/user@example.com").contentType(APPLICATION_JSON)
                .content("""
                        {"name":"Novo nome"}
                        """)).andExpect(status().isOk());
        verify(service).update(eq("user@example.com"), any());
    }
    @Test
    void shouldRejectBlankGoogleName() throws Exception {
        mvc.perform(post("/v1/user/google").contentType(APPLICATION_JSON).content("""
                {"name":"   ","email":"user@example.com"}
                """)).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void shouldRejectPatchNameLongerThanDatabaseColumn() throws Exception {
        mvc.perform(patch("/v1/user/user@example.com").contentType(APPLICATION_JSON)
                .content("""
                        {"name":"%s"}
                        """.formatted("x".repeat(257)))).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
