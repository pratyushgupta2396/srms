package com.example.studentdb.repository;

import com.example.studentdb.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

//import java.lang.ScopedValue;
import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Student findByUserUsername(String username);
    List<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String name, String email);
    Student findByEmail(String email);
}
