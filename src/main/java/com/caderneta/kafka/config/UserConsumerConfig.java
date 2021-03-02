package com.caderneta.kafka.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@Configuration
public class UserConsumerConfig {

//	@Value(value = "${kafka.bootstrapAddress}")
//    private String bootstrapAddress;
	
//	private static final String GROUP_ID = "caderneta_user";

//    @Bean
//    public ConcurrentKafkaListenerContainerFactory<String, UserDTO> userListenerContainerFactory() {
//        ConcurrentKafkaListenerContainerFactory<String, UserDTO> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(userConsumerFactory());
//        return factory;
//    }
//    
//    private ConsumerFactory<String, UserDTO> userConsumerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
//        props.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
//        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
//        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "50");
//        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new JsonDeserializer<>(UserDTO.class, false));
//    }
}
