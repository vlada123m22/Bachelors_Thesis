package com.example.timesaver.config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void testHandleValidationExceptions() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "message");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, Object>> resp = handler.handleValidationExceptions(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Validation failed", resp.getBody().get("message"));
        assertTrue(((Map)resp.getBody().get("errors")).containsKey("field"));
    }

    @Test
    public void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Denied");
        ResponseEntity<Map<String, String>> resp = handler.handleAccessDeniedException(ex);
        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
        assertEquals("Access denied: Denied", resp.getBody().get("message"));
    }

    @Test
    public void testHandleNoSuchElementException() {
        NoSuchElementException ex = new NoSuchElementException("Not found");
        ResponseEntity<Map<String, String>> resp = handler.handleNoSuchElementException(ex);
        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Not found", resp.getBody().get("message"));
    }

    @Test
    public void testHandleRuntimeException() {
        RuntimeException ex = new RuntimeException("Error");
        ResponseEntity<Map<String, String>> resp = handler.handleRuntimeException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("Error", resp.getBody().get("message"));
    }

    @Test
    public void testHandleGlobalException() {
        Exception ex = new Exception("Global");
        ResponseEntity<Map<String, String>> resp = handler.handleGlobalException(ex);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resp.getStatusCode());
        assertTrue(resp.getBody().get("message").contains("Global"));
    }
}
