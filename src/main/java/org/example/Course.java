package org.example;

public class Course {
    private int id;
    private int professorId;
    private String subject;
    private String title;
    private String courseNumber;
    private String semesterOffered;
    private int creditHours;
    private int numberOfStudents;

    public Course() {
    }

    public Course(int id, int professorId, String subject, String title, String courseNumber, String semesterOffered, int creditHours, int numberOfStudents) {
        this.id = id;
        this.professorId = professorId;
        this.subject = subject;
        this.title = title;
        this.courseNumber = courseNumber;
        this.semesterOffered = semesterOffered;
        this.creditHours = creditHours;
        this.numberOfStudents = numberOfStudents;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProfessorId() {
        return professorId;
    }

    public void setProfessorId(int professorId) {
        this.professorId = professorId;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getSemesterOffered() {
        return semesterOffered;
    }

    public void setSemesterOffered(String semesterOffered) {
        this.semesterOffered = semesterOffered;
    }

    public int getCreditHours() {
        return creditHours;
    }

    public void setCreditHours(int creditHours) {
        this.creditHours = creditHours;
    }

    public int getNumberOfStudents() {
        return numberOfStudents;
    }

    public void setNumberOfStudents(int numberOfStudents) {
        this.numberOfStudents = numberOfStudents;
    }
}
