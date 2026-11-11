package com.example.student.mapper;

import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponseList;
import com.example.student.model.Student;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StudentMapperImplTest {
    private final StudentMapper mapper = new StudentMapperImpl();

    @Test
    void mapsRequestToStudent() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", "+79991234567", "ivan@example.com");

        Student student = mapper.requestToStudent(request);

        assertThat(student.getName()).isEqualTo("Ivan");
        assertThat(student.getSurname()).isEqualTo("Ivanov");
        assertThat(student.getPhoneNumber()).isEqualTo("+79991234567");
        assertThat(student.getEmail()).isEqualTo("ivan@example.com");
    }

    @Test
    void mapsStudentToResponses() {
        Student student = student(1L);

        assertThat(mapper.studentToResponse(student).getId()).isEqualTo(1L);
        assertThat(mapper.studentToFullResponse(student).getEmail()).isEqualTo("ivan@example.com");
        assertThat(mapper.studentToStudentResponseForList(student).getSurname()).isEqualTo("Ivanov");
    }

    @Test
    void mapsStudentListAndCount() {
        StudentResponseList response = mapper.studentListToStudentResponseList(
                List.of(student(1L), student(2L)));

        assertThat(response.getResponse()).hasSize(2);
        assertThat(response.getTotalCount()).isEqualTo(2);
        assertThat(mapper.studentListToListOfStudentResponse(List.of())).isEmpty();
    }

    private static Student student(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setName("Ivan");
        student.setSurname("Ivanov");
        student.setPhoneNumber("+79991234567");
        student.setEmail("ivan@example.com");
        return student;
    }
}
