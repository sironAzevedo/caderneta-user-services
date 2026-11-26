package br.com.user;

import br.com.user.model.User;
import br.com.user.model.Role;
import br.com.user.repository.IUserRepository;
import br.com.user.repository.IRoleRepository;
import br.com.user.service.CloudStorageProvider;
import br.com.user.service.IUserService;
import com.br.azevedo.model.dto.UserDTO;
import com.br.azevedo.model.enums.SistemaEnum;
import com.br.azevedo.model.enums.PerfilEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.List;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "database.url=jdbc:h2:mem:users;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;INIT=CREATE DOMAIN IF NOT EXISTS dominio_status AS VARCHAR(32)",
        "database.driver-class-name=org.h2.Driver",
        "database.user=sa",
        "database.pass=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "security.enabled=false",
        "cloud.aws.credentials.access-key=test",
        "cloud.aws.credentials.secret-key=test"
})
class ApplicationTests {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private IUserService users;

    @Autowired
    private IUserRepository repository;

    @Autowired
    private IRoleRepository roles;

    @Autowired
    private PlatformTransactionManager transactions;

    @MockBean
    private CloudStorageProvider storage;

    @SpyBean
    private RedisCacheManager redisCaches;

    private final ConcurrentMapCacheManager caches = new ConcurrentMapCacheManager();

    @BeforeEach
    void redirectCacheOperationsToMemory() {
        doAnswer(invocation -> caches.getCache(invocation.getArgument(0)))
                .when(redisCaches).getCache(anyString());
    }

    @Test
    void checksS3OutsideTheDatabaseTransaction() {
        var user = repository.save(User.builder().name("Ciclano").email("s3@example.com")
                .photo("avatar/avatar-blank.jpg").build());
        when(storage.fileExists(anyString(), any(Duration.class))).thenAnswer(invocation -> {
            assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isFalse();
            return false;
        });

        assertThat(users.login(user.getEmail()).getPhoto()).endsWith("avatar/avatar-blank.jpg");
    }

    @Test
    void invalidatesCacheByIdWhenEnrollmentAddsSystem() throws MalformedURLException {
        when(storage.generatePresignedUploadUrl(any())).thenReturn(new URL("https://test.invalid/avatar/upload"));
        var user = repository.save(User.builder().name("Ciclano").email("enrollment@example.com")
                .photo("avatar/known.jpg").sistemas(Set.of(SistemaEnum.CADERNETA)).build());
        assertThat(users.findById(user.getId()).getSistemas()).containsExactly(SistemaEnum.CADERNETA);
        var enrollment = new UserDTO();
        enrollment.setEmail(user.getEmail());
        enrollment.setSistemas(Set.of(SistemaEnum.MARKETPLACE));

        users.create(enrollment);

        assertThat(users.findById(user.getId()).getSistemas()).containsExactlyInAnyOrder(
                SistemaEnum.CADERNETA, SistemaEnum.MARKETPLACE);
    }

    @Test
    void keepsCacheWhenEnrollmentRollsBack() {
        var user = repository.save(User.builder().name("Ciclano").email("rollback@example.com")
                .photo("avatar/known.jpg").sistemas(Set.of(SistemaEnum.CADERNETA)).build());
        users.login(user.getEmail());
        var loginCache = caches.getCache("user_services_cliente_login_email");
        var cached = loginCache.get(user.getEmail());
        assertThat(cached).isNotNull();

        var enrollment = new UserDTO();
        enrollment.setEmail(user.getEmail());
        enrollment.setSistemas(Set.of(SistemaEnum.MARKETPLACE));
        new TransactionTemplate(transactions).executeWithoutResult(status -> {
            users.create(enrollment);
            assertThat(loginCache.get(user.getEmail())).isNotNull();
            status.setRollbackOnly();
        });

        assertThat(loginCache.get(user.getEmail()).get()).isSameAs(cached.get());
        assertThat(users.login(user.getEmail()).getSistemas()).containsExactly(SistemaEnum.CADERNETA);
    }

    @Test
    void avatarDiscoveryInvalidatesAllLookupsAndPersistsTheNewPhoto() throws MalformedURLException {
        var user = repository.save(User.builder().name("Ciclano").email("avatar@example.com")
                .photo("avatar/avatar-blank.jpg").build());
        when(storage.generatePresignedUploadUrl(any())).thenReturn(new URL("https://test.invalid/avatar/upload"));
        when(storage.fileExists(anyString(), any(Duration.class))).thenReturn(true);
        users.findById(user.getId());
        users.findByEmail(user.getEmail());

        var avatar = users.login(user.getEmail()).getPhoto();
        assertThat(avatar).doesNotEndWith("avatar-blank.jpg");
        assertThat(repository.findById(user.getId()).orElseThrow().getPhoto()).isEqualTo(avatar);
        assertThat(users.findById(user.getId()).getPhoto()).isEqualTo(avatar);
        assertThat(users.findByEmail(user.getEmail()).getPhoto()).isEqualTo(avatar);
        assertThat(users.login(user.getEmail()).getPhoto()).isEqualTo(avatar);
    }

    @Test
    void generatesFreshUploadUrlWhenUserProfileIsCached() throws MalformedURLException {
        var user = repository.save(User.builder().name("Ciclano").email("upload@example.com")
                .photo("avatar/known.jpg").build());
        var first = new URL("https://test.invalid/avatar/upload?signature=first");
        var second = new URL("https://test.invalid/avatar/upload?signature=second");
        when(storage.generatePresignedUploadUrl(any())).thenReturn(first, second);

        assertThat(users.findByEmail(user.getEmail()).getPhotoUploaded()).isEqualTo(first.toString());
        assertThat(users.findByEmail(user.getEmail()).getPhotoUploaded()).isEqualTo(second.toString());
        var cachedProfile = caches.getCache("user_services_cliente_por_email").get(user.getEmail(), UserDTO.class);
        assertThat(cachedProfile.getPhotoUploaded()).isNull();
    }

    @Test
    void returnsEachRoleOnceForUserEnrolledInTwoSystems() {
        var role = new Role();
        role.setName(PerfilEnum.ROLE_USER);
        roles.save(role);
        var user = repository.save(User.builder().name("Ciclano").email("roles@example.com")
                .photo("avatar/known.jpg").roles(List.of(role))
                .sistemas(Set.of(SistemaEnum.CADERNETA, SistemaEnum.MARKETPLACE)).build());

        assertThat(users.login(user.getEmail()).getPerfis()).containsExactly(PerfilEnum.ROLE_USER);
    }

    @Test
    void startsWithHikariDataSource() {
        assertThat(dataSource).isInstanceOf(com.zaxxer.hikari.HikariDataSource.class);
    }
}
