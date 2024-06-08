package br.com.user.service;

import br.com.user.model.dto.LoginDTO;
import br.com.user.model.dto.UserDTO;

public interface IUserService {

	void create(UserDTO dto);
	
	void login(LoginDTO dto);

	UserDTO findByEmail(final String email);
}
