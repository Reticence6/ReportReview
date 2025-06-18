package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

public class AiScoringSettings {
    @TableId(type = IdType.AUTO)
    private int id;
    private boolean enabled;
    private String model;
    private int threshold;
    @TableField(value = "human_review")
    private boolean humanReview;
    private String criteria;
    @TableField(exist = false)
    private boolean autoFeedback = true;
    @TableField(exist = false)
    private String feedbackStyle = "detailed";
    @TableField(exist = false)
    private int feedbackLength = 200;
    @TableField(exist = false)
    private boolean includeSuggestions = true;
    private String advanced;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;

    public AiScoringSettings() {
    }

    public AiScoringSettings(int id, boolean enabled, String model, int threshold, boolean humanReview, String criteria, boolean autoFeedback, String feedbackStyle, int feedbackLength, boolean includeSuggestions, String advanced, String createdAt, String updatedAt) {
        this.id = id;
        this.enabled = enabled;
        this.model = model;
        this.threshold = threshold;
        this.humanReview = humanReview;
        this.criteria = criteria;
        this.autoFeedback = autoFeedback;
        this.feedbackStyle = feedbackStyle;
        this.feedbackLength = feedbackLength;
        this.includeSuggestions = includeSuggestions;
        this.advanced = advanced;
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

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public boolean isAutoFeedback() {
        return autoFeedback;
    }

    public void setAutoFeedback(boolean autoFeedback) {
        this.autoFeedback = autoFeedback;
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

    public String getAdvanced() {
        return advanced;
    }

    public void setAdvanced(String advanced) {
        this.advanced = advanced;
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
        return "AiScoringSettings{" +
                "id=" + id +
                ", enabled=" + enabled +
                ", model='" + model + '\'' +
                ", threshold=" + threshold +
                ", humanReview=" + humanReview +
                ", criteria='" + criteria + '\'' +
                ", autoFeedback=" + autoFeedback +
                ", feedbackStyle='" + feedbackStyle + '\'' +
                ", feedbackLength=" + feedbackLength +
                ", includeSuggestions=" + includeSuggestions +
                ", advanced='" + advanced + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }
} 