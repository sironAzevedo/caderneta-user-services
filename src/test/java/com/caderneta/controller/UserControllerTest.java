package com.caderneta.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.caderneta.service.IUserService;
import com.caderneta.util.UserCreate;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private IUserService service;

	@Test
	void whenValidInput_thenReturns200() throws Exception {
		mockMvc.perform(get("/v1/user/by-email")
		        .contentType(MediaType.APPLICATION_JSON)
		        .param("email", "email@test.com"))
		        .andExpect(status().isOk());	
		
	}
	
	@Test
	void whenCreateUserValid_thenReturns200() throws Exception {
		mockMvc.perform(post("/v1/user")
		        .contentType(MediaType.APPLICATION_JSON)
		        .content(objectMapper.writeValueAsString(UserCreate.createUser())))
		        .andExpect(status().isOk());	
	}

}
