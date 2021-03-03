package com.caderneta.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.caderneta.model.dto.LoginDTO;
import com.caderneta.model.dto.UserDTO;
import com.caderneta.service.IUserService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping(value = "/v1/user")
public class UserController {
	
	@Autowired
	private IUserService service;
	
	@PostMapping
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Create User")
	public void create(@Valid @RequestBody UserDTO dto) {		
		service.create(dto);
	}
	
	@ResponseBody
	@PostMapping("/login")
	@ResponseStatus(value = HttpStatus.OK)
	public void login(@Valid @RequestBody LoginDTO dto) {		
		service.login(dto);
	}
	
	@ResponseBody
	@GetMapping(value = "/by-email")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find User")
	public UserDTO findByEmail(@RequestParam(value = "email") String email) {
		return service.findUserByEmail(email);
	}
}
