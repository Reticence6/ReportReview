package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ai_scoring_result")
public class AIScoreResult {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(exist = false)
    private AIScoreTask aiScoreTask;
    @TableField(exist = false)
    private Report report;
    private String filename;
    @TableField(exist = false)
    private Student student;
    private int score;
    private String confidence;
    private boolean confirmed;
    @TableField(exist = false)
    private User confirmedUser;
    @TableField(value = "confirmed_at")
    private String confirmedAt;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    public AIScoreResult() {
    }

    public AIScoreResult(int id, AIScoreTask aiScoreTask, Report report, String filename, Student student, int score, String confidence, boolean confirmed, User confirmedUser, String confirmedAt, String createdAt, String updatedAt) {
        this.id = id;
        this.aiScoreTask = aiScoreTask;
        this.report = report;
        this.filename = filename;
        this.student = student;
        this.score = score;
        this.confidence = confidence;
        this.confirmed = confirmed;
        this.confirmedUser = confirmedUser;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AIScoreTask getAiScoreTask() {
        return aiScoreTask;
    }

    public void setAiScoreTask(AIScoreTask aiScoreTask) {
        this.aiScoreTask = aiScoreTask;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public User getConfirmedUser() {
        return confirmedUser;
    }

    public void setConfirmedUser(User confirmedUser) {
        this.confirmedUser = confirmedUser;
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

    @Override
    public String toString() {
        return "AIScoreResult{" +
                "id=" + id +
                ", aiScoreTask=" + aiScoreTask +
                ", report=" + report +
                ", filename='" + filename + '\'' +
                ", student=" + student +
                ", score=" + score +
                ", confidence='" + confidence + '\'' +
                ", confirmed=" + confirmed +
                ", confirmedUser=" + confirmedUser +
                ", confirmedAt='" + confirmedAt + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}
