package br.com.user;

import com.br.azevedo.infra.cache.EnableCache;
import com.br.azevedo.security.EnableSecurity;
import com.br.azevedo.utils.mensagemUtils.EnableI18N;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableI18N
@EnableCache
@EnableSecurity
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@ComponentScan(basePackages = {"br.com", "com.br.azevedo"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
