package com.example.studentdb.controller;

import com.example.studentdb.dto.StudentResultDTO;
import com.example.studentdb.entity.Mark;
import com.example.studentdb.entity.Student;
import com.example.studentdb.entity.Subject;
import com.example.studentdb.entity.User;
import com.example.studentdb.repository.MarkRepository;
import com.example.studentdb.repository.StudentRepository;
import com.example.studentdb.repository.SubjectRepository;
import com.example.studentdb.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SubjectRepository subjectRepository;
    private final MarkRepository markRepository;

    public AdminController(StudentRepository studentRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           SubjectRepository subjectRepository,
                           MarkRepository markRepository) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.subjectRepository = subjectRepository;
        this.markRepository = markRepository;
    }

    // ----- Admin Dashboard -----
    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("message", "Welcome Admin!");
        return "admin_dashboard"; // admin_dashboard.html
    }

    // ========================= STUDENTS =========================

    // HTML view
    @GetMapping("/students")
    public String listStudents(Model model) {
        List<Student> students = studentRepository.findAll();
        model.addAttribute("students", students);
        return "admin_students"; // template
    }

    // JSON API endpoint
    @GetMapping("/students/json")
    @ResponseBody
    public List<Student> listStudentsJson() {
        return studentRepository.findAll();
    }

    @GetMapping("/students/add")
    public String addStudentForm(Model model) {
        model.addAttribute("student", new Student());
        return "admin_add_student"; // template
    }

    @PostMapping("/students/save")
    public String saveStudent(@ModelAttribute Student student) {
        // Create linked User
        User user = new User();
        user.setUsername(student.getEmail());                     // email as username
        user.setPassword(passwordEncoder.encode("student123"));   // default password
        user.setRole("STUDENT");
        userRepository.save(user);

        // Link student with User
        student.setUser(user);
        studentRepository.save(student);

        return "redirect:/admin/students";
    }

    // ----- Edit Student Form -----
    @GetMapping("/students/edit/{id}")
    public String editStudentForm(@PathVariable Long id, Model model) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        model.addAttribute("student", student);
        return "admin_edit_student"; // HTML page
    }

    // ----- Update Student -----
    @PostMapping("/students/update")
    public String updateStudent(@ModelAttribute Student student) {
        Student existingStudent = studentRepository.findById(student.getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        existingStudent.setName(student.getName());
        existingStudent.setEmail(student.getEmail());
        existingStudent.setRollNumber(student.getRollNumber());

        // Update linked user's username if email changed
        if (!existingStudent.getUser().getUsername().equals(student.getEmail())) {
            existingStudent.getUser().setUsername(student.getEmail());
        }

        studentRepository.save(existingStudent);
        return "redirect:/admin/students";
    }

    // ----- Delete Student -----
    @GetMapping("/students/delete/{id}")
    public String deleteStudent(@PathVariable Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        userRepository.delete(student.getUser());
        studentRepository.delete(student);
        return "redirect:/admin/students";
    }

    // ========================= SUBJECTS =========================

    @GetMapping("/subjects")
    public String listSubjects(Model model) {
        List<Subject> subjects = subjectRepository.findAll();
        model.addAttribute("subjects", subjects);
        return "admin_subjects"; // template
    }

    @GetMapping("/subjects/add")
    public String addSubjectForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "admin_add_subject"; // template
    }

    @PostMapping("/subjects/save")
    public String saveSubject(@ModelAttribute Subject subject) {
        subjectRepository.save(subject);
        return "redirect:/admin/subjects";
    }

    // ========================= MARKS =========================

    @GetMapping("/marks")
    public String listMarks(Model model) {
        List<Mark> marks = markRepository.findAll();
        model.addAttribute("marks", marks);
        return "admin_marks"; // template
    }

    @GetMapping("/marks/add")
    public String addMarkForm(Model model) {
        model.addAttribute("mark", new Mark());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("subjects", subjectRepository.findAll());
        return "admin_add_mark"; // template
    }

    @PostMapping("/marks/save")
    public String saveMark(@ModelAttribute Mark mark, Model model) {
        Student student = studentRepository.findById(mark.getStudent().getId())
                .orElseThrow(() -> new RuntimeException("Student not found"));
        Subject subject = subjectRepository.findById(mark.getSubject().getId())
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (markRepository.findByStudentAndSubject(student, subject).isPresent()) {
            model.addAttribute("error", "Marks for this student and subject already exist!");
            model.addAttribute("students", studentRepository.findAll());
            model.addAttribute("subjects", subjectRepository.findAll());
            model.addAttribute("mark", new Mark());
            return "admin_add_mark";
        }

        mark.setStudent(student);
        mark.setSubject(subject);
        markRepository.save(mark);

        return "redirect:/admin/marks";
    }

    // ========================= RESULTS =========================
    @GetMapping("/results")
    public String listResults(
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "size", required = false, defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            Model model) {

        List<Student> students;

        if (!keyword.isEmpty()) {
            students = studentRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword);
        } else {
            students = studentRepository.findAll();
        }

        int totalStudents = students.size();
        int totalPages = (int) Math.ceil((double) totalStudents / size);
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalStudents);
        List<Student> pagedStudents = (fromIndex >= totalStudents) ? new ArrayList<>() : students.subList(fromIndex, toIndex);

        List<StudentResultDTO> studentResults = new ArrayList<>();
        for (Student student : pagedStudents) {
            int totalMarks = 0;
            int subjectsCount = 0;
            boolean hasFail = false;

            if (student.getMarks() != null && !student.getMarks().isEmpty()) {
                for (Mark m : student.getMarks()) {
                    totalMarks += m.getMarks();
                    subjectsCount++;
                    if (m.getMarks() < 35) hasFail = true;
                }
            }

            double percentage = subjectsCount > 0 ? (totalMarks * 100.0 / (subjectsCount * 100)) : 0;
            String resultStatus = (hasFail || subjectsCount == 0) ? "Fail" : "Pass";

            studentResults.add(new StudentResultDTO(student, totalMarks, percentage, resultStatus));
        }

        model.addAttribute("studentResults", studentResults);
        model.addAttribute("keyword", keyword);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);

        return "admin_results";
    }
}
