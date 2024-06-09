package br.com.user.config;

import br.com.user.handler.exception.InternalErrorException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Slf4j
@Profile("!test")
@Configuration
public class DatabaseConfig {

    @Value("${database.url}")
    private String url;

    @Value("${database.driver-class-name}")
    private String driver;

    @Value("${database.user}")
    private String user;

    @Value("${database.pass}")
    private String pass;

    @Value("${database.default_schema}")
    private String schema;

    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DriverManagerDataSource data() {
        log.info("Inicio - Configuração database");

        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(driver);
            ds.setUrl(url);
            ds.setUsername(user);
            ds.setPassword(pass);
            ds.setSchema(schema);
            log.info("Fim - Configuração database");
            return ds;
        } catch (Exception e) {
            log.error("Erro na configuração da base de dados");
            throw new InternalErrorException("Erro ao conectar ao database - Detalhe:" + e.getMessage());
        }
    }
}
