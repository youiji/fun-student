package com.example.student.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsKnownExceptionsToExpectedResponses() {
        assertError(handler.catchEntityNotFoundException(new EntityNotFoundException("missing")),
                HttpStatus.NOT_FOUND, "missing");
        assertError(handler.catchEmailAlreadyExistsException(new EmailAlreadyExistsException("duplicate")),
                HttpStatus.CONFLICT, "duplicate");
        assertError(handler.catchBadCredentials(new BadCredentialsException("internal")),
                HttpStatus.UNAUTHORIZED, "Неверный email или пароль");
        assertError(handler.catchAccessDenied(new AccessDeniedException("internal")),
                HttpStatus.FORBIDDEN, "Недостаточно прав");
        assertError(handler.catchUnreadableMessage(null),
                HttpStatus.BAD_REQUEST, "Некорректное тело запроса");
        assertError(handler.catchUnexpectedException(new IllegalStateException("internal")),
                HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    }

    @Test
    void joinsValidationFieldErrors() {
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("request", "email", "Некорректный email"),
                new FieldError("request", "password", "Пароль обязателен")));
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        when(exception.getBindingResult()).thenReturn(bindingResult);

        assertError(handler.catchValidationException(exception),
                HttpStatus.BAD_REQUEST,
                "email: Некорректный email; password: Пароль обязателен");
    }

    private static void assertError(ResponseEntity<AppError> response, HttpStatus status, String message) {
        assertThat(response.getStatusCode()).isEqualTo(status);
        assertThat(response.getBody()).extracting(AppError::getStatusCode).isEqualTo(status.value());
        assertThat(response.getBody()).extracting(AppError::getMessage).isEqualTo(message);
    }
}
