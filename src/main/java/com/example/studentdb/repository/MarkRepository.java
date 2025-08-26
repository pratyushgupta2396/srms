package com.example.studentdb.repository;

import com.example.studentdb.entity.Mark;
import com.example.studentdb.entity.Student;
import com.example.studentdb.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MarkRepository extends JpaRepository<Mark, Long> {
    List<Mark> findByStudent(Student student);
    Optional<Mark> findByStudentAndSubject(Student student, Subject subject);

}
