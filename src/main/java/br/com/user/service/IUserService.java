package br.com.user.service;

import br.com.user.model.dto.LoginDTO;
import br.com.user.model.dto.UserDTO;

import java.util.List;

public interface IUserService {

	void create(UserDTO dto);

	UserDTO login(final String email);

	UserDTO findByEmail(final String email);

	UserDTO findById(final Long id);

	List<UserDTO> findAll();
}
