package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.util.List;

public class Course {
    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    private int teacherId;
    private String semester;
    private float credit;
    private String description;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    // students
    @TableField(exist = false)
    private List<Student> students;
    // teacher
    @TableField(exist = false)
    private User teacher;
    // reports
    @TableField(exist = false)
    private List<Report> reports;

    public Course() {
    }

    public Course(int id, String name, int teacherId, String semester, float credit, String description, String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.semester = semester;
        this.credit = credit;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Course(int id, String name, int teacherId, String semester, float credit, String description, String createdAt, String updatedAt, List<Student> students, User teacher, List<Report> reports) {
        this.id = id;
        this.name = name;
        this.teacherId = teacherId;
        this.semester = semester;
        this.credit = credit;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.students = students;
        this.teacher = teacher;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    public String getSemester() {
        return semester;
    }

    public void setSemester(String semester) {
        this.semester = semester;
    }

    public int getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(int teacherId) {
        this.teacherId = teacherId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public User getTeacher() {
        return teacher;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public List<Report> getReports() {
        return reports;
    }

    public void setReports(List<Report> reports) {
        this.reports = reports;
    }

    @Override
    public String toString() {
        return "Course{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", teacherId=" + teacherId +
                ", semester='" + semester + '\'' +
                ", credit=" + credit +
                ", description='" + description + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", students=" + students +
                ", teacher=" + teacher +
                ", reports=" + reports +
                '}';
    }
}
