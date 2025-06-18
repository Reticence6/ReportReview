package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("ai_scoring_settings")
public class AIScoreSetting {
    @TableId(type = IdType.AUTO)
    private int id;
    private boolean enabled;
    private String model;
    private int threshold;
    @TableField(value = "human_review")
    private boolean humanReview;
    // JSON-criteria
    @TableField(value = "feedback_style")
    private String feedbackStyle;
    @TableField(value = "feedback_length")
    private int feedbackLength;
    @TableField(value = "include_suggestions")
    private boolean includeSuggestions;
    // JSON-advanced
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    public AIScoreSetting() {
    }

    public AIScoreSetting(int id, boolean enabled, String model, int threshold, boolean humanReview, String feedbackStyle, int feedbackLength, boolean includeSuggestions, String createdAt, String updatedAt) {
        this.id = id;
        this.enabled = enabled;
        this.model = model;
        this.threshold = threshold;
        this.humanReview = humanReview;
        this.feedbackStyle = feedbackStyle;
        this.feedbackLength = feedbackLength;
        this.includeSuggestions = includeSuggestions;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isHumanReview() {
        return humanReview;
    }

    public void setHumanReview(boolean humanReview) {
        this.humanReview = humanReview;
    }

    public String getFeedbackStyle() {
        return feedbackStyle;
    }

    public void setFeedbackStyle(String feedbackStyle) {
        this.feedbackStyle = feedbackStyle;
    }

    public int getFeedbackLength() {
        return feedbackLength;
    }

    public void setFeedbackLength(int feedbackLength) {
        this.feedbackLength = feedbackLength;
    }

    public boolean isIncludeSuggestions() {
        return includeSuggestions;
    }

    public void setIncludeSuggestions(boolean includeSuggestions) {
        this.includeSuggestions = includeSuggestions;
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
        return "AIScoreSetting{" +
                "id=" + id +
                ", enabled=" + enabled +
                ", model='" + model + '\'' +
                ", threshold=" + threshold +
                ", humanReview=" + humanReview +
                ", feedbackStyle='" + feedbackStyle + '\'' +
                ", feedbackLength=" + feedbackLength +
                ", includeSuggestions=" + includeSuggestions +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
}

