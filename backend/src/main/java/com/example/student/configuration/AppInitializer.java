package com.example.student.configuration;

import com.example.student.model.Role;
import com.example.student.model.Student;
import com.example.student.model.User;
import com.example.student.repository.StudentRepository;
import com.example.student.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppInitializer {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final StudentRepository studentRepository;
    @Value("${app.seed.superadmin-password}")
    private String superadminPassword;
    @Value("${app.seed.admin-password}")
    private String adminPassword;
    @Value("${app.seed.teacher-password}")
    private String teacherPassword;

    @PostConstruct
    public void init() {
        createUserIfNotExists("superadmin@example.com", superadminPassword, Set.of(Role.SUPER_ADMIN));
        createUserIfNotExists("admin@example.com", adminPassword, Set.of(Role.ADMIN));
        createUserIfNotExists("teacher@example.com", teacherPassword, Set.of(Role.TEACHER));
        createStudentsIfEmpty();
    }

    private void createUserIfNotExists(String email, String rawPassword, Set<Role> roles) {
        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRoles(roles);
            userRepository.save(user);
        }
    }
    private void createStudentsIfEmpty() {
        if (studentRepository.count() == 0) {
            Student s1 = new Student();
            s1.setName("Michail");
            s1.setSurname("Ivanov");
            s1.setEmail("michail.ivanov@example.com");

            Student s2 = new Student();
            s2.setName("Petr");
            s2.setSurname("Semenov");
            s2.setEmail("petr.semenov@example.com");

            Student s3 = new Student();
            s3.setName("Sergey");
            s3.setSurname("Boroda");
            s3.setEmail("sergey.boroda@example.com");

            Student s4 = new Student();
            s4.setName("Ekaterina");
            s4.setSurname("Sidorova");
            s4.setEmail("ekaterina.sidorova@example.com");

            Student s5 = new Student();
            s5.setName("Dmitry");
            s5.setSurname("Alexandrov");
            s5.setEmail("dmitry.alexandrov@example.com");

            studentRepository.save(s1);
            studentRepository.save(s2);
            studentRepository.save(s3);
            studentRepository.save(s4);
            studentRepository.save(s5);

            log.info("Созданы тестовые записи студентов.");
        }
    }
}
