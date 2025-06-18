package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.List;

public class Student {
    @TableId(type = IdType.INPUT)
    private String id;
    private String name;
    private int gender;
    private String grade;
    private String major;
    @TableField(value = "class_name")
    private String className;
    private String phone;
    private String email;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    // courses
    @TableField(exist = false)
    private List<Course> courses;
    // reports
    @TableField(exist = false)
    private List<Report> reports;

    public Student() {
    }

    public Student(String id, String name, int gender, String grade, String major, String className, String phone, String email, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.grade = grade;
        this.major = major;
        this.className = className;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Student(String id, String name, int gender, String grade, String major, String className, String phone, String email, String createdAt, String updatedAt, List<Course> courses, List<Report> reports) {
        this.id = id;
        this.name = name;
        this.gender = gender;
        this.grade = grade;
        this.major = major;
        this.className = className;
        this.phone = phone;
        this.email = email;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.courses = courses;
        this.reports = reports;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public int getGender() {
        return gender;
    }

    public void setGender(int gender) {
        this.gender = gender;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", gender=" + gender +
                ", grade='" + grade + '\'' +
                ", major='" + major + '\'' +
                ", className='" + className + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", courses=" + courses +
                ", reports=" + reports +
                '}';
    }
}
