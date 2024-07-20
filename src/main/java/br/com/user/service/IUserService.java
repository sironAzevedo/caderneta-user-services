package br.com.user.service;

import br.com.user.model.dto.LoginDTO;
import br.com.user.model.dto.UserDTO;

public interface IUserService {

	void create(UserDTO dto);

	UserDTO login(final String email);

	UserDTO findByEmail(final String email);
}
