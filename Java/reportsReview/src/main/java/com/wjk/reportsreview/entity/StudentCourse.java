package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class StudentCourse {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "student_id")
    private String studentId;
    @TableField(value = "course_id")
    private int courseId;
    private Float score;
    private String grade;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;
    
    @TableField(exist = false)
    private Student student;
    @TableField(exist = false)
    private Course course;

    public StudentCourse() {
    }

    public StudentCourse(int id, String studentId, int courseId, Float score, String grade, String createdAt, String updatedAt) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.grade = grade;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public StudentCourse(int id, String studentId, int courseId, Float score, String grade, String createdAt, String updatedAt, Student student, Course course) {
        this.id = id;
        this.studentId = studentId;
        this.courseId = courseId;
        this.score = score;
        this.grade = grade;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.student = student;
        this.course = course;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    @Override
    public String toString() {
        return "StudentCourse{" +
                "id=" + id +
                ", studentId='" + studentId + '\'' +
                ", courseId=" + courseId +
                ", score=" + score +
                ", grade='" + grade + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", student=" + student +
                ", course=" + course +
                '}';
    }
} 