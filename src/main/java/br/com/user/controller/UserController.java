package br.com.user.controller;

import br.com.user.model.dto.UserDTO;
import br.com.user.service.IUserService;
import com.br.azevedo.infra.log.method.MethodLoggable;
import com.br.azevedo.security.user.ValidationUser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/user")
public class UserController {

	private final IUserService service;

	@PostMapping
	@ResponseBody
	@MethodLoggable
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Create User")
	public void create(@Valid @RequestBody UserDTO dto) {
		service.create(dto);
	}

	@MethodLoggable
	@ResponseBody
	@GetMapping("/login")
	@ResponseStatus(value = HttpStatus.OK)
	public UserDTO login(@RequestParam(value = "email") String email) {
		return service.login(email);
	}

	@ResponseBody
	@ValidationUser
	@MethodLoggable
	@GetMapping("/{email}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find User")
	public UserDTO findByEmail(@PathVariable("email") String email) {
		return service.findByEmail(email);
	}

	@GetMapping
	@ResponseBody
	@MethodLoggable
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find All User")
	public List<UserDTO> findAll() {
		return service.findAll();
	}
}
