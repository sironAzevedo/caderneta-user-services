package com.caderneta.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import com.caderneta.model.User;
import com.caderneta.model.dto.UserDTO;

@Mapper
public interface UserMapper {
	
	UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
	
	UserDTO toDTO(User entity);
	
	@Mappings({
	      @Mapping(target="name", source="dto.name"),
	      @Mapping(target="email", source="dto.email"),
	      @Mapping(target="status", source="dto.status", defaultExpression = "java(com.caderneta.model.enums.UserStatusEnum.ACTIVE)" )
	    })
	User toEntity(UserDTO dto);
}
