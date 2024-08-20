package br.com.user;

import com.br.azevedo.security.EnableSecurity;
import com.br.azevedo.utils.mensagemUtils.EnableI18N;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@EnableI18N
@EnableSecurity(publicPaths = ".*/v1/user")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"br.com.user", "com.br.azevedo.security"})
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
