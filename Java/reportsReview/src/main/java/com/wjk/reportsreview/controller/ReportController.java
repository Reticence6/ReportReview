package com.wjk.reportsreview.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjk.reportsreview.common.Result;
import com.wjk.reportsreview.entity.Course;
import com.wjk.reportsreview.entity.Report;
import com.wjk.reportsreview.entity.Student;
import com.wjk.reportsreview.entity.User;
import com.wjk.reportsreview.mapper.CourseMapper;
import com.wjk.reportsreview.mapper.ReportMapper;
import com.wjk.reportsreview.mapper.StudentMapper;
import com.wjk.reportsreview.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;
import com.alibaba.fastjson.JSON;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportMapper reportMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * 获取报告列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param status 状态筛选
     * @param date 日期筛选
     * @param keyword 搜索关键字
     * @return 报告列表
     */
    @GetMapping
    public Result<Map<String, Object>> getReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) String keyword) {
        
        // 构建查询条件
        QueryWrapper<Report> queryWrapper = new QueryWrapper<>();
        
        // 添加状态筛选
        if (status != null && !status.isEmpty()) {
            int statusCode;
            switch (status) {
                case "pending":
                    statusCode = 0;
                    break;
                case "reviewed":
                    statusCode = 1;
                    break;
                case "rejected":
                    statusCode = 2;
                    break;
                case "ai":
                    statusCode = 3;
                    break;
                default:
                    statusCode = -1;
            }
            
            if (statusCode >= 0) {
                queryWrapper.eq("status", statusCode);
            }
        }
        
        // 添加日期筛选
        if (date != null && !date.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String endDate = now.format(formatter);
            String startDate;
            
            switch (date) {
                case "today":
                    startDate = now.withHour(0).withMinute(0).withSecond(0).format(formatter);
                    break;
                case "week":
                    startDate = now.minusWeeks(1).format(formatter);
                    break;
                case "month":
                    startDate = now.minusMonths(1).format(formatter);
                    break;
                default:
                    startDate = null;
            }
            
            if (startDate != null) {
                queryWrapper.between("submit_time", startDate, endDate);
            }
        }
        
        // 添加关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("title", keyword)
                    .or()
                    .inSql("student_id", "SELECT id FROM student WHERE name LIKE '%" + keyword + "%'")
            );
        }
        
        // 执行分页查询
        Page<Report> pageResult = new Page<>(page, pageSize);
        Page<Report> reportPage = reportMapper.selectPage(pageResult, queryWrapper);
        
        // 为每个报告关联学生信息
        for (Report report : reportPage.getRecords()) {
            // 查询关联的学生信息
            Student student = studentMapper.selectById(report.getStudentId());
            if (student != null) {
                report.setStudent(student);
            }
        }
        
        // 组装返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", reportPage.getTotal());
        resultMap.put("list", reportPage.getRecords());
        
        return Result.success(resultMap);
    }
    
    /**
     * 获取报告详情
     * @param id 报告ID
     * @return 报告详情
     */
    @GetMapping("/{id}")
    public Result<Report> getReportById(@PathVariable Integer id) {
        // 查询报告基本信息
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error(1001, "报告不存在");
        }
        
        // 查询关联的学生信息
        Student student = studentMapper.selectById(report.getStudentId());
        report.setStudent(student);
        
        // 查询关联的课程信息
        Course course = courseMapper.selectById(report.getCourseId());
        report.setCourse(course);
        
        // 查询关联的评审人信息
        if (report.getReviewerId() != null && report.getReviewerId() > 0) {
            User reviewer = userMapper.selectById(report.getReviewerId());
            report.setReviewer(reviewer);
        }
        
        // 处理文件路径，确保前端可以直接访问
        if (report.getContent() != null && !report.getContent().isEmpty()) {
            // 如果路径不是以http或https开头，添加完整服务器地址
            if (!report.getContent().startsWith("http://") && !report.getContent().startsWith("https://")) {
                // 构建完整URL
                String serverUrl = "http://localhost:8080"; // 可以从配置中获取
                String filePath = serverUrl + "/" + report.getContent();
                report.setContent(filePath);
            }
        }
        
        return Result.success(report);
    }
    
    /**
     * 下载报告文件
     * @param id 报告ID
     * @return 文件流
     */
    @GetMapping("/{id}/download")
    public void downloadReport(@PathVariable Integer id, HttpServletResponse response) {
        try {
            // 查询报告信息
            Report report = reportMapper.selectById(id);
            if (report == null || report.getContent() == null || report.getContent().isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("文件不存在");
                return;
            }
            
            // 获取文件路径
            String filePath = report.getContent();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            
            // 构建完整的文件路径
            String projectPath = System.getProperty("user.dir");
            String fullPath = projectPath + "/src/main/resources/" + filePath;
            File file = new File(fullPath);
            
            // 检查文件是否存在
            if (!file.exists()) {
                // 尝试从运行时目录获取
                fullPath = projectPath + "/target/classes/" + filePath;
                file = new File(fullPath);
                
                if (!file.exists()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write("文件不存在");
                    return;
                }
            }
            
            // 设置响应头
            String fileName = file.getName();
            if (fileName.contains("_")) {
                fileName = fileName.substring(fileName.indexOf("_") + 1);
            }
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                    URLEncoder.encode(fileName, "UTF-8"));
            response.setContentLength((int) file.length());
            
            // 输出文件流
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 OutputStream os = response.getOutputStream()) {
                
                byte[] buffer = new byte[1024];
                int i;
                while ((i = bis.read(buffer)) != -1) {
                    os.write(buffer, 0, i);
                }
                os.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("下载失败: " + e.getMessage());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * 获取报告文件内容预览
     * @param id 报告ID
     * @return 文件内容预览
     */
    @GetMapping("/{id}/preview")
    public Result<Map<String, Object>> previewReport(@PathVariable Integer id) {
        try {
            // 查询报告信息
            Report report = reportMapper.selectById(id);
            if (report == null || report.getContent() == null || report.getContent().isEmpty()) {
                return Result.error(1001, "文件不存在");
            }
            
            // 获取文件路径
            String filePath = report.getContent();
            if (filePath.startsWith("/")) {
                filePath = filePath.substring(1);
            }
            
            // 构建完整的文件路径
            String projectPath = System.getProperty("user.dir");
            String fullPath = projectPath + "/src/main/resources/" + filePath;
            File file = new File(fullPath);
            
            // 检查文件是否存在
            if (!file.exists()) {
                // 尝试从运行时目录获取
                fullPath = projectPath + "/target/classes/" + filePath;
                file = new File(fullPath);
                
                if (!file.exists()) {
                    return Result.error(1001, "文件不存在");
                }
            }
            
            // 获取文件类型
            String fileName = file.getName();
            String fileType = "unknown";
            if (fileName.contains(".")) {
                fileType = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
            }
            
            // 文件内容预览
            Map<String, Object> data = new HashMap<>();
            data.put("fileName", fileName);
            data.put("fileSize", file.length());
            data.put("fileType", fileType);
            data.put("filePath", "/" + filePath);
            
            // 根据文件类型返回不同的预览信息
            data.put("content", "此文件类型不支持内容预览");
            data.put("previewType", "none");
            
            return Result.success(data);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(1002, "获取文件内容失败: " + e.getMessage());
        }
    }
    
    /**
     * 上传报告
     * @param title 报告标题
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @param file 报告文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadReport(
            @RequestParam String title,
            @RequestParam String studentId,
            @RequestParam Integer courseId,
            @RequestParam MultipartFile file) {
        
        // 校验参数
        if (title == null || title.isEmpty() || studentId == null || studentId.isEmpty() || courseId == null) {
            return Result.error(1002, "参数不完整");
        }
        
        // 检查学生是否存在
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            return Result.error(1003, "学生不存在");
        }
        
        // 检查课程是否存在
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            return Result.error(1004, "课程不存在");
        }
        
        // 保存文件到 uploads 目录
        String fileName = file.getOriginalFilename();
        
        try {
            // 1. 保存到源代码目录 (开发环境)
            String projectPath = System.getProperty("user.dir");
            String uploadDir = projectPath + "/src/main/resources/uploads";
            
            // 确保目录存在
            java.io.File dir = new java.io.File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    System.err.println("警告：无法创建源代码目录: " + uploadDir);
                }
            }
            
            // 2. 同时保存到运行时目录 (生产环境)
            String runtimeDir = projectPath + "/target/classes/uploads";
            java.io.File runtimeDirFile = new java.io.File(runtimeDir);
            if (!runtimeDirFile.exists()) {
                boolean created = runtimeDirFile.mkdirs();
                if (!created) {
                    System.err.println("警告：无法创建运行时目录: " + runtimeDir);
                }
            }
            
            // 生成唯一文件名，避免文件覆盖
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            
            // 保存到源代码目录
            String srcFilePath = uploadDir + "/" + uniqueFileName;
            java.io.File destFile = new java.io.File(srcFilePath);
            file.transferTo(destFile);
            
            // 复制到运行时目录
            String runtimeFilePath = runtimeDir + "/" + uniqueFileName;
            java.io.File runtimeFile = new java.io.File(runtimeFilePath);
            java.nio.file.Files.copy(destFile.toPath(), runtimeFile.toPath(), 
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // 使用相对路径存储到数据库，便于通过URL访问
            String dbFilePath = "uploads/" + uniqueFileName;
            
            // 创建报告记录
            Report report = new Report();
            report.setTitle(title);
            report.setStudentId(studentId);
            report.setCourseId(courseId);
            report.setSubmitTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            report.setContent(dbFilePath); // 保存相对路径
            report.setStatus(0); // 待审核状态
            
            // 保存报告
            int result = reportMapper.insert(report);
            if (result > 0) {
                Map<String, Object> data = new HashMap<>();
                data.put("id", report.getId());
                data.put("title", report.getTitle());
                data.put("status", "pending");
                data.put("filePath", dbFilePath);
                return Result.success("上传成功", data);
            } else {
                return Result.error(1005, "上传失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(1006, "文件保存失败: " + e.getMessage());
        }
    }
    
    /**
     * 提交报告评分
     * @param id 报告ID
     * @param scoreMap 评分信息
     * @return 评分结果
     */
    @PostMapping("/{id}/score")
    public Result<Map<String, Object>> scoreReport(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> scoreMap) {
        
        // 查询报告
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error(1001, "报告不存在");
        }
        
        // 解析评分参数
        Float score = Float.parseFloat(scoreMap.get("score").toString());
        String feedback = (String) scoreMap.get("feedback");
        
        // 处理scoreDetails，确保是有效的JSON
        String scoreDetails = null;
        try {
            Object scoreDetailsObj = scoreMap.get("scoreDetails");
            if (scoreDetailsObj != null) {
                // 如果是JSONObject或Map类型，使用com.alibaba.fastjson.JSON转换为字符串
                if (scoreDetailsObj instanceof Map) {
                    scoreDetails = JSON.toJSONString(scoreDetailsObj);
                } else {
                    // 如果已经是字符串，验证其JSON格式正确性
                    String detailsStr = scoreDetailsObj.toString();
                    // 通过解析再序列化的方式确保JSON格式正确
                    JSON.parse(detailsStr); // 这会抛出异常如果JSON格式不正确
                    scoreDetails = detailsStr;
                }
            } else {
                // 如果scoreDetails为null，设置为空JSON对象
                scoreDetails = "{}";
            }
        } catch (Exception e) {
            return Result.error(1002, "评分详情JSON格式不正确: " + e.getMessage());
        }
        
        // 更新报告评分
        report.setScore(score);
        report.setFeedback(feedback);
        report.setScoreDetails(scoreDetails);
        report.setStatus(1); // 已评审状态
        report.setReviewTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        report.setReviewerId(1); // 实际项目中应该从当前登录用户获取
        
        // 保存评分
        int result = reportMapper.updateById(report);
        if (result > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", report.getId());
            data.put("score", report.getScore());
            data.put("status", "reviewed");
            return Result.success("评分成功", data);
        } else {
            return Result.error(1006, "评分失败");
        }
    }

    /**
     * 删除报告
     * @param id 报告ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteReport(@PathVariable Integer id) {
        // 查询报告信息
        Report report = reportMapper.selectById(id);
        if (report == null) {
            return Result.error(1001, "报告不存在");
        }
        
        try {
            // 如果存在文件，尝试删除文件
            if (report.getContent() != null && !report.getContent().isEmpty()) {
                String filePath = report.getContent();
                // 去除可能存在的URL前缀
                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    int pathStart = filePath.indexOf("/", 8); // 找到域名后的第一个斜杠
                    if (pathStart > 0) {
                        filePath = filePath.substring(pathStart);
                    }
                }
                
                if (filePath.startsWith("/")) {
                    filePath = filePath.substring(1);
                }
                
                // 尝试删除源代码目录中的文件
                String projectPath = System.getProperty("user.dir");
                String srcFilePath = projectPath + "/src/main/resources/" + filePath;
                File srcFile = new File(srcFilePath);
                if (srcFile.exists()) {
                    srcFile.delete();
                }
                
                // 尝试删除运行时目录中的文件
                String runtimeFilePath = projectPath + "/target/classes/" + filePath;
                File runtimeFile = new File(runtimeFilePath);
                if (runtimeFile.exists()) {
                    runtimeFile.delete();
                }
            }
            
            // 删除数据库记录
            int result = reportMapper.deleteById(id);
            if (result > 0) {
                return Result.success("删除成功");
            } else {
                return Result.error(1007, "删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(1008, "删除失败: " + e.getMessage());
        }
    }
} 
 