package com.example.timesaver.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

public class ConfigBeansTest {

    @Test
    public void testJacksonConfig() {
        JacksonConfig config = new JacksonConfig();
        ObjectMapper mapper = config.objectMapper();
        assertNotNull(mapper);
    }

    @Test
    public void testJwtConfig() {
        JwtConfig config = new JwtConfig();
        config.setSECRET("secret");
        assertNotNull(config.getSECRET());
    }

    @Test
    public void testLiquibaseConfig() {
        LiquibaseConfig config = new LiquibaseConfig();
        assertNotNull(config);
    }

    @Test
    public void testSecurityConfigBeans() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config.passwordEncoder());
    }
}
