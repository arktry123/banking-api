package com.eagle.banking.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = mock(FilterChain.class);
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_noAuthorizationHeader_doesNothing() throws IOException, ServletException {
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_invalidAuthorizationHeader_doesNothing() throws IOException, ServletException {
        request.addHeader("Authorization", "InvalidHeader");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verifyNoInteractions(jwtUtil);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_invalidToken_clearsSecurityContext() throws IOException, ServletException {
        request.addHeader("Authorization", "Bearer invalidToken");
        doThrow(new JwtException("Invalid token")).when(jwtUtil).parseToken("invalidToken");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).parseToken("invalidToken");
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws IOException, ServletException {
        request.addHeader("Authorization", "Bearer validToken");

        // Mock Claims
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("userId");

        // Mock Jws<Claims>
        Jws<Claims> jwsClaims = mock(Jws.class);
        when(jwsClaims.getPayload()).thenReturn(claims);

        // Mock JwtUtil to return the mocked Jws<Claims>
        when(jwtUtil.parseToken("validToken")).thenReturn(jwsClaims);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtUtil).parseToken("validToken");
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo("userId");
    }
}