package com.example.student.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {
    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @Spy
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    @InjectMocks
    private JwtFilter jwtFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void continuesRequestWithoutAuthorizationHeader() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtFilter.doFilter(new MockHttpServletRequest(), response, filterChain);

        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.same(response));
        verifyNoJwtInteractions();
    }

    @Test
    void continuesRequestWithNonBearerAuthorizationHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic credentials");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
        verifyNoJwtInteractions();
    }

    @Test
    void authenticatesRequestWithValidToken() throws Exception {
        UserDetails user = User.withUsername("teacher@example.com").password("encoded").roles("TEACHER").build();
        MockHttpServletRequest request = requestWithToken("valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("teacher@example.com");
        when(userDetailsService.loadUserByUsername("teacher@example.com")).thenReturn(user);
        when(jwtService.validateToken("valid-token", user)).thenReturn(true);

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication())
                .isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName())
                .isEqualTo("teacher@example.com");
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doesNotReplaceExistingAuthentication() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("existing", null));
        MockHttpServletRequest request = requestWithToken("valid-token");
        when(jwtService.extractUsername("valid-token")).thenReturn("teacher@example.com");

        jwtFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsUnauthorizedWhenTokenUserIsMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.extractUsername("deleted-token")).thenReturn("deleted@example.com");
        when(userDetailsService.loadUserByUsername("deleted@example.com"))
                .thenThrow(new org.springframework.security.core.userdetails.UsernameNotFoundException("missing"));

        jwtFilter.doFilter(requestWithToken("deleted-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Недействительный токен");
        verify(filterChain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void returnsUnauthorizedWhenTokenIsInvalid() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("invalid"));

        jwtFilter.doFilter(requestWithToken("invalid-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("\"statusCode\":401");
        verify(filterChain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doesNotWriteUnauthorizedBodyWhenResponseIsCommitted() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setCommitted(true);
        when(jwtService.extractUsername("invalid-token")).thenThrow(new JwtException("invalid"));

        jwtFilter.doFilter(requestWithToken("invalid-token"), response, filterChain);

        assertThat(response.getStatus()).isEqualTo(200);
        verify(filterChain, never()).doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private static MockHttpServletRequest requestWithToken(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }

    private void verifyNoJwtInteractions() {
        verify(jwtService, never()).extractUsername(anyString());
        verify(userDetailsService, never()).loadUserByUsername(anyString());
    }
}
