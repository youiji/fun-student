package com.example.student.service;

import com.example.student.model.Role;
import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    void convertsUserToSpringUserDetailsWithRoleAuthorities() {
        User user = User.builder()
                .email("teacher@example.com")
                .password("encoded")
                .roles(Set.of(Role.TEACHER, Role.ADMIN))
                .build();
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        var details = service.loadUserByUsername(user.getEmail());

        assertThat(details.getUsername()).isEqualTo("teacher@example.com");
        assertThat(details.getPassword()).isEqualTo("encoded");
        assertThat(details.getAuthorities())
                .extracting(Object::toString)
                .containsExactlyInAnyOrder("ROLE_TEACHER", "ROLE_ADMIN");
    }

    @Test
    void throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User with email missing@example.com not found");
    }
}
