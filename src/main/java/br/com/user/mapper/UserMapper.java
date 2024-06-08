package br.com.user.mapper;

import br.com.user.model.User;
import br.com.user.model.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
	
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
	UserDTO toDTO(User entity);
	
	@Mappings({
	      @Mapping(target="name", source="dto.name"),
	      @Mapping(target="email", source="dto.email"),
//	      @Mapping(target="status", source="dto.status", defaultExpression = "java(com.caderneta.model.enums.UserStatusEnum.ACTIVE)" )
	    })
	User toEntity(UserDTO dto);
}
