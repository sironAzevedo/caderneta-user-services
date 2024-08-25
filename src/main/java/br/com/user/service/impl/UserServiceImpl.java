package br.com.user.service.impl;

import br.com.user.mapper.UserMapper;
import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.UserDTO;
import br.com.user.model.enums.PerfilEnum;
import br.com.user.model.enums.UserStatusEnum;
import br.com.user.repository.IRoleRepository;
import br.com.user.repository.IUserRepository;
import br.com.user.service.IUserService;
import com.br.azevedo.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

	private final IUserRepository repo;
	private final IRoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
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
		repo.save(user);
	}

	@Override
	@Cacheable(value = "user_services_cliente_por_email", key = "#email", unless="#result == null")
	public UserDTO findByEmail(String email) {
		return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTO).orElseThrow(() -> new UserException("Usuario não encontrado"));
	}

	@Override
	public List<UserDTO> findAll() {
		return repo.findAll().stream().map(UserMapper.INSTANCE::toDTO).toList();
	}

	@Override
	@Cacheable(value = "user_services_cliente_login_email", key = "#email", unless="#result == null")
	public UserDTO login(String email) {
		log.info("Consultando o email {}", email);
		return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTOLogin).orElseThrow(() -> new UserException("Usuario não encontrado"));
	}
}
