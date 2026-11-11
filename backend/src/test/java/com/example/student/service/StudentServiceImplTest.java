package com.example.student.service;

import com.example.student.dto.StudentFullResponse;
import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponse;
import com.example.student.dto.StudentResponseList;
import com.example.student.dto.StudentUpdateRequest;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.mapper.StudentMapper;
import com.example.student.model.Student;
import com.example.student.repository.StudentRepository;
import com.example.student.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {
    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudentMapper studentMapper;

    @InjectMocks
    private StudentServiceImpl studentService;

    @Test
    void savesMappedStudentAndReturnsResponse() {
        StudentRequest request = new StudentRequest("Ivan", "Ivanov", null, "ivan@example.com");
        Student student = student(1L);
        StudentResponse response = new StudentResponse(1L, "Ivan", "Ivanov");
        when(studentMapper.requestToStudent(request)).thenReturn(student);
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(response);

        assertThat(studentService.save(request)).isSameAs(response);

        verify(studentMapper).requestToStudent(request);
        verify(studentRepository).save(student);
        verify(studentMapper).studentToResponse(student);
    }

    @Test
    void findsStudentById() {
        Student student = student(7L);
        StudentFullResponse response = new StudentFullResponse(7L, "Ivan", "Ivanov", null, "ivan@example.com");
        when(studentRepository.findById(7L)).thenReturn(Optional.of(student));
        when(studentMapper.studentToFullResponse(student)).thenReturn(response);

        assertThat(studentService.findById(7L)).isSameAs(response);
    }

    @Test
    void throwsWhenFindingMissingStudent() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.findById(99L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student with ID 99 not found!");
        verifyNoInteractions(studentMapper);
    }

    @Test
    void findsAllStudentsAndMapsThem() {
        List<Student> students = List.of(student(1L), student(2L));
        StudentResponseList response = new StudentResponseList(List.of(), 2);
        when(studentRepository.findAll()).thenReturn(students);
        when(studentMapper.studentListToStudentResponseList(students)).thenReturn(response);

        assertThat(studentService.findAll()).isSameAs(response);
    }

    @Test
    void updatesProvidedStudentFields() {
        Student student = student(3L);
        StudentUpdateRequest request = new StudentUpdateRequest("+79991234567", "new@example.com");
        StudentResponse response = new StudentResponse(3L, "Ivan", "Ivanov");
        when(studentRepository.findById(3L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(response);

        assertThat(studentService.update(3L, request)).isSameAs(response);

        assertThat(student.getPhoneNumber()).isEqualTo("+79991234567");
        assertThat(student.getEmail()).isEqualTo("new@example.com");
        verify(studentRepository).save(student);
    }

    @Test
    void keepsUnprovidedStudentFieldDuringUpdate() {
        Student student = student(4L);
        student.setPhoneNumber("+70000000000");
        StudentUpdateRequest request = new StudentUpdateRequest(null, "new@example.com");
        when(studentRepository.findById(4L)).thenReturn(Optional.of(student));
        when(studentRepository.save(student)).thenReturn(student);
        when(studentMapper.studentToResponse(student)).thenReturn(new StudentResponse());

        studentService.update(4L, request);

        assertThat(student.getPhoneNumber()).isEqualTo("+70000000000");
        assertThat(student.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void deletesExistingStudent() {
        Student student = student(5L);
        when(studentRepository.findById(5L)).thenReturn(Optional.of(student));

        studentService.deleteById(5L);

        verify(studentRepository).delete(student);
    }

    @Test
    void doesNotDeleteMissingStudent() {
        when(studentRepository.findById(6L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.deleteById(6L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Student with ID 6 not found!");
        verifyNoInteractions(studentMapper);
    }

    private static Student student(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setName("Ivan");
        student.setSurname("Ivanov");
        student.setEmail("ivan@example.com");
        return student;
    }
}
