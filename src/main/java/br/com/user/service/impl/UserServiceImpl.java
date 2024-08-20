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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

	private final IUserRepository repo;
	private final IRoleRepository roleRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	
	@Override
	public void create(UserDTO dto) {
		
		if (repo.existsByEmail(dto.getEmail())) {
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
	public UserDTO findByEmail(String email) {
		return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTO).orElseThrow(() -> new UserException("Usuario não encontrado"));
	}

	@Override
	public List<UserDTO> findAll() {
		return repo.findAll().stream().map(UserMapper.INSTANCE::toDTO).toList();
	}

	@Override
	public UserDTO login(String email) {
		return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTOLogin).orElseThrow(() -> new UserException("Usuario não encontrado"));
	}
}
