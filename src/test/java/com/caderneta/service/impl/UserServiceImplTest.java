package com.caderneta.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.caderneta.handler.exception.UserException;
import com.caderneta.mapper.UserMapper;
import com.caderneta.model.User;
import com.caderneta.model.dto.LoginDTO;
import com.caderneta.model.dto.UserDTO;
import com.caderneta.repository.IUserRepository;
import com.caderneta.service.IUserService;
import com.caderneta.util.UserCreate;

@SpringBootTest
class UserServiceImplTest {
	
	@Autowired(required=true)
	private IUserService service;
	
	@MockBean
	private IUserRepository repository;
	
	private User user;
	
	@BeforeEach
	void setUp() {
		user = UserCreate.createUser();
//		user.setId(1000L);
	}

	@Test
	void whenCreateUserValid_thenSucess() {
		when(repository.existsByEmail(ArgumentMatchers.anyString())).thenReturn(false);
		service.create(UserMapper.INSTANCE.toDTO(user));
		verify(repository, times(1)).save(user);
	}
	
	@Test
	void whenFindEmilValid_thenReturnUser( ) {
		when(repository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.of(user));
		UserDTO byEmail = service.findUserByEmail(user.getEmail());
		assertNotNull(byEmail);
	}
	
	@Test
	void whenLoginValid_thenSucess( ) {
		LoginDTO login = UserCreate.login();
		when(repository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.of(user));
		service.login(login);
		assertEquals("123", login.getPassword());
	}
	
	@Test
    @DisplayName("Create user with exist email throws UserException")
    void createUser_with_emailExist_throwsUserException(){
		when(repository.existsByEmail(ArgumentMatchers.anyString())).thenReturn(true);

        Assertions.assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> service.create(UserMapper.INSTANCE.toDTO(user)));
    }
	
	@Test
    @DisplayName("Find user with not exist email throws UserException")
    void findUser_with_emailNotExist_throwsUserException(){
		when(repository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> service.findUserByEmail(user.getEmail()));
    }
	
	@Test
    @DisplayName("Login with password different throws UserException")
    void whenLogin_with_emailPasswordDifferent_ReturnthrowsUserException(){
		LoginDTO login = UserCreate.login();
		login.setPassword("1234");
		when(repository.findByEmail(ArgumentMatchers.anyString())).thenReturn(Optional.of(user));

        Assertions.assertThatExceptionOfType(UserException.class)
                .isThrownBy(() -> service.login(login));
    }

}
