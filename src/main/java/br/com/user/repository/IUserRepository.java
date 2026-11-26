package br.com.user.repository;

import br.com.user.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    @Modifying
    @Query("""
            update User u set u.photo = :photo, u.updatedAt = :updatedAt
            where u.id = :id and u.name = :name and u.email = :email
              and coalesce(u.photo, '') = :previousPhoto
            """)
    int updateAvatarIfUnchanged(Long id, String name, String email, String previousPhoto,
                                String photo, LocalDate updatedAt);
}
