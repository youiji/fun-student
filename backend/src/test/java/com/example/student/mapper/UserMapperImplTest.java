package com.example.student.mapper;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponseList;
import com.example.student.model.Role;
import com.example.student.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperImplTest {
    private final UserMapper mapper = new UserMapperImpl();

    @Test
    void mapsRequestToUser() {
        UserRequest request = new UserRequest("teacher@example.com", "secret", Set.of(Role.TEACHER));

        User user = mapper.toEntity(request);

        assertThat(user.getEmail()).isEqualTo("teacher@example.com");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRoles()).containsExactly(Role.TEACHER);
    }

    @Test
    void mapsUserToResponse() {
        User user = user(1L);

        assertThat(mapper.toResponse(user).getId()).isEqualTo(1L);
        assertThat(mapper.toResponse(user).getEmail()).isEqualTo("teacher@example.com");
    }

    @Test
    void mapsUserListAndCount() {
        UserResponseList response = mapper.toListResponse(List.of(user(1L), user(2L)));

        assertThat(response.getResponses()).hasSize(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(mapper.toResponseList(List.of())).isEmpty();
    }

    private static User user(Long id) {
        return User.builder()
                .id(id)
                .email("teacher@example.com")
                .password("secret")
                .roles(Set.of(Role.TEACHER))
                .build();
    }
}
