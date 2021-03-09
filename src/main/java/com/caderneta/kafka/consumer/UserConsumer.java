package com.caderneta.kafka.consumer;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.caderneta.model.dto.UserDTO;
import com.caderneta.service.IUserService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile(value = {"!test", "!heroku"})
public class UserConsumer {

	@Autowired
	private IUserService service;
	
	@KafkaListener(topics = "stream_new_user", groupId = "caderneta_user", containerFactory = "userListenerContainerFactory")
	public void consume(UserDTO message) throws IOException {
		log.info(String.format("Consuming stream_new_user, message: %s", message.toString()));
		service.create(message);
	}
}
