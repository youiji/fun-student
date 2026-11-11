package com.example.student.controller;

import com.example.student.dto.OnCreate;
import com.example.student.dto.user.UserRequest;
import com.example.student.dto.user.UserResponse;
import com.example.student.dto.user.UserResponseList;
import com.example.student.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController controller;

    @Test
    void delegatesUserOperationsToService() {
        UserRequest request = new UserRequest();
        UserResponse response = new UserResponse();
        UserResponseList list = new UserResponseList();
        when(userService.findAll()).thenReturn(list);
        when(userService.save(request)).thenReturn(response);
        when(userService.updateUser(1L, request)).thenReturn(response);

        assertThat(controller.getAllUsers()).isSameAs(list);
        assertThat(controller.save(request)).isSameAs(response);
        assertThat(controller.updateUser(1L, request)).isSameAs(response);
        controller.deleteById(1L);

        verify(userService).findAll();
        verify(userService).save(request);
        verify(userService).updateUser(1L, request);
        verify(userService).deleteById(1L);
    }
}
