package com.caderneta.repository;

//@DataJpaTest
//@DisplayName("Tests for User Repository")
public class IUserRepositoryTest {
	
//	@Autowired
//	private IUserRepository repository;
//
//	@Test
//    @DisplayName("Save creates user when Successful")
//    void save_PersistUser_WhenSuccessful() {
//		User u = UserCreate.createUser();
//		User userSaved = this.repository.save(u);
//		Assertions.assertThat(userSaved).isNotNull();
//        Assertions.assertThat(userSaved.getId()).isNotNull();
//        Assertions.assertThat(userSaved.getName()).isEqualTo(u.getName());
//	}
//
//	@Test
//    @DisplayName("Save updates user when Successful")
//    void save_UpdatesAnime_WhenSuccessful(){
//        User userToBeSaved = UserCreate.createUser();
//        User userSaved = this.repository.save(userToBeSaved);
//
//        userSaved.setName("Overlord");
//
//        User userUpdated = this.repository.save(userSaved);
//
//        Assertions.assertThat(userUpdated).isNotNull();
//        Assertions.assertThat(userUpdated.getId()).isNotNull();
//        Assertions.assertThat(userUpdated.getName()).isEqualTo(userSaved.getName());
//    }
//
//	@Test
//    @DisplayName("Delete removes user when Successful")
//    void delete_RemovesUser_WhenSuccessful(){
//        User userToBeSaved = UserCreate.createUser();
//
//        User userSaved = this.repository.save(userToBeSaved);
//
//        this.repository.delete(userSaved);
//
//        Optional<User> animeOptional = this.repository.findById(userSaved.getId());
//
//        Assertions.assertThat(animeOptional).isEmpty();
//
//    }
//
//    @Test
//    @DisplayName("Find By Email returns User when Successful")
//    void findByName_ReturnsListOfAnime_WhenSuccessful(){
//    	User userToBeSaved = UserCreate.createUser();
//
//    	User userSaved = this.repository.save(userToBeSaved);
//
//        Optional<User> user = this.repository.findByEmail(userSaved.getEmail());
//
//        Assertions.assertThat(user).isNotEmpty();
//        Assertions.assertThat(user).contains(userSaved);
//
//    }
//
//    @Test
//    @DisplayName("Find By Email returns empty when no user is found")
//    void findByName_ReturnsEmptyList_WhenAnimeIsNotFound(){
//        Optional<User> user = this.repository.findByEmail("xaxa@email.com");
//        Assertions.assertThat(user).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Save throw DataIntegrityViolationException when name is empty")
//    void save_ThrowsConstraintViolationException_WhenNameIsEmpty(){
//        User user = UserCreate.createUser();
//        user.setName(null);
//
//        Assertions.assertThatExceptionOfType(DataIntegrityViolationException.class)
//                .isThrownBy(() -> this.repository.save(user));
//    }
}
