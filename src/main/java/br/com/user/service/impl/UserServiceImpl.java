package br.com.user.service.impl;

import br.com.user.communs.Constantes;
import br.com.user.mapper.UserMapper;
import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.FileReference;
import br.com.user.repository.IRoleRepository;
import br.com.user.repository.IUserRepository;
import br.com.user.service.CloudStorageProvider;
import br.com.user.service.CloudStorageProvider.FileLookupException;
import br.com.user.service.IUserService;
import com.br.azevedo.exception.UserException;
import com.br.azevedo.model.dto.UserDTO;
import com.br.azevedo.model.enums.PerfilEnum;
import com.br.azevedo.model.enums.SistemaEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.com.user.communs.Constantes.AVATAR_BLANK;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

	// O cliente auth-api encerra a chamada de login após 5 segundos.
	private static final Duration LOGIN_AVATAR_TIMEOUT = Duration.ofSeconds(2);

	private final IUserRepository repo;
	private final IRoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CloudStorageProvider cloudStorageProvider;
	private final UserProfiles profiles;
	private final ApplicationEventPublisher events;

	@Value("${avatar.base-url}")
	private String avatarBaseUrl;
	
	@Override
	@Transactional
	public void create(UserDTO dto) {
		Set<SistemaEnum> requestedSistemas = resolveSistemas(dto);
		var existing = repo.findByEmail(dto.getEmail());
		if (existing.isPresent()) {
			User user = existing.get();
			if (user.getSistemas() == null) {
				user.setSistemas(new HashSet<>());
			}
			if (user.getSistemas().containsAll(requestedSistemas)) {
				throw new UserException("Este e-mail já existe");
			}
			user.getSistemas().addAll(requestedSistemas);
			user.setUpdatedAt(LocalDate.now());
			saveAndInvalidate(user);
			return;
		}

		User user = UserMapper.INSTANCE.toEntity(dto);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		user.setProvider("LOCAL");
		user.setTermsAgreed(Boolean.TRUE.equals(dto.getTermsAgreed()));
		List<Role> roles = roleRepository.findByName(PerfilEnum.ROLE_USER);
		user.setRoles(roles);
		user.setStatus(UserStatusEnum.ACTIVE);
		user.setPhoto(defaultAvatar());
		user.setSistemas(requestedSistemas);
		saveAndInvalidate(user);
	}

	@Override
	@Transactional
	public UserDTO createOrUpdateGoogleUser(UserDTO dto) {
		Set<SistemaEnum> requestedSistemas = resolveSistemas(dto);

		return repo.findByEmail(dto.getEmail())
				.map(existingUser -> {
					if (StringUtils.isBlank(existingUser.getProvider()) || "LOCAL".equals(existingUser.getProvider())) {
						existingUser.setProvider("GOOGLE");
						existingUser.setProviderId(dto.getProviderId());
					}
					if (StringUtils.isNotBlank(dto.getPhoto()) && hasDefaultAvatar(existingUser.getPhoto())) {
						existingUser.setPhoto(dto.getPhoto());
					}
					if (existingUser.getTermsAgreed() == null || !existingUser.getTermsAgreed()) {
						existingUser.setTermsAgreed(true);
					}
					if (existingUser.getSistemas() == null) {
						existingUser.setSistemas(new HashSet<>());
					}
					existingUser.getSistemas().addAll(requestedSistemas);
					existingUser.setUpdatedAt(LocalDate.now());
					User updated = saveAndInvalidate(existingUser);
					return UserMapper.INSTANCE.toDTO(updated);
				})
				.orElseGet(() -> {
					User newUser = UserMapper.INSTANCE.toEntity(dto);
					newUser.setPassword(null);
					newUser.setProvider("GOOGLE");
					newUser.setProviderId(dto.getProviderId());
					newUser.setTermsAgreed(true);
					newUser.setStatus(UserStatusEnum.ACTIVE);
					newUser.setPhoto(StringUtils.defaultIfBlank(dto.getPhoto(), defaultAvatar()));
					List<Role> roles = roleRepository.findByName(PerfilEnum.ROLE_USER);
					newUser.setRoles(roles);
					newUser.setSistemas(requestedSistemas);
					User saved = saveAndInvalidate(newUser);
					return UserMapper.INSTANCE.toDTO(saved);
				});
	}

	private Set<SistemaEnum> resolveSistemas(UserDTO dto) {
		if (dto != null && dto.getSistemas() != null && !dto.getSistemas().isEmpty()) {
			return new HashSet<>(dto.getSistemas());
		}
		return new HashSet<>(Collections.singleton(SistemaEnum.CADERNETA));
	}

	@Override
	@Transactional
	public UserDTO update(final String email, final UserDTO dto) {
		User user = repo.findByEmail(email).orElseThrow(() -> new UserException("Usuario não encontrado"));
		if (StringUtils.isNotBlank(dto.getName())) {
			user.setName(dto.getName());
		}
		if (StringUtils.isNotBlank(dto.getPassword())) {
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
		}

		if (StringUtils.isNotBlank(dto.getPhoto())) {
			user.setPhoto(dto.getPhoto());
		}

		if (ObjectUtils.isNotEmpty(dto.getPhotoUpdate()) && dto.getPhotoUpdate()) {
			log.info("Foto foi atualizada");
		}

		user.setUpdatedAt(LocalDate.now());
		User saved = saveAndInvalidate(user);
		return UserMapper.INSTANCE.toDTO(saved);
	}

	private User saveAndInvalidate(User user) {
		User saved = repo.save(user);
		events.publishEvent(new UserCache.Changed(saved.getId(), saved.getEmail()));
		return saved;
	}

	@Override
	public UserDTO findByEmail(String email) {
		return withAvatar(profiles.findByEmail(email), true);
	}

	@Override
	public UserDTO findById(Long id) {
		return withAvatar(profiles.findById(id), true);
	}

	@Override
	public List<UserDTO> findAll() {
		return profiles.findAll();
	}

	@Override
	public UserDTO login(String email) {
		return withAvatar(profiles.findForLogin(email), false);
	}

	private UserDTO withAvatar(UserDTO profile, boolean includeUploadUrl) {
		// O cache guarda apenas os dados persistidos. A resposta recebe sua própria URL pré-assinada.
		var response = new UserDTO();
		BeanUtils.copyProperties(profile, response);
		FileReference file = includeUploadUrl || hasDefaultAvatar(profile.getPhoto())
				? createFileReference(profile)
				: null;
		response.setPhoto(resolveAvatar(profile, file, includeUploadUrl));
		response.setPhotoUploaded(includeUploadUrl && file != null
				? cloudStorageProvider.generatePresignedUploadUrl(file).toString()
				: null);
		return response;
	}

	private String resolveAvatar(UserDTO user, FileReference file, boolean includeUploadUrl) {
		if (!hasDefaultAvatar(user.getPhoto())) {
			return user.getPhoto();
		}
		String fallback = defaultAvatar();
		if (file == null) {
			return fallback;
		}
		try {
			boolean exists = includeUploadUrl
					? cloudStorageProvider.fileExists(file.getPathAvatar())
					: cloudStorageProvider.fileExists(file.getPathAvatar(), LOGIN_AVATAR_TIMEOUT);
			if (!exists) {
				return fallback;
			}
		} catch (FileLookupException exception) {
			log.warn("Falha ao consultar avatar do usuário {}: {}", user.getId(), exception.getCause().getClass().getSimpleName());
			return fallback;
		}

		String avatarUrl = avatarUrl(file.getPathAvatar());
		return profiles.updateDiscoveredAvatar(user, avatarUrl) ? avatarUrl : fallback;
	}

	private FileReference createFileReference(UserDTO user) {
		if (StringUtils.isAnyBlank(user.getName(), user.getEmail())) {
			return null;
		}
		return FileReference.builder()
				.name(Constantes.sha256(user.getName() + user.getEmail()) + ".jpg")
				.durationLink(7L)
				.isPublicAccessible(true)
				.build();
	}

	private boolean hasDefaultAvatar(String photo) {
		return StringUtils.isBlank(photo) || photo.endsWith(AVATAR_BLANK);
	}

	private String defaultAvatar() {
		return avatarUrl(AVATAR_BLANK);
	}

	private String avatarUrl(String path) {
		return StringUtils.appendIfMissing(avatarBaseUrl, "/") + path;
	}
}
