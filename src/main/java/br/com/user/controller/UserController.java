package br.com.user.controller;

import br.com.user.service.IUserService;
import com.br.azevedo.infra.log.method.MethodLoggable;
import com.br.azevedo.model.dto.UserDTO;
import com.br.azevedo.security.EnableSecurity;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMethod;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/v1/user")
public class UserController {

	private static final int MAX_USER_FIELD_LENGTH = 256;

	private final IUserService service;

	@PostMapping
	@ResponseBody
	@MethodLoggable
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Create User")
	@EnableSecurity(publicMethods = {RequestMethod.POST})
	public void create(@Valid @RequestBody UserDTO dto) {
		service.create(dto);
	}

	@PostMapping("/google")
	@ResponseBody
	@MethodLoggable
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Create or Update Google User")
	@EnableSecurity(publicMethods = {RequestMethod.POST})
	public UserDTO googleAuth(@Valid @RequestBody UserDTO dto) {
		if (StringUtils.isBlank(dto.getName()) || dto.getName().length() > MAX_USER_FIELD_LENGTH
				|| dto.getEmail().length() > MAX_USER_FIELD_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome ou e-mail inválido");
		}
		return service.createOrUpdateGoogleUser(dto);
	}

	@ResponseBody
	@MethodLoggable
	@GetMapping("/login/{email}")
	@ResponseStatus(value = HttpStatus.OK)
	public UserDTO login(@PathVariable("email") String email) {
		return service.login(email);
	}

	@ResponseBody
	@MethodLoggable
	@GetMapping("/{email}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find User Email")
	public UserDTO findByEmail(@PathVariable("email") String email) {
		return service.findByEmail(email);
	}

	@ResponseBody
	@MethodLoggable
	@GetMapping("/code/{id}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find User ID")
	public UserDTO findById(@PathVariable("id") Long id) {
		return service.findById(id);
	}

	@GetMapping
	@ResponseBody
	@MethodLoggable
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Find All User")
	public List<UserDTO> findAll() {
		return service.findAll();
	}

	@ResponseBody
	@MethodLoggable
	@PatchMapping("/{email}")
	@ResponseStatus(value = HttpStatus.OK)
	@Operation(summary = "Update User (name and password only)")
	public UserDTO patchUpdate(
			@PathVariable("email") @NotBlank @Email @Size(max = MAX_USER_FIELD_LENGTH) String email,
			@RequestBody UserDTO dto) {
		if (StringUtils.length(dto.getName()) > MAX_USER_FIELD_LENGTH) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome excede o tamanho permitido");
		}
		if (StringUtils.isAllBlank(dto.getName(), dto.getPassword(), dto.getPhoto()) && dto.getPhotoUpdate() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe um campo para atualizar");
		}
		return service.update(email, dto);
	}
}
