package com.example.student.security;

import com.example.student.configuration.SecurityConfiguration;
import com.example.student.controller.StudentController;
import com.example.student.controller.UserController;
import com.example.student.dto.StudentResponseList;
import com.example.student.dto.user.UserResponseList;
import com.example.student.exception.EntityNotFoundException;
import com.example.student.service.StudentService;
import com.example.student.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = {StudentController.class, UserController.class})
@Import(SecurityConfiguration.class)
class SecurityAuthorizationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @MockitoBean
    private StudentService studentService;

    @MockitoBean
    private UserService userService;

    @Test
    void unauthenticatedUserCannotListStudents() throws Exception {
        mockMvc.perform(get("/api/v1/student"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherCanListStudents() throws Exception {
        when(studentService.findAll()).thenReturn(new StudentResponseList(java.util.List.of(), 0));

        mockMvc.perform(get("/api/v1/student"))
                .andExpect(status().isOk());

        verify(studentService).findAll();
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherCannotDeleteStudent() throws Exception {
        mockMvc.perform(delete("/api/v1/student/1"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(studentService);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanDeleteStudent() throws Exception {
        doNothing().when(studentService).deleteById(1L);

        mockMvc.perform(delete("/api/v1/student/1"))
                .andExpect(status().isOk());

        verify(studentService).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherCannotManageUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(userService);
    }

    @Test
    @WithMockUser(roles = "SUPER_ADMIN")
    void superAdminCanManageUsers() throws Exception {
        when(userService.findAll()).thenReturn(new UserResponseList(java.util.List.of(), 0));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());

        verify(userService).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletingMissingStudentReturnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Student with ID 999 not found!"))
                .when(studentService).deleteById(999L);

        mockMvc.perform(delete("/api/v1/student/999"))
                .andExpect(status().isNotFound());

        verify(studentService).deleteById(999L);
    }

    @Test
    void invalidJwtReturnsUnauthorizedJson() throws Exception {
        when(jwtService.extractUsername("malformed-token"))
                .thenThrow(new io.jsonwebtoken.MalformedJwtException("malformed"));

        mockMvc.perform(get("/api/v1/student")
                        .header("Authorization", "Bearer" + " malformed-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Недействительный токен"));
    }

    @Test
    void jwtForDeletedUserReturnsUnauthorizedJson() throws Exception {
        when(jwtService.extractUsername("deleted-user-token")).thenReturn("deleted@example.com");
        when(userDetailsService.loadUserByUsername("deleted@example.com"))
                .thenThrow(new UsernameNotFoundException("internal database detail"));

        mockMvc.perform(get("/api/v1/student")
                        .header("Authorization", "Bearer" + " deleted-user-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Недействительный токен"));
    }

    @Test
    void missingAuthenticationReturnsUnauthorizedJson() throws Exception {
        mockMvc.perform(get("/api/v1/student"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Требуется аутентификация"));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void forbiddenRequestReturnsForbiddenJson() throws Exception {
        mockMvc.perform(delete("/api/v1/student/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.statusCode").value(403))
                .andExpect(jsonPath("$.message").value("Недостаточно прав"));
    }
}
