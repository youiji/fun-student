package com.example.student.dto;

import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.model.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoTest {
    private static Validator validator;
    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        objectMapper = new ObjectMapper();
    }

    @Test
    void acceptsValidStudentRequest() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "+79991234567", "ivan@example.com");

        assertThat(validator.validate(request)).isEmpty();
    }

    @Test
    void rejectsStudentRequestWithMissingRequiredFields() {
        StudentRequest request = new StudentRequest();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "surname", "email");
    }

    @Test
    void rejectsStudentRequestWithInvalidLengthsAndEmail() {
        StudentRequest request = new StudentRequest(
                "n".repeat(101),
                "s".repeat(101),
                "p".repeat(31),
                "e".repeat(256));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("name", "surname", "phoneNumber", "email", "email");
    }

    @Test
    void acceptsStudentUpdateWhenPhoneOrEmailIsProvided() {
        assertThat(validator.validate(new StudentUpdateRequest("+79991234567", null))).isEmpty();
        assertThat(validator.validate(new StudentUpdateRequest(null, "ivan@example.com"))).isEmpty();
    }

    @Test
    void rejectsStudentUpdateWithoutChanges() {
        StudentUpdateRequest request = new StudentUpdateRequest(null, null);

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactly("updateProvided");
    }

    @Test
    void rejectsStudentUpdateWithInvalidEmailAndLengths() {
        StudentUpdateRequest request = new StudentUpdateRequest(
                "p".repeat(31),
                "not-an-email");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("phoneNumber", "email");
    }

    @Test
    void acceptsValidLoginRequest() {
        assertThat(validator.validate(new LoginRequest("ivan@example.com", "secret"))).isEmpty();
    }

    @Test
    void rejectsLoginRequestWithMissingOrInvalidFields() {
        LoginRequest request = new LoginRequest("not-an-email", "");

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "password");
    }

    @Test
    void acceptsValidUserRequestOnCreate() {
        UserRequest request = new UserRequest("teacher@example.com", "secret", Set.of(Role.TEACHER));

        assertThat(validator.validate(request, OnCreate.class)).isEmpty();
    }

    @Test
    void requiresPasswordAndRoleWhenCreatingUser() {
        UserRequest request = new UserRequest("teacher@example.com", null, Set.of());

        assertThat(validator.validate(request, jakarta.validation.groups.Default.class, OnCreate.class))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("password", "roles");
    }

    @Test
    void rejectsUserRequestWithInvalidEmailAndPasswordLength() {
        UserRequest request = new UserRequest("not-an-email", "short", Set.of(Role.ADMIN));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .containsExactlyInAnyOrder("email", "password");
    }

    @Test
    void serializesStudentPhoneUsingApiFieldName() throws Exception {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "+79991234567", "ivan@example.com");

        String json = objectMapper.writeValueAsString(request);

        assertThat(json).contains("\"phone_number\":\"+79991234567\"");
        assertThat(json).doesNotContain("\"phoneNumber\"");
    }

    @Test
    void responseDtosExposeTheirValuesThroughConstructorsAndAccessors() {
        StudentResponse student = new StudentResponse(1L, "Ivan", "Ivanov");
        StudentResponseForList studentForList = new StudentResponseForList(1L, "Ivan", "Ivanov");
        StudentFullResponse fullStudent =
                new StudentFullResponse(1L, "Ivan", "Ivanov", "+79991234567", "ivan@example.com");
        AuthResponse auth = new AuthResponse("token", "Bearer", 3600, List.of("ROLE_TEACHER"));
        UserResponse user = new UserResponse(2L, "teacher@example.com", Set.of(Role.TEACHER));

        assertThat(student.getId()).isEqualTo(1L);
        assertThat(student.getName()).isEqualTo("Ivan");
        assertThat(student.getSurname()).isEqualTo("Ivanov");
        assertThat(studentForList).isEqualTo(new StudentResponseForList(1L, "Ivan", "Ivanov"));
        assertThat(fullStudent.getEmail()).isEqualTo("ivan@example.com");
        assertThat(auth.getToken()).isEqualTo("token");
        assertThat(auth.getRoles()).containsExactly("ROLE_TEACHER");
        assertThat(user.getRoles()).containsExactly(Role.TEACHER);
    }

    @Test
    void responseListDtosStoreResponsesAndTotalCount() {
        StudentResponseForList student = new StudentResponseForList(1L, "Ivan", "Ivanov");
        UserResponse user = new UserResponse(2L, "teacher@example.com", Set.of(Role.TEACHER));
        StudentResponseList students = new StudentResponseList(List.of(student), 1);
        UserResponseList users = new UserResponseList(List.of(user), 1);

        assertThat(students.getResponse()).containsExactly(student);
        assertThat(students.getTotalCount()).isEqualTo(1);
        assertThat(users.getResponses()).containsExactly(user);
        assertThat(users.getTotalCount()).isEqualTo(1);
    }
}
