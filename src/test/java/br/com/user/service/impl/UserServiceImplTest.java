package br.com.user.service.impl;

import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.FileReference;
import br.com.user.repository.IRoleRepository;
import br.com.user.repository.IUserRepository;
import br.com.user.service.CloudStorageProvider;
import com.br.azevedo.exception.UserException;
import com.br.azevedo.model.dto.UserDTO;
import com.br.azevedo.model.enums.PerfilEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IRoleRepository roleRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private CloudStorageProvider cloudStorageProvider;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso quando e-mail não existir")
    void shouldCreateUserSuccessfully() {
        UserDTO dto = new UserDTO();
        dto.setName("Fulano");
        dto.setEmail("fulano@example.com");
        dto.setPassword("senha123");
        dto.setTermsAgreed(true);

        when(userRepository.existsByEmail("fulano@example.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        Role userRole = new Role();
        userRole.setName(PerfilEnum.ROLE_USER);
        when(roleRepository.findByName(PerfilEnum.ROLE_USER)).thenReturn(List.of(userRole));

        userService.create(dto);

        verify(userRepository).save(argThat(user ->
                "Fulano".equals(user.getName()) &&
                "fulano@example.com".equals(user.getEmail()) &&
                "encodedPassword".equals(user.getPassword()) &&
                "LOCAL".equals(user.getProvider()) &&
                UserStatusEnum.ACTIVE.equals(user.getStatus()) &&
                Boolean.TRUE.equals(user.getTermsAgreed())
        ));
    }

    @Test
    @DisplayName("Deve lançar UserException ao tentar cadastrar usuário com e-mail duplicado")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        UserDTO dto = new UserDTO();
        dto.setEmail("existente@example.com");

        when(userRepository.existsByEmail("existente@example.com")).thenReturn(true);

        assertThrows(UserException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar usuário por e-mail e gerar links de avatar")
    void shouldFindUserByEmail() throws MalformedURLException {
        User user = User.builder()
                .id(1L)
                .name("Ciclano")
                .email("ciclano@example.com")
                .photo("avatar/avatar-blank.jpg")
                .status(UserStatusEnum.ACTIVE)
                .build();

        when(userRepository.findByEmail("ciclano@example.com")).thenReturn(Optional.of(user));
        when(cloudStorageProvider.generatePresignedUploadUrl(any(FileReference.class)))
                .thenReturn(new URL("https://s3.amazonaws.com/caderneta-service-image/avatar/upload"));
        when(cloudStorageProvider.fileExists(anyString())).thenReturn(false);

        UserDTO result = userService.findByEmail("ciclano@example.com");

        assertNotNull(result);
        assertEquals("Ciclano", result.getName());
        assertEquals("ciclano@example.com", result.getEmail());
        assertNotNull(result.getPhotoUploaded());
    }

    @Test
    @DisplayName("Deve atualizar usuário com sucesso")
    void shouldUpdateUser() {
        User user = User.builder()
                .id(1L)
                .name("Nome Antigo")
                .email("update@example.com")
                .build();

        when(userRepository.findByEmail("update@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO updateDto = new UserDTO();
        updateDto.setName("Nome Novo");

        UserDTO updated = userService.update("update@example.com", updateDto);

        assertNotNull(updated);
        assertEquals("Nome Novo", updated.getName());
        verify(userRepository).save(argThat(u -> "Nome Novo".equals(u.getName())));
    }
}
