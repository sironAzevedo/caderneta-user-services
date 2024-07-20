package br.com.user.controller;

import br.com.user.model.dto.LoginDTO;
import br.com.user.model.dto.UserDTO;
import br.com.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/user")
public class UserController {

	private final IUserService service;
	
	@PostMapping
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Create User")
	public void create(@Valid @RequestBody UserDTO dto) {
		service.create(dto);
	}
	
	@ResponseBody
	@GetMapping("/login")
	@ResponseStatus(value = HttpStatus.OK)
	public UserDTO login(@RequestParam(value = "email") String email) {
		return service.login(email);
	}

	@ResponseBody
	@GetMapping(value = "/by-email")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find User")
	public UserDTO findByEmail(@RequestParam(value = "email") String email) {
		return service.findByEmail(email);
	}
}
