package com.example.studentdb.dto;

import com.example.studentdb.entity.Student;

public class StudentResultDTO {
    private Student student;
    private int totalMarks;
    private double percentage;
    private String resultStatus;

    public StudentResultDTO(Student student, int totalMarks, double percentage, String resultStatus) {
        this.student = student;
        this.totalMarks = totalMarks;
        this.percentage = percentage;
        this.resultStatus = resultStatus;
    }

    // Getters
    public Student getStudent() { return student; }
    public int getTotalMarks() { return totalMarks; }
    public double getPercentage() { return percentage; }
    public String getResultStatus() { return resultStatus; }
}
