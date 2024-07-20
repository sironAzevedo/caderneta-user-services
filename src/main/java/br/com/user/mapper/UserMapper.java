package br.com.user.mapper;

import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.UserDTO;
import br.com.user.model.enums.PerfilEnum;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;

@Mapper
public interface UserMapper {
	
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

	@Mapping(target="password", ignore = true)
	@Mapping(target="perfis", ignore = true)
	UserDTO toDTO(User entity);

	@Mapping(target="perfis", expression = "java(resolvePerfis(entity.getRoles()))")
	UserDTO toDTOLogin(User entity);
	
	@Mappings({
	      @Mapping(target="name", source="dto.name"),
	      @Mapping(target="email", source="dto.email")
	    })
	User toEntity(UserDTO dto);

	default List<PerfilEnum> resolvePerfis(final List<Role> roles) {
		return roles.stream().map(Role::getName).toList();
	}
}
