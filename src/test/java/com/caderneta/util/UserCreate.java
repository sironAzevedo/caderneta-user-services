package com.caderneta.util;

import com.caderneta.model.User;
import com.caderneta.model.dto.LoginDTO;
import com.caderneta.model.enums.UserStatusEnum;

public final class UserCreate {
	
	private UserCreate() {
		super();
	}

	public static User createUser() {
		User user = new User();
		user.setName("Test da Silva");
		user.setEmail("test.sivla@email.com");
		user.setPassword("123");
		user.setStatus(UserStatusEnum.ACTIVE);		
		return user;
	}
	
	public static LoginDTO login() {
		LoginDTO l = new LoginDTO();
		l.setEmail("test.sivla@email.com");
		l.setPassword("123");
		return l;		
	}

}
