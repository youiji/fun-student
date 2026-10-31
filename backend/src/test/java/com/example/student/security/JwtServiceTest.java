package com.example.student.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    @Test
    void rejectsJwtSecretShorterThanHs256Minimum() {
        assertThatThrownBy(() -> new JwtService("too-short-secret"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET must contain at least 32 bytes");
    }

    @Test
    void rejectsBlankJwtSecret() {
        assertThatThrownBy(() -> new JwtService(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET must be configured");
    }
}
