package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "user_id")
    private Integer userId;
    private String type;
    private String content;
    private String time;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;
    
    @TableField(exist = false)
    private User user;

    public ChatMessage() {
    }

    public ChatMessage(int id, Integer userId, String type, String content, String time, String createdAt, String updatedAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.time = time;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ChatMessage(int id, Integer userId, String type, String content, String time, String createdAt, String updatedAt, User user) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.content = content;
        this.time = time;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.user = user;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "id=" + id +
                ", userId=" + userId +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", time='" + time + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", user=" + user +
                '}';
    }
}
