package com.example.student.controller;

import com.example.student.dto.StudentFullResponse;
import com.example.student.dto.StudentRequest;
import com.example.student.dto.StudentResponse;
import com.example.student.dto.StudentResponseForList;
import com.example.student.dto.StudentResponseList;
import com.example.student.dto.StudentUpdateRequest;
import com.example.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentControllerTest {
    @Mock
    private StudentService studentService;

    @InjectMocks
    private StudentController controller;

    @Test
    void delegatesStudentOperationsToService() {
        StudentRequest createRequest = new StudentRequest();
        StudentUpdateRequest updateRequest = new StudentUpdateRequest();
        StudentResponse response = new StudentResponse();
        StudentResponseList list = new StudentResponseList(List.<StudentResponseForList>of(), 0);
        StudentFullResponse fullResponse = new StudentFullResponse();
        when(studentService.findAll()).thenReturn(list);
        when(studentService.findById(1L)).thenReturn(fullResponse);
        when(studentService.save(createRequest)).thenReturn(response);
        when(studentService.update(1L, updateRequest)).thenReturn(response);

        assertThat(controller.findAll()).isSameAs(list);
        assertThat(controller.findStudentById(1L)).isSameAs(fullResponse);
        assertThat(controller.createStudent(createRequest)).isSameAs(response);
        assertThat(controller.updateStudent(1L, updateRequest)).isSameAs(response);
        controller.deleteStudentById(1L);

        verify(studentService).findAll();
        verify(studentService).findById(1L);
        verify(studentService).save(createRequest);
        verify(studentService).update(1L, updateRequest);
        verify(studentService).deleteById(1L);
    }
}
