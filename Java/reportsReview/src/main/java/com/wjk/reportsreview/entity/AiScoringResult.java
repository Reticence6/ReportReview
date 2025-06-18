package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class AiScoringResult {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "task_id")
    private String taskId;
    @TableField(value = "report_id")
    private int reportId;
    private String filename;
    @TableField(value = "student_id")
    private String studentId;
    private float score;
    private float confidence;
    private boolean confirmed;
    @TableField(value = "confirmed_by")
    private int confirmedBy;
    @TableField(value = "confirmed_at")
    private String confirmedAt;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;
    
    @TableField(exist = false)
    private AiScoringTask task;
    @TableField(exist = false)
    private Report report;
    @TableField(exist = false)
    private Student student;
    @TableField(exist = false)
    private User confirmer;

    public AiScoringResult() {
    }

    public AiScoringResult(int id, String taskId, int reportId, String filename, String studentId, float score, float confidence, boolean confirmed, int confirmedBy, String confirmedAt, String createdAt, String updatedAt) {
        this.id = id;
        this.taskId = taskId;
        this.reportId = reportId;
        this.filename = filename;
        this.studentId = studentId;
        this.score = score;
        this.confidence = confidence;
        this.confirmed = confirmed;
        this.confirmedBy = confirmedBy;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AiScoringResult(int id, String taskId, int reportId, String filename, String studentId, float score, float confidence, boolean confirmed, int confirmedBy, String confirmedAt, String createdAt, String updatedAt, AiScoringTask task, Report report, Student student, User confirmer) {
        this.id = id;
        this.taskId = taskId;
        this.reportId = reportId;
        this.filename = filename;
        this.studentId = studentId;
        this.score = score;
        this.confidence = confidence;
        this.confirmed = confirmed;
        this.confirmedBy = confirmedBy;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.task = task;
        this.report = report;
        this.student = student;
        this.confirmer = confirmer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public int getConfirmedBy() {
        return confirmedBy;
    }

    public void setConfirmedBy(int confirmedBy) {
        this.confirmedBy = confirmedBy;
    }

    public String getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(String confirmedAt) {
        this.confirmedAt = confirmedAt;
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

    public AiScoringTask getTask() {
        return task;
    }

    public void setTask(AiScoringTask task) {
        this.task = task;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public User getConfirmer() {
        return confirmer;
    }

    public void setConfirmer(User confirmer) {
        this.confirmer = confirmer;
    }

    @Override
    public String toString() {
        return "AiScoringResult{" +
                "id=" + id +
                ", taskId='" + taskId + '\'' +
                ", reportId=" + reportId +
                ", filename='" + filename + '\'' +
                ", studentId='" + studentId + '\'' +
                ", score=" + score +
                ", confidence=" + confidence +
                ", confirmed=" + confirmed +
                ", confirmedBy=" + confirmedBy +
                ", confirmedAt='" + confirmedAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", task=" + task +
                ", report=" + report +
                ", student=" + student +
                ", confirmer=" + confirmer +
                '}';
    }
} 