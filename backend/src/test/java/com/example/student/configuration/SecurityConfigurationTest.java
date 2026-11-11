package com.example.student.configuration;

import com.example.student.security.JwtFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SecurityConfigurationTest {
    private final SecurityConfiguration configuration =
            new SecurityConfiguration(Mockito.mock(JwtFilter.class));

    @Test
    void createsPasswordEncoderAndObjectMapper() {
        PasswordEncoder encoder = configuration.passwordEncoder();

        assertThat(encoder.matches("secret", encoder.encode("secret"))).isTrue();
        assertThat(configuration.objectMapper()).isNotNull();
    }

    @Test
    void createsAuthenticationManagerFromConfiguration() throws Exception {
        AuthenticationConfiguration authenticationConfiguration = Mockito.mock(AuthenticationConfiguration.class);
        AuthenticationManager manager = Mockito.mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(manager);

        assertThat(configuration.authenticationManager(authenticationConfiguration)).isSameAs(manager);
    }

}
