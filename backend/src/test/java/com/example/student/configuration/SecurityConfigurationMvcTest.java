package com.example.student.configuration;

import com.example.student.controller.StudentController;
import com.example.student.security.JwtFilter;
import com.example.student.security.JwtService;
import com.example.student.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StudentController.class)
@Import(SecurityConfiguration.class)
class SecurityConfigurationMvcTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoSpyBean
    private JwtFilter jwtFilter;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private StudentService studentService;

    @Test
    void rejectsUnauthenticatedRequestWithJsonError() throws Exception {
        mockMvc.perform(get("/api/v1/student"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.message").value("Требуется аутентификация"));
    }
}
