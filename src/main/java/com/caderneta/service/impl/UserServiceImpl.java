package com.caderneta.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.caderneta.handler.exception.UserException;
import com.caderneta.mapper.UserMapper;
import com.caderneta.model.User;
import com.caderneta.model.dto.LoginDTO;
import com.caderneta.model.dto.UserDTO;
import com.caderneta.repository.IUserRepository;
import com.caderneta.service.IUserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

	private final IUserRepository repo;
	
	@Override
	public void create(UserDTO dto) {
		
		if (repo.existsByEmail(dto.getEmail())) {
			throw new UserException("Este e-mail já existe");
		}
		
		User user = UserMapper.INSTANCE.toEntity(dto);
		user.setPassword(dto.getPassword());
		repo.save(user);
	}

	@Override
	public UserDTO findUserByEmail(String email) {
		return repo.findByEmail(email).map(UserMapper.INSTANCE::toDTO).orElseThrow(() -> new UserException("Usuario não encontrado"));
	}

	@Override
	public void login(LoginDTO dto) {
		UserDTO user = this.findUserByEmail(dto.getEmail());
		
		if(!user.getPassword().equals(dto.getPassword())) {
			throw new UserException("Email e/ou senha estão incorretos");
		}
	}
}
