package com.example.studentdb.controller;

import com.example.studentdb.entity.Mark;
import com.example.studentdb.entity.Student;
import com.example.studentdb.repository.MarkRepository;
import com.example.studentdb.repository.StudentRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.awt.Color;   // ✅ Required for colors
import com.lowagie.text.PageSize;   // ✅ Required for page size
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.security.Principal;
import java.util.List;

@Controller
public class StudentController {

    private final StudentRepository studentRepository;
    private final MarkRepository markRepository;

    public StudentController(StudentRepository studentRepository,
                             MarkRepository markRepository) {
        this.studentRepository = studentRepository;
        this.markRepository = markRepository;
    }

    // ----- Student Dashboard -----
    @GetMapping("/student/dashboard")
    public String studentDashboard(Authentication authentication, Model model) {
        String username = authentication.getName();

        Student student = studentRepository.findAll()
                .stream()
                .filter(s -> s.getUser().getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (student != null) {
            model.addAttribute("student", student);
            model.addAttribute("message", "Welcome " + student.getName() + "!");
        } else {
            model.addAttribute("message", "Welcome Student!");
        }

        return "student_dashboard";
    }

    // ----- View My Results -----
    @GetMapping("/student/results")
    public String viewResults(Authentication authentication, Model model) {
        String username = authentication.getName();

        Student student = studentRepository.findByUserUsername(username);

        if (student != null) {
            List<Mark> marks = markRepository.findByStudent(student);

            int totalMarks = marks.stream().mapToInt(Mark::getMarks).sum();
            int subjectCount = marks.size();
            double percentage = subjectCount > 0 ? (totalMarks * 1.0 / subjectCount) : 0.0;

            boolean hasFailed = marks.stream().anyMatch(m -> m.getMarks() < 35);
            String resultStatus = hasFailed ? "Fail" : "Pass";

            model.addAttribute("student", student);
            model.addAttribute("marks", marks);
            model.addAttribute("totalMarks", totalMarks);
            model.addAttribute("percentage", percentage);
            model.addAttribute("resultStatus", resultStatus);

        } else {
            model.addAttribute("error", "No student found for username: " + username);
        }

        return "student_results";
    }

    // ----- Download Result PDF -----
    @GetMapping("/student/results/pdf")
    public void downloadResultPdf(HttpServletResponse response, Principal principal) throws Exception {
        String email = principal.getName();
        Student student = studentRepository.findByEmail(email);
        if (student == null) {
            throw new RuntimeException("Student not found");
        }

        List<Mark> marks = markRepository.findByStudent(student);
        int totalMarks = marks.stream().mapToInt(Mark::getMarks).sum();
        double percentage = marks.isEmpty() ? 0 : (double) totalMarks / marks.size();
        String resultStatus = percentage >= 33 ? "Pass" : "Fail";

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=Result_" + student.getName() + ".pdf");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Header
        Font headerFont = new Font(Font.HELVETICA, 18, Font.BOLD, Color.BLUE);
        Paragraph instituteName = new Paragraph("Techno India University, West Bengal", headerFont);
        instituteName.setAlignment(Element.ALIGN_CENTER);
        document.add(instituteName);

        Paragraph address = new Paragraph("EM-4, Sector-V, Salt Lake, Kolkata - 700091, West Bengal, India | info@techno.edu.in",
                new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY));
        address.setAlignment(Element.ALIGN_CENTER);
        document.add(address);

        document.add(new Paragraph(" "));
        Paragraph reportTitle = new Paragraph("OFFICIAL RESULT CARD",
                new Font(Font.HELVETICA, 14, Font.BOLD));
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(reportTitle);

        document.add(new Paragraph("Generated on: " + java.time.LocalDate.now(),
                new Font(Font.HELVETICA, 9, Font.ITALIC, Color.GRAY)));
        document.add(new Paragraph(" "));

        // Student Info
        PdfPTable studentTable = new PdfPTable(2);
        studentTable.setWidthPercentage(100);
        studentTable.setSpacingBefore(10);

        studentTable.addCell(makeCell("Name:", true));
        studentTable.addCell(makeCell(student.getName(), false));

        studentTable.addCell(makeCell("Email:", true));
        studentTable.addCell(makeCell(student.getEmail(), false));

        studentTable.addCell(makeCell("Roll Number:", true));
        studentTable.addCell(makeCell(student.getRollNumber(), false));

        document.add(studentTable);
        document.add(new Paragraph(" "));

        // Subjects
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        PdfPCell header1 = new PdfPCell(new Phrase("Subject",
                new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
        header1.setBackgroundColor(new Color(52, 152, 219));
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header1.setPadding(6);

        PdfPCell header2 = new PdfPCell(new Phrase("Marks",
                new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE)));
        header2.setBackgroundColor(new Color(52, 152, 219));
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setPadding(6);

        table.addCell(header1);
        table.addCell(header2);

        for (Mark m : marks) {
            table.addCell(makeCell(m.getSubject().getName(), false));
            table.addCell(makeCell(String.valueOf(m.getMarks()), false));
        }

        document.add(table);
        document.add(new Paragraph(" "));

        // Summary
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setSpacingBefore(15);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);

        summaryTable.addCell(makeCell("Total Marks", true));
        summaryTable.addCell(makeCell(String.valueOf(totalMarks), false));

        summaryTable.addCell(makeCell("Percentage", true));
        summaryTable.addCell(makeCell(String.format("%.2f %%", percentage), false));

        PdfPCell resultCell = new PdfPCell(new Phrase("Final Result: " + resultStatus,
                new Font(Font.HELVETICA, 12, Font.BOLD,
                        resultStatus.equals("Pass") ? Color.GREEN : Color.RED)));
        resultCell.setColspan(2);
        resultCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryTable.addCell(resultCell);

        document.add(summaryTable);

        // Signature
        document.add(new Paragraph("\n\n\n"));
        Paragraph signature = new Paragraph("Pratyush Gupta \nPrincipal Signature",
                new Font(Font.HELVETICA, 11, Font.NORMAL));
        signature.setAlignment(Element.ALIGN_RIGHT);
        document.add(signature);

        document.close();
    }

    // Helper
    private PdfPCell makeCell(String text, boolean bold) {
        Font font = bold
                ? new Font(Font.HELVETICA, 11, Font.BOLD, Color.BLACK)
                : new Font(Font.HELVETICA, 11, Font.NORMAL, Color.BLACK);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setPadding(5);
        return cell;
    }
}
