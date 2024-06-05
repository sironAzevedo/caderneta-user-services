package com.caderneta.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@ActiveProfiles("test")
@TestPropertySource(locations = {"classpath:application.yml"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractTest {

    @LocalServerPort
    private int port;

    static {
        ClassLoader.getSystemClassLoader().setPackageAssertionStatus("org.postgresql.Driver", false);
    }

    public AbstractTest() {}

}
