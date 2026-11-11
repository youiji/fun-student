package com.example.student.security;

import com.example.student.model.Role;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {
    private static final String SECRET = "a".repeat(32);

    @Test
    void rejectsBlankSecret() {
        assertThatThrownBy(() -> new JwtService(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET must be configured");
    }

    @Test
    void rejectsShortSecret() {
        assertThatThrownBy(() -> new JwtService("short"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("JWT_SECRET must contain at least 32 bytes");
    }

    @Test
    void generatesTokenWithUsernameAndRoles() {
        JwtService service = new JwtService(SECRET);

        String token = service.generateToken("teacher@example.com", Set.of(Role.TEACHER, Role.ADMIN));

        assertThat(service.extractUsername(token)).isEqualTo("teacher@example.com");
        assertThat(service.extractRoles(token)).containsExactlyInAnyOrder(Role.TEACHER, Role.ADMIN);
    }

    @Test
    void validatesTokenForMatchingUser() {
        JwtService service = new JwtService(SECRET);
        String token = service.generateToken("teacher@example.com", Set.of(Role.TEACHER));
        UserDetailsStub user = new UserDetailsStub("teacher@example.com");

        assertThat(service.validateToken(token, user)).isTrue();
    }

    @Test
    void rejectsTokenForDifferentUserOrSecret() {
        JwtService service = new JwtService(SECRET);
        String token = service.generateToken("teacher@example.com", Set.of(Role.TEACHER));

        assertThat(service.validateToken(token, new UserDetailsStub("admin@example.com"))).isFalse();
        assertThatThrownBy(() -> new JwtService("b".repeat(32)).extractUsername(token))
                .isInstanceOf(RuntimeException.class);
    }

    private record UserDetailsStub(String username)
            implements org.springframework.security.core.userdetails.UserDetails {
        @Override
        public java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> getAuthorities() {
            return java.util.List.of();
        }

        @Override
        public String getPassword() {
            return "password";
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
