package com.example.timesaver.config;

import com.example.timesaver.model.User;
import com.example.timesaver.service.JwtService;
import com.example.timesaver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @BeforeEach
    public void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testDoFilterInternalNoHeader() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testDoFilterInternalInvalidToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid");
        when(jwtService.validateToken("invalid")).thenReturn(false);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testDoFilterInternalUserNotFound() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid");
        when(jwtService.validateToken("valid")).thenReturn(true);
        when(jwtService.extractUsername("valid")).thenReturn("user");
        when(userRepository.findByUserName("user")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testDoFilterInternalSuccess() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid");
        when(jwtService.validateToken("valid")).thenReturn(true);
        when(jwtService.extractUsername("valid")).thenReturn("user");
        User user = new User();
        user.setUserName("user");
        user.setRoles(Collections.emptySet());
        when(userRepository.findByUserName("user")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("user", SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
