package com.caderneta.kafka;

import org.springframework.kafka.support.serializer.JsonDeserializer;

public class CustomJsonDeserializer<T> extends JsonDeserializer<T> {

	public CustomJsonDeserializer(Class<? super T> targetType) {
		super(targetType);
		this.addTrustedPackages("*");
	}
}
