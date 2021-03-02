package com.caderneta.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
    public void addCorsMappings(CorsRegistry registry) {
		registry
    	.addMapping("/**")
    	.allowedMethods(
    			HttpMethod.POST.name(), 
    			HttpMethod.GET.name(), 
    			HttpMethod.PUT.name(), 
    			HttpMethod.DELETE.name(), 
    			HttpMethod.PATCH.name(),
    			HttpMethod.HEAD.name(),
    			HttpMethod.OPTIONS.name(),
    			HttpMethod.TRACE.name());
    }
}
