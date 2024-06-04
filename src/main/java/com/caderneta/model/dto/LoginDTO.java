package com.caderneta.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LoginDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	@Email
    @NotEmpty
	private String email;

	@NotEmpty
    private String password;

	public LoginDTO() {
		super();
	}

}
