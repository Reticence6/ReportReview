package com.wjk.reportsreview.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("report_attachment")
public class ReportAttachment {

    @TableId(type = IdType.AUTO)
    private int id;
    @TableField(value = "report_id")
    private int reportId;
    private String name;
    private String path;
    private int size;
    private String type;
    @TableField(value = "created_at")
    private String createdAt;
    @TableField(value = "updated_at")
    private String updatedAt;
    
    @TableField(exist = false)
    private Report report;

    public ReportAttachment() {
    }

    public ReportAttachment(int id, int reportId, String name, String path, int size, String type, String createdAt, String updatedAt) {
        this.id = id;
        this.reportId = reportId;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public ReportAttachment(int id, int reportId, String name, String path, int size, String type, String createdAt, String updatedAt, Report report) {
        this.id = id;
        this.reportId = reportId;
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.report = report;
    }

    @Override
    public String toString() {
        return "ReportAttachment{" +
                "id=" + id +
                ", reportId=" + reportId +
                ", name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", size=" + size +
                ", type='" + type + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", updatedAt='" + updatedAt + '\'' +
                ", report=" + report +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }
}
