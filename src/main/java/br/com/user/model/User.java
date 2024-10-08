package br.com.user.model;

import br.com.user.model.enums.UserStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Setter
@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Table(name = "TB_USER")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "ID")
	@GeneratedValue(generator = "USUARIO_ID_SEQ", strategy = GenerationType.IDENTITY)
	@SequenceGenerator(name = "USUARIO_ID_SEQ", sequenceName = "USUARIO_ID_SEQ", allocationSize = 1)
	private Long id;

	@NotNull
	@Column(name = "NAME", nullable = false)
	private String name;

	@NotNull
	@Column(name = "EMAIL", nullable = false)
	private String email;

	@NotNull
	@Column(name = "PWD", nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(name = "STATUS", columnDefinition = "dominio_status")
    private UserStatusEnum status;

	@CreatedDate
	@Temporal(TemporalType.DATE)
	@Column(name = "DT_CADASTRO", updatable = false)
	private LocalDate createdAt;

	@LastModifiedDate
	@Column(name = "DT_UPDATE")
	@Temporal(TemporalType.DATE)
	private LocalDate updatedAt;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.MERGE })
	@JoinTable(name = "TB_USER_ROLE", joinColumns = @JoinColumn(name = "ID_USER", referencedColumnName = "ID"),
			inverseJoinColumns = @JoinColumn(name = "ID_ROLE", referencedColumnName = "ID"))
	private List<Role> roles;

	@PrePersist
	public void prePersist() {
		createdAt = LocalDate.now();
	}

}
