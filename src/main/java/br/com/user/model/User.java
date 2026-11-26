package br.com.user.model;

import com.br.azevedo.model.enums.SistemaEnum;
import com.br.azevedo.model.enums.UserStatusEnum;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "TB_USER")
public class User implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "ID")
	@GeneratedValue(generator = "USUARIO_ID_SEQ", strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "USUARIO_ID_SEQ", sequenceName = "USUARIO_ID_SEQ", allocationSize = 1)
	private Long id;

	@NotNull
	@Column(name = "NAME", nullable = false)
	private String name;

	@NotNull
	@Column(name = "EMAIL", nullable = false)
	private String email;

	@ToString.Exclude
	@Column(name = "PWD", nullable = true)
	private String password;

	@Column(name = "PROVIDER")
	private String provider;

	@Column(name = "PROVIDER_ID")
	private String providerId;

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

	@ToString.Exclude
	@BatchSize(size = 50)
	@ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.MERGE })
	@JoinTable(name = "TB_USER_ROLE", joinColumns = @JoinColumn(name = "ID_USER", referencedColumnName = "ID"),
			inverseJoinColumns = @JoinColumn(name = "ID_ROLE", referencedColumnName = "ID"))
	private List<Role> roles;

	@ToString.Exclude
	@BatchSize(size = 50)
	@ElementCollection(fetch = FetchType.LAZY)
	@CollectionTable(name = "TB_USER_SYSTEM", joinColumns = @JoinColumn(name = "ID_USER"))
	@Column(name = "SISTEMA")
	@Enumerated(EnumType.STRING)
	@Builder.Default
	private Set<SistemaEnum> sistemas = new HashSet<>();

	@NotNull
	@Column(name = "PHOTO")
	private String photo;

	@Column(name = "TERMS_AGREED")
	private Boolean termsAgreed;

	@Override
	public boolean equals(Object other) {
		return this == other || (other instanceof User user && getId() != null && getId().equals(user.getId()));
	}

	@Override
	public int hashCode() {
		return User.class.hashCode();
	}

	@PrePersist
	public void prePersist() {
		createdAt = LocalDate.now();
	}

}
