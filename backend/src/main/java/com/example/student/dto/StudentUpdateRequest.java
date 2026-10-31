package com.example.student.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdateRequest {
    @Size(max = 30, message = "Номер телефона не должен превышать 30 символов")
    private String phoneNumber;
    @Email(message = "Некорректный email")
    @Size(max = 255, message = "Email не должен превышать 255 символов")
    private String email;

    @AssertTrue(message = "Необходимо указать email или номер телефона")
    public boolean isUpdateProvided() {
        return (phoneNumber != null && !phoneNumber.isBlank())
                || (email != null && !email.isBlank());
    }
}
