package br.com.user.repository;

import br.com.user.model.Role;
import br.com.user.model.enums.PerfilEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IRoleRepository extends JpaRepository<Role, String> {

    List<Role> findByName(PerfilEnum perfil);
}
