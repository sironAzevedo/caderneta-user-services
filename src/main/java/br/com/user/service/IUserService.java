package br.com.user.service;

import com.br.azevedo.model.dto.UserDTO;

import java.util.List;

public interface IUserService {

	void create(UserDTO dto);

	UserDTO createOrUpdateGoogleUser(UserDTO dto);

	UserDTO update(String email, UserDTO dto);

	UserDTO findByEmail(String email);

	UserDTO findById(Long id);

	List<UserDTO> findAll();

	UserDTO login(String email);

}
