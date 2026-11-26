package br.com.user.service.impl;

import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.FileReference;
import br.com.user.repository.IRoleRepository;
import br.com.user.repository.IUserRepository;
import br.com.user.service.CloudStorageProvider;
import br.com.user.service.CloudStorageProvider.FileLookupException;
import com.br.azevedo.exception.UserException;
import com.br.azevedo.model.dto.UserDTO;
import com.br.azevedo.model.enums.PerfilEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.exception.SdkClientException;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
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

    @Mock
    private ApplicationEventPublisher events;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        var profiles = new UserProfiles(userRepository, events);
        userService = new UserServiceImpl(userRepository, roleRepository, passwordEncoder, cloudStorageProvider,
                profiles, events);
        ReflectionTestUtils.setField(userService, "avatarBaseUrl", "https://test.invalid/");
    }

    @Test
    @DisplayName("Deve cadastrar usuário com sucesso quando e-mail não existir")
    void shouldCreateUserSuccessfully() {
        UserDTO dto = new UserDTO();
        dto.setName("Fulano");
        dto.setEmail("fulano@example.com");
        dto.setPassword("senha123");
        dto.setTermsAgreed(true);

        when(passwordEncoder.encode("senha123")).thenReturn("encodedPassword");
        Role userRole = new Role();
        userRole.setName(PerfilEnum.ROLE_USER);
        when(roleRepository.findByName(PerfilEnum.ROLE_USER)).thenReturn(List.of(userRole));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

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


        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(User.builder()
                .sistemas(java.util.Set.of(com.br.azevedo.model.enums.SistemaEnum.CADERNETA)).build()));

        assertThrows(UserException.class, () -> userService.create(dto));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve vincular novo sistema ao usuário existente quando ainda não possuir o sistema")
    void shouldEnrollNewSystemWhenUserAlreadyExistsForAnotherSystem() {
        UserDTO dto = new UserDTO();
        dto.setEmail("existente@example.com");
        dto.setSistemas(java.util.Set.of(com.br.azevedo.model.enums.SistemaEnum.MARKETPLACE));

        User existingUser = User.builder()
                .id(1L)
                .email("existente@example.com")
                .sistemas(new java.util.HashSet<>(java.util.Set.of(com.br.azevedo.model.enums.SistemaEnum.CADERNETA)))
                .build();

        when(userRepository.findByEmail("existente@example.com")).thenReturn(Optional.of(existingUser));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        userService.create(dto);

        verify(userRepository).save(argThat(user ->
                user.getSistemas().contains(com.br.azevedo.model.enums.SistemaEnum.CADERNETA) &&
                user.getSistemas().contains(com.br.azevedo.model.enums.SistemaEnum.MARKETPLACE)
        ));
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
    @Test
    void shouldPreserveLoginWhenS3IsUnavailable() {
        User user = User.builder().id(1L).name("Ciclano").email("ciclano@example.com")
                .photo("avatar/avatar-blank.jpg").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cloudStorageProvider.fileExists(anyString(), any(Duration.class))).thenThrow(
                new FileLookupException(SdkClientException.create("S3 indisponível")));

        assertEquals("https://test.invalid/avatar/avatar-blank.jpg", userService.login(user.getEmail()).getPhoto());
        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldHandleLegacyUserWithoutNameOrPhoto() {
        User user = User.builder().id(1L).email("legado@example.com").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDTO result = userService.findByEmail(user.getEmail());

        assertTrue(result.getPhoto().endsWith("avatar/avatar-blank.jpg"));
        assertNull(result.getPhotoUploaded());
        verifyNoInteractions(cloudStorageProvider);
    }
    @Test
    void leavesTimeToRespondBeforeTheAuthApiTimeout() {
        var user = User.builder().id(1L).name("Ciclano").email("timeout@example.com")
                .photo("avatar/avatar-blank.jpg").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        var s3 = mock(S3Client.class);
        when(s3.headObject(any(HeadObjectRequest.class))).thenThrow(SdkClientException.create("S3 indisponível"));
        var storage = new S3CloudStorageProviderImpl(null, s3);
        ReflectionTestUtils.setField(storage, "bucketName", "test");
        var users = new UserServiceImpl(userRepository, roleRepository, passwordEncoder, storage,
                new UserProfiles(userRepository, events), events);
        ReflectionTestUtils.setField(users, "avatarBaseUrl", "https://test.invalid/");

        assertEquals("https://test.invalid/avatar/avatar-blank.jpg", users.login(user.getEmail()).getPhoto());

        var request = ArgumentCaptor.forClass(HeadObjectRequest.class);
        verify(s3).headObject(request.capture());
        assertEquals(Duration.ofSeconds(2), request.getValue().overrideConfiguration()
                .orElseThrow().apiCallTimeout().orElseThrow());
    }
    @Test
    void usesConfiguredDefaultWhenStoredPlaceholderPointsToAnotherBucket() {
        var user = User.builder().id(1L).name("Ciclano").email("config@example.com")
                .photo("https://old.invalid/avatar/avatar-blank.jpg").build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertEquals("https://test.invalid/avatar/avatar-blank.jpg", userService.login(user.getEmail()).getPhoto());
    }
}
