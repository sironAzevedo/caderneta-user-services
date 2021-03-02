package com.caderneta.service;

import com.caderneta.model.dto.LoginDTO;
import com.caderneta.model.dto.UserDTO;

public interface IUserService {

	void create(UserDTO dto);
	
	void login(LoginDTO dto);

	UserDTO findUserByEmail(final String email);
}
