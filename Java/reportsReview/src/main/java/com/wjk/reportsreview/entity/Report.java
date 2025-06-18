package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class Report {
    @TableId(type = IdType.AUTO)
    private int id;
    private String title;
    @TableField(value = "student_id")
    private String studentId;
    @TableField(value = "course_id")
    private int courseId;
    @TableField(value = "submit_time")
    private String submitTime;
    private String content;
    private int status;
    private float score;
    private String feedback;
    @TableField(value = "score_details")
    private String scoreDetails;
    @TableField(value = "reviewer_id")
    private Integer reviewerId;
    @TableField(value = "review_time")
    private String reviewTime;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    @TableField(exist = false)
    private User reviewer;
    @TableField(exist = false)
    private Student student;
    @TableField(exist = false)
    private Course course;

    public Report() {
    }

    public Report(int id, String title, String studentId, int courseId, String submitTime, String content, int status, float score, String feedback, String scoreDetails, Integer reviewerId, String reviewTime, String createdAt, String updatedAt) {
        this.id = id;
        this.title = title;
        this.studentId = studentId;
        this.courseId = courseId;
        this.submitTime = submitTime;
        this.content = content;
        this.status = status;
        this.score = score;
        this.feedback = feedback;
        this.scoreDetails = scoreDetails;
        this.reviewerId = reviewerId;
        this.reviewTime = reviewTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Report(int id, String title, String studentId, int courseId, String submitTime, String content, int status, float score, String feedback, String scoreDetails, Integer reviewerId, String reviewTime, String createdAt, String updatedAt, User reviewer, Student student, Course course) {
        this.id = id;
        this.title = title;
        this.studentId = studentId;
        this.courseId = courseId;
        this.submitTime = submitTime;
        this.content = content;
        this.status = status;
        this.score = score;
        this.feedback = feedback;
        this.scoreDetails = scoreDetails;
        this.reviewerId = reviewerId;
        this.reviewTime = reviewTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.reviewer = reviewer;
        this.student = student;
        this.course = course;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public String getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(String submitTime) {
        this.submitTime = submitTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getScoreDetails() {
        return scoreDetails;
    }

    public void setScoreDetails(String scoreDetails) {
        this.scoreDetails = scoreDetails;
    }

    public Integer getReviewerId() {
        return reviewerId;
    }

    public void setReviewerId(Integer reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String getReviewTime() {
        return reviewTime;
    }

    public void setReviewTime(String reviewTime) {
        this.reviewTime = reviewTime;
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

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
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
        return "Report{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseId=" + courseId +
                ", submitTime='" + submitTime + '\'' +
                ", content='" + content + '\'' +
                ", status=" + status +
                ", score=" + score +
                ", feedback='" + feedback + '\'' +
                ", scoreDetails='" + scoreDetails + '\'' +
                ", reviewerId=" + reviewerId +
                ", reviewTime='" + reviewTime + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", reviewer=" + reviewer +
                ", student=" + student +
                ", course=" + course +
                '}';
    }
}
