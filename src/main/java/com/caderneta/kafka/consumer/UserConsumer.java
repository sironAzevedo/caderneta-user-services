package com.caderneta.kafka.consumer;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserConsumer {

//	@Autowired
//	private IUserService service;
//	
//	@KafkaListener(topics = "stream_new_user", groupId = "caderneta_user", containerFactory = "userListenerContainerFactory")
//	public void consume(UserDTO message) throws IOException {
//		log.info(String.format("Consuming stream_new_user, message: %s", message.toString()));
//		service.create(message);
//	}
}
