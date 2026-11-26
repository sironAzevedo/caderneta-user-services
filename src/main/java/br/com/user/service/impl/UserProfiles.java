package br.com.user.service.impl;

import br.com.user.mapper.UserMapper;
import br.com.user.model.User;
import br.com.user.repository.IUserRepository;
import com.br.azevedo.exception.UserException;
import com.br.azevedo.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
class UserProfiles {

    private final IUserRepository repository;
    private final ApplicationEventPublisher events;

    @Cacheable(cacheNames = UserCache.BY_EMAIL, key = "#email")
    @Transactional(readOnly = true)
    public UserDTO findByEmail(String email) {
        return UserMapper.INSTANCE.toDTO(requireByEmail(email));
    }

    @Cacheable(cacheNames = UserCache.BY_ID, key = "#id")
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        var user = repository.findById(id)
                .orElseThrow(() -> new UserException("Usuario não encontrado"));
        return UserMapper.INSTANCE.toDTO(user);
    }

    @Cacheable(cacheNames = UserCache.LOGIN, key = "#email")
    @Transactional(readOnly = true)
    public UserDTO findForLogin(String email) {
        return UserMapper.INSTANCE.toDTOLogin(requireByEmail(email));
    }

    @Transactional(readOnly = true)
    public List<UserDTO> findAll() {
        return repository.findAll().stream().map(UserMapper.INSTANCE::toDTO).toList();
    }

    @Transactional
    public boolean updateDiscoveredAvatar(UserDTO user, String avatarUrl) {
        var updated = repository.updateAvatarIfUnchanged(
                user.getId(), user.getName(), user.getEmail(), StringUtils.defaultString(user.getPhoto()),
                avatarUrl, LocalDate.now());
        if (updated == 0) {
            return false;
        }
        events.publishEvent(new UserCache.Changed(user.getId(), user.getEmail()));
        return true;
    }

    private User requireByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UserException("Usuario não encontrado"));
    }
}
