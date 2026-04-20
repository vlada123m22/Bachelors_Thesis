package com.example.timesaver.service;

import com.example.timesaver.config.JwtConfig;
import com.example.timesaver.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class JwtServiceTest {

    @Mock
    private JwtConfig jwtConfig;

    @InjectMocks
    private JwtService jwtService;

    private final String secret = "myVeryLongSecretKeyThatIsAtLeast32BytesLong";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    public void setup() {
        when(jwtConfig.getSECRET()).thenReturn(secret);
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", expiration);
    }

    @Test
    public void testGenerateAndExtractUsername() {
        String username = "testuser";
        Set<Role> roles = Collections.emptySet();
        
        String token = jwtService.generateToken(username, roles);
        assertNotNull(token);
        
        String extracted = jwtService.extractUsername(token);
        assertEquals(username, extracted);
    }

    @Test
    public void testValidateTokenSuccess() {
        String token = jwtService.generateToken("user", Collections.emptySet());
        assertTrue(jwtService.validateToken(token));
    }

    @Test
    public void testValidateTokenInvalid() {
        assertFalse(jwtService.validateToken("invalid-token"));
    }

    @Test
    public void testExtractUsernameInvalidToken() {
        assertNull(jwtService.extractUsername("invalid-token"));
    }

    @Test
    public void testExpiredToken() {
        ReflectionTestUtils.setField(jwtService, "EXPIRATION_TIME", -1000L); // Already expired
        String token = jwtService.generateToken("user", Collections.emptySet());
        assertFalse(jwtService.validateToken(token));
    }
}
