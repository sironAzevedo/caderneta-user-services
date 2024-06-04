package com.caderneta.config;

import com.caderneta.handler.exception.InternalErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Profile("!test")
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.driver-class-name}")
    private String driver;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${database.user}")
    private String user;

    @Value("${database.pass}")
    private String pass;

    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    @Bean
    @Primary
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public DriverManagerDataSource data() {

        try {
            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName(driver);
            ds.setUrl(url);
            ds.setUsername(user);
            ds.setPassword(pass);
            ds.setSchema(schema);
            return ds;
        } catch (Exception e) {
            throw new InternalErrorException(e.getLocalizedMessage());
        }
    }
}
