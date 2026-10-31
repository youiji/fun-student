package com.example.student.dto;

import com.example.student.dto.user.UserRequest;
import jakarta.validation.groups.Default;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void rejectsStudentWithoutRequiredFields() {
        StudentRequest request = new StudentRequest();

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "surname", "email");
    }

    @Test
    void rejectsMalformedUserEmailAndEmptyRoles() {
        UserRequest request = new UserRequest("not-an-email", "secret", Set.of());

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("email", "roles");
    }

    @Test
    void requiresPasswordWhenCreatingUser() {
        UserRequest request = new UserRequest("student@example.com", null, Set.of(com.example.student.model.Role.TEACHER));

        assertThat(validator.validate(request, Default.class, OnCreate.class))
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("password");
    }

    @Test
    void acceptsValidLoginFields() {
        LoginRequest request = new LoginRequest("student@example.com", "secret");

        assertThat(validator.validate(request)).isEmpty();
    }
}
