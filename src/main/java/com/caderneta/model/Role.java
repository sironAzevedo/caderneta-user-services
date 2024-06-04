package com.caderneta.model;

import com.caderneta.model.enums.PerfilEnum;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Setter
@Getter
@Entity
@ToString
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = "TB_ROLE")
public class Role implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @Enumerated(EnumType.STRING)
    private PerfilEnum name;

    @ManyToMany(mappedBy = "roles")
    private List<User> users;
}
