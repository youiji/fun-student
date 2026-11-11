package com.example.student.service;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.exception.EmailAlreadyExistsException;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.UserMapper;
import com.example.student.model.Role;
import com.example.student.model.User;
import com.example.student.repository.UserRepository;
import com.example.student.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void delegatesCheckingEmailExistence() {
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThat(userService.existsByEmail("user@example.com")).isTrue();
    }

    @Test
    void findsUserByEmail() {
        User user = user(1L, "user@example.com");
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThat(userService.findByEmail(user.getEmail())).isSameAs(user);
    }

    @Test
    void throwsWhenFindingMissingUserByEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByEmail("missing@example.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with email missing@example.com not found!");
    }

    @Test
    void findsAllUsersAndMapsThem() {
        List<User> users = List.of(user(1L, "user@example.com"));
        UserResponseList response = new UserResponseList(List.of(), 1);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toListResponse(users)).thenReturn(response);

        assertThat(userService.findAll()).isSameAs(response);
    }

    @Test
    void savesNewUserWithEncodedPassword() {
        UserRequest request = new UserRequest("new@example.com", "secret", Set.of(Role.TEACHER));
        User savedUser = user(2L, request.getEmail());
        UserResponse response = new UserResponse(2L, request.getEmail(), request.getRoles());
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(response);

        assertThat(userService.save(request)).isSameAs(response);

        verify(passwordEncoder).encode("secret");
        verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
                user.getEmail().equals(request.getEmail())
                        && user.getPassword().equals("encoded-secret")
                        && user.getRoles().equals(request.getRoles())));
    }

    @Test
    void rejectsNewUserWithDuplicateEmail() {
        UserRequest request = new UserRequest("existing@example.com", "secret", Set.of(Role.ADMIN));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.save(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email existing@example.com is already exists");
        verifyNoInteractions(passwordEncoder, userMapper);
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void updatesUserAndEncodesNewPassword() {
        User user = user(3L, "old@example.com");
        UserRequest request = new UserRequest("new@example.com", "new-secret", Set.of(Role.TEACHER));
        UserResponse response = new UserResponse(3L, request.getEmail(), request.getRoles());
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("new-secret")).thenReturn("encoded-new-secret");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(response);

        assertThat(userService.updateUser(3L, request)).isSameAs(response);

        assertThat(user.getEmail()).isEqualTo("new@example.com");
        assertThat(user.getPassword()).isEqualTo("encoded-new-secret");
        assertThat(user.getRoles()).containsExactly(Role.TEACHER);
    }

    @Test
    void updatesUserWithoutReplacingPasswordForBlankValue() {
        User user = user(4L, "user@example.com");
        user.setPassword("existing-password");
        UserRequest request = new UserRequest("user@example.com", " ", Set.of(Role.ADMIN));
        when(userRepository.findById(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(new UserResponse());

        userService.updateUser(4L, request);

        assertThat(user.getPassword()).isEqualTo("existing-password");
        verify(userRepository, never()).existsByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsUpdateWithDuplicateEmail() {
        User user = user(5L, "old@example.com");
        UserRequest request = new UserRequest("existing@example.com", null, Set.of(Role.ADMIN));
        when(userRepository.findById(5L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(5L, request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email existing@example.com is already exists");
        verify(userRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void throwsWhenUpdatingMissingUser() {
        when(userRepository.findById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(6L,
                new UserRequest("user@example.com", null, Set.of(Role.ADMIN))))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with ID 6 not found!");
    }

    @Test
    void deletesExistingUser() {
        User user = user(7L, "user@example.com");
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));

        userService.deleteById(7L);

        verify(userRepository).delete(user);
    }

    @Test
    void throwsWhenDeletingMissingUser() {
        when(userRepository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteById(8L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("User with ID 8 not found!");
        verify(userRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private static User user(Long id, String email) {
        return User.builder()
                .id(id)
                .email(email)
                .password("password")
                .roles(Set.of(Role.TEACHER))
                .build();
    }
}
