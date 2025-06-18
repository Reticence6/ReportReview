package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class AiScoringTask {
    @TableId(type = IdType.INPUT)
    private String id;
    private String status;
    private int progress;
    private String message;
    @TableField(value = "created_by")
    private int createdBy;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;
    
    @TableField(exist = false)
    private User creator;

    public AiScoringTask() {
    }

    public AiScoringTask(String id, String status, int progress, String message, int createdBy, String createdAt, String updatedAt) {
        this.id = id;
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public AiScoringTask(String id, String status, int progress, String message, int createdBy, String createdAt, String updatedAt, User creator) {
        this.id = id;
        this.status = status;
        this.progress = progress;
        this.message = message;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.creator = creator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
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

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Override
    public String toString() {
        return "AiScoringTask{" +
                "id='" + id + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                ", message='" + message + '\'' +
                ", createdBy=" + createdBy +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", creator=" + creator +
                '}';
    }
} 