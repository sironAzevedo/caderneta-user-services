package br.com.user.model.dto;

import br.com.user.model.enums.PerfilEnum;
import br.com.user.model.enums.UserStatusEnum;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;

	@NotEmpty
	private String name;
	
	@Email
    @NotEmpty
	private String email;

	@NotEmpty
	private String password;

	private UserStatusEnum status;

	private List<PerfilEnum> perfis;

	private String photo;

	private String photoUploaded;

	private Boolean photoUpdate;

	public UserDTO() {
		super();
	}
}
