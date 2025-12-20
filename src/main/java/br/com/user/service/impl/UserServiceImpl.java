package br.com.user.service.impl;

import br.com.user.communs.Constantes;
import br.com.user.mapper.UserMapper;
import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.FileReference;
import br.com.user.model.dto.UserDTO;
import br.com.user.model.enums.PerfilEnum;
import br.com.user.model.enums.UserStatusEnum;
import br.com.user.repository.IRoleRepository;
import br.com.user.repository.IUserRepository;
import br.com.user.service.CloudStorageProvider;
import br.com.user.service.IUserService;
import com.br.azevedo.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import static br.com.user.communs.Constantes.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

	private final IUserRepository repo;
	private final IRoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final CloudStorageProvider cloudStorageProvider;
	
	@Override
	public void create(UserDTO dto) {
		
		if (repo.existsByEmail(dto.getEmail())) {
			log.error("Este e-mail [{}] já existe", dto.getEmail());
			throw new UserException("Este e-mail já existe");
		}
		
		User user = UserMapper.INSTANCE.toEntity(dto);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));
		List<Role> roles = roleRepository.findByName(PerfilEnum.ROLE_USER);
		user.setRoles(roles);
		user.setStatus(UserStatusEnum.ACTIVE);
		user.setPhoto(PHOTO_DEFAULT);
		repo.save(user);
	}

	@Override
	@Caching(evict = {
			@CacheEvict(value = "user_services_cliente_por_id", key = "#result.id", condition = "#result != null"),
			@CacheEvict(value = "user_services_cliente_por_email", key = "#result.email", condition = "#result != null"),
			@CacheEvict(value = "user_services_cliente_login_email", key = "#result.email", condition = "#result != null")
	})
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
		User saved = repo.save(user);
		return UserMapper.INSTANCE.toDTO(saved);
	}

	@Override
	@Cacheable(value = "user_services_cliente_por_email", key = "#email", unless="#result == null")
	public UserDTO findByEmail(String email) {
		User user = repo.findByEmail(email)
				.orElseThrow(() -> new UserException("Usuario não encontrado"));

		UserDTO dto = UserMapper.INSTANCE.toDTO(user);
		dto.setPhoto(gerarLinkDownloadAvatar(user));
		dto.setPhotoUploaded(gerarLinkUploudAvatar(user));
		return dto;
	}

	@Override
	@Cacheable(value = "user_services_cliente_por_id", key = "#id", unless="#result == null")
	public UserDTO findById(final Long id) {
		User user = repo.findById(id)
				.orElseThrow(() -> new UserException("Usuario não encontrado"));

		UserDTO dto = UserMapper.INSTANCE.toDTO(user);
		dto.setPhoto(gerarLinkDownloadAvatar(user));
		dto.setPhotoUploaded(gerarLinkUploudAvatar(user));
		return dto;
	}

	@Override
	public List<UserDTO> findAll() {
		return repo.findAll()
				.stream().map(UserMapper.INSTANCE::toDTO)
				.toList();
	}

	@Override
	@Cacheable(value = "user_services_cliente_login_email", key = "#email", unless="#result == null")
	public UserDTO login(String email) {
		User user = repo.findByEmail(email)
				.orElseThrow(() -> new UserException("Usuario não encontrado"));

		UserDTO dto = UserMapper.INSTANCE.toDTOLogin(user);
		dto.setPhoto(gerarLinkDownloadAvatar(user));
		return dto;
	}

	private String gerarLinkUploudAvatar(User user) {
		var fileReference = createFileReference(user);
		return cloudStorageProvider.generatePresignedUploadUrl(fileReference).toString();
	}

	private String gerarLinkDownloadAvatar(User user) {
		if (user.getPhoto().endsWith(AVATAR_BLANK)) {
			FileReference fileReference = createFileReference(user);
			if (cloudStorageProvider.fileExists(fileReference.getPathAvatar())) {
				String photoAvatar = URL_AVATAR.concat(fileReference.getPathAvatar());

				var userDto = new UserDTO();
				userDto.setPhoto(photoAvatar);

				update(user.getEmail(), userDto);
				return photoAvatar;
			}
			return PHOTO_DEFAULT;
		}
		return user.getPhoto();
	}

	private FileReference createFileReference(User user) {
		var fileNameAux = getFileNameAvatar(user);

		return FileReference.builder()
				.name(fileNameAux.concat(".jpg"))
				.durationLink(7L)
				.isPublicAccessible(true)
				.build();
	}

	private static String getFileNameAvatar(User user) {
		var fileName = user.getName().concat(user.getEmail());
		return Constantes.sha256(fileName);
	}

}
