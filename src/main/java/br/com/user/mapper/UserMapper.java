package br.com.user.mapper;

import br.com.user.model.Role;
import br.com.user.model.User;
import br.com.user.model.dto.UserDTO;
import br.com.user.model.enums.PerfilEnum;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class UserMapper {

    public static final UserMapper INSTANCE = new UserMapper();

    public UserDTO toDTO(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setProvider(entity.getProvider());
        dto.setProviderId(entity.getProviderId());
        dto.setStatus(entity.getStatus());
        dto.setPhoto(entity.getPhoto());
        dto.setPerfis(resolvePerfis(entity.getRoles()));
        return dto;
    }

    public UserDTO toDTOLogin(User entity) {
        if (entity == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setEmail(entity.getEmail());
        dto.setPassword(entity.getPassword());
        dto.setProvider(entity.getProvider());
        dto.setProviderId(entity.getProviderId());
        dto.setStatus(entity.getStatus());
        dto.setPhoto(entity.getPhoto());
        dto.setPerfis(resolvePerfis(entity.getRoles()));
        return dto;
    }

    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .password(dto.getPassword())
                .provider(dto.getProvider())
                .providerId(dto.getProviderId())
                .status(dto.getStatus())
                .photo(dto.getPhoto())
                .build();
    }

    public List<PerfilEnum> resolvePerfis(final List<Role> roles) {
        if (roles == null) {
            return Collections.emptyList();
        }
        return roles.stream().map(Role::getName).toList();
    }
}
