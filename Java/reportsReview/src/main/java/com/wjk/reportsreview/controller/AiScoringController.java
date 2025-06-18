package com.wjk.reportsreview.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;
import com.wjk.reportsreview.common.Result;
import com.wjk.reportsreview.entity.AiScoringTask;
import com.wjk.reportsreview.entity.Report;
import com.wjk.reportsreview.mapper.AiScoringTaskMapper;
import com.wjk.reportsreview.mapper.ReportMapper;
import com.wjk.reportsreview.service.AiScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/ai-scoring")
public class AiScoringController {
    
    @Autowired
    private AiScoringTaskMapper aiScoringTaskMapper;
    
    @Autowired
    private ReportMapper reportMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Autowired
    private AiScoringService aiScoringService;
    
    @Value("${qianwen.api.key}")
    private String apiKey;
    
    @Value("${qianwen.api.url}")
    private String apiUrl;
    
    @Value("${dashscope.api.key:${qianwen.api.key}}")
    private String dashscopeApiKey;

    /**
     * 获取AI评分任务状态
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task/{taskId}")
    public Result<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        // 查询任务
        AiScoringTask task = aiScoringTaskMapper.selectById(taskId);
        if (task == null) {
            return Result.error(1002, "任务不存在");
        }
        
        // 组装返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("taskId", task.getId());
        resultMap.put("status", task.getStatus());
        resultMap.put("progress", task.getProgress());
        resultMap.put("message", task.getMessage());
        
        System.out.println("获取任务状态: " + taskId + ", 当前状态: " + task.getStatus() + ", 进度: " + task.getProgress());
        
        // 如果任务已完成，查询评分结果
        if ("2".equals(task.getStatus())) {
            System.out.println("任务已完成，开始查询评分结果");
            
            // 查询与任务创建时间接近的已评分报告
            // 这里是简化处理，实际项目中应该有任务与报告的关联表
            // 假设任务的创建时间和报告的更新时间差在5分钟以内
            LocalDateTime taskCreateTime = LocalDateTime.parse(
                task.getCreatedAt(), 
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            );
            
            LocalDateTime startTime = taskCreateTime.minusMinutes(1);
            LocalDateTime endTime = taskCreateTime.plusMinutes(15); // 增加时间窗口到15分钟
            
            String startTimeStr = startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String endTimeStr = endTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            System.out.println("查询时间范围: " + startTimeStr + " 到 " + endTimeStr);
            
            // 查询在指定时间范围内更新的、状态为AI评分(3)的报告
            List<Report> reports = reportMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Report>()
                    .eq("status", 3)
                    .between("updated_at", startTimeStr, endTimeStr)
                    .orderByDesc("updated_at")
            );
            
            System.out.println("找到符合条件的报告数量: " + reports.size());
            
            // 如果没找到报告，尝试使用更宽松的条件再次查询
            if (reports.isEmpty()) {
                System.out.println("未找到报告，使用更宽松条件重新查询");
                LocalDateTime extendedStartTime = taskCreateTime.minusMinutes(30);
                LocalDateTime extendedEndTime = taskCreateTime.plusMinutes(30);
                
                String extStartTimeStr = extendedStartTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                String extEndTimeStr = extendedEndTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
                System.out.println("扩展查询时间范围: " + extStartTimeStr + " 到 " + extEndTimeStr);
                
                reports = reportMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Report>()
                        .eq("status", 3)
                        .between("updated_at", extStartTimeStr, extEndTimeStr)
                        .orderByDesc("updated_at")
                );
                System.out.println("扩展查询后找到报告数量: " + reports.size());
            }
            
            // 如果还是没找到，尝试不限制时间，只按状态查询最新的几条
            if (reports.isEmpty()) {
                System.out.println("扩展查询仍未找到报告，查询最新的AI评分报告");
                reports = reportMapper.selectList(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Report>()
                        .eq("status", 3)
                        .orderByDesc("updated_at")
                        .last("limit 5")
                );
                System.out.println("最终查询找到的报告数量: " + reports.size());
            }
            
            List<Map<String, Object>> results = new ArrayList<>();
            for (Report report : reports) {
                Map<String, Object> result = new HashMap<>();
                result.put("id", report.getId());
                result.put("filename", report.getTitle());
                result.put("student", report.getStudentId()); // 这里应该查询学生信息，现在简化处理
                result.put("score", report.getScore());
                result.put("feedback", report.getFeedback());
                result.put("scoreDetails", report.getScoreDetails());
                result.put("reportId", report.getId());
                results.add(result);
                
                System.out.println("添加评分结果 - 报告ID: " + report.getId() + 
                                  ", 标题: " + report.getTitle() + 
                                  ", 分数: " + report.getScore() + 
                                  ", 更新时间: " + report.getUpdatedAt());
            }
            
            resultMap.put("results", results);
            System.out.println("返回评分结果数量: " + results.size());
        }
        
        return Result.success(resultMap);
    }

    /**
     * 确认AI评分结果
     * @param reportId 报告ID
     * @return 确认结果
     */
    @PostMapping("/confirm/{reportId}")
    public Result<?> confirmAiScore(@PathVariable Integer reportId) {
        // 查询报告
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            return Result.error(1003, "报告不存在");
        }
        
        // 检查报告状态是否为AI评分
        if (report.getStatus() != 3) {
            return Result.error(1004, "报告状态不是AI评分");
        }
        
        // 更新报告状态为已评审
        report.setStatus(1);
        report.setReviewTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        reportMapper.updateById(report);
        
        return Result.success("确认成功");
    }    

    /**
     * AI评分报告
     * @param requestBody 包含reportId或reportIds的请求体
     * @return 评分任务ID
     */
    @PostMapping("/score")
    public Result<Map<String, Object>> scoreReports(@RequestBody Map<String, Object> requestBody) {
        List<Integer> reportIds = new ArrayList<>();
        
        // 处理不同格式的请求体
        if (requestBody.containsKey("reportId")) {
            // 单个报告ID
            Object reportIdObj = requestBody.get("reportId");
            if (reportIdObj instanceof Integer) {
                reportIds.add((Integer) reportIdObj);
            } else if (reportIdObj instanceof String) {
                try {
                    reportIds.add(Integer.parseInt((String) reportIdObj));
                } catch (NumberFormatException e) {
                    return Result.error(1001, "报告ID格式不正确");
                }
            } else {
                return Result.error(1001, "报告ID格式不正确");
            }
        } else if (requestBody.containsKey("reportIds")) {
            // 报告ID数组
            Object reportIdsObj = requestBody.get("reportIds");
            if (reportIdsObj instanceof List) {
                List<?> list = (List<?>) reportIdsObj;
                for (Object item : list) {
                    if (item instanceof Integer) {
                        reportIds.add((Integer) item);
                    } else if (item instanceof String) {
                        try {
                            reportIds.add(Integer.parseInt((String) item));
                        } catch (NumberFormatException e) {
                            // 忽略无效的ID
                        }
                    }
                }
            }
        }
        
        // 校验参数
        if (reportIds.isEmpty()) {
            return Result.error(1001, "请提供有效的报告ID");
        }
        
        System.out.println("开始AI批改，报告ID: " + reportIds);
        
        // 创建评分任务
        String taskId = "task" + System.currentTimeMillis();
        AiScoringTask task = new AiScoringTask();
        task.setId(taskId);
        task.setStatus("1"); // 1表示处理中，使用字符串形式的数字
        task.setProgress(0);
        task.setMessage("任务已创建，等待处理");
        task.setCreatedBy(1); // 实际项目中应该从当前登录用户获取
        task.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        task.setUpdatedAt(task.getCreatedAt());
        
        // 保存任务
        aiScoringTaskMapper.insert(task);
        
        // 调用服务类处理评分任务
        System.out.println("准备启动异步任务处理，将调用AiScoringService...");
        aiScoringService.processAiScoringTask(taskId, reportIds);
        System.out.println("异步任务已提交，继续执行...");
        
        // 返回任务ID
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("taskId", taskId);
        
        return Result.success("评分任务已创建", resultMap);
    }

    /**
     * 测试接口 - 同步调用AI批改（仅用于诊断问题）
     * @param reportId 报告ID
     * @return 批改结果
     */
    @GetMapping("/test/{reportId}")
    public Result<?> testAiScoring(@PathVariable Integer reportId) {
        try {
            System.out.println("开始同步测试AI批改，报告ID: " + reportId);
            
            // 查询报告
            Report report = reportMapper.selectById(reportId);
            if (report == null) {
                return Result.error(1001, "报告不存在");
            }
            
            // 直接在控制器中执行批改，不使用异步
            System.out.println("准备直接调用AI服务，跳过异步处理");
            Map<String, Object> result = aiScoringService.testScoreReportWithAI(report);
            System.out.println("同步AI批改完成，得分: " + result.get("score"));
            
            return Result.success("AI批改完成", result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(5001, "AI批改测试失败: " + e.getMessage());
        }
    }

    /**
     * 获取报告状态
     * @param taskId 任务ID（可选）
     * @param reportId 报告ID（可选）
     * @return 状态信息
     */
    @GetMapping("/status")
    public Result<Map<String, Object>> getStatus(
            @RequestParam(value = "taskId", required = false) String taskId,
            @RequestParam(value = "reportId", required = false) Integer reportId) {
        
        Map<String, Object> result = new HashMap<>();
        
        // 检查任务状态
        if (taskId != null && !taskId.isEmpty()) {
            AiScoringTask task = aiScoringTaskMapper.selectById(taskId);
            if (task != null) {
                Map<String, Object> taskInfo = new HashMap<>();
                taskInfo.put("id", task.getId());
                taskInfo.put("status", task.getStatus());
                taskInfo.put("progress", task.getProgress());
                taskInfo.put("message", task.getMessage());
                taskInfo.put("createdAt", task.getCreatedAt());
                taskInfo.put("updatedAt", task.getUpdatedAt());
                result.put("taskInfo", taskInfo);
                
                System.out.println("任务状态: " + taskId + " = " + task.getStatus() + " (" + task.getMessage() + ")");
            } else {
                result.put("taskError", "任务未找到: " + taskId);
            }
        }
        
        // 检查报告状态
        if (reportId != null) {
            Report report = reportMapper.selectById(reportId);
            if (report != null) {
                Map<String, Object> reportInfo = new HashMap<>();
                reportInfo.put("id", report.getId());
                reportInfo.put("title", report.getTitle());
                reportInfo.put("status", report.getStatus());
                reportInfo.put("score", report.getScore());
                reportInfo.put("hasContent", report.getContent() != null && !report.getContent().isEmpty());
                reportInfo.put("contentLength", report.getContent() != null ? report.getContent().length() : 0);
                reportInfo.put("hasFeedback", report.getFeedback() != null && !report.getFeedback().isEmpty());
                reportInfo.put("feedbackLength", report.getFeedback() != null ? report.getFeedback().length() : 0);
                reportInfo.put("hasScoreDetails", report.getScoreDetails() != null && !report.getScoreDetails().isEmpty());
                reportInfo.put("updatedAt", report.getUpdatedAt());
                result.put("reportInfo", reportInfo);
                
                System.out.println("报告状态: " + reportId + " = " + report.getStatus() + ", 分数: " + report.getScore() + 
                                  ", 内容长度: " + (report.getContent() != null ? report.getContent().length() : 0) + 
                                  ", 反馈长度: " + (report.getFeedback() != null ? report.getFeedback().length() : 0));
            } else {
                result.put("reportError", "报告未找到: " + reportId);
            }
        }
        
        // 获取最近5个AI评分状态的报告
        List<Report> recentReports = reportMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<Report>()
                .eq("status", 3)
                .orderByDesc("updated_at")
                .last("limit 5")
        );
        
        List<Map<String, Object>> recentList = new ArrayList<>();
        for (Report report : recentReports) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", report.getId());
            info.put("title", report.getTitle());
            info.put("score", report.getScore());
            info.put("updatedAt", report.getUpdatedAt());
            recentList.add(info);
        }
        result.put("recentAiReports", recentList);
        
        System.out.println("找到 " + recentReports.size() + " 个最近AI评分的报告");
        
        return Result.success(result);
    }

    /**
     * 检查报告文件路径
     * @param reportId 报告ID
     * @return 文件路径检查结果
     */
    @GetMapping("/check-file/{reportId}")
    public Result<?> checkReportFilePath(@PathVariable Integer reportId) {
        try {
            // 查询报告
            Report report = reportMapper.selectById(reportId);
            if (report == null) {
                return Result.error(1001, "报告不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("reportId", report.getId());
            result.put("title", report.getTitle());
            
            // 检查文件路径
            if (report.getContent() == null || report.getContent().trim().isEmpty()) {
                result.put("hasPath", false);
                result.put("pathContent", null);
                result.put("error", "报告内容为空，无法获取文件路径");
                return Result.success(result);
            }
            
            // 假设report.content存储的是文件路径
            String filePath = report.getContent().trim();
            result.put("hasPath", true);
            result.put("pathContent", filePath);
            
            // 尝试检查文件是否存在
            try {
                // 测试多种可能的路径解析方式
                List<Map<String, Object>> pathTests = new ArrayList<>();
                
                // 0. 测试通过服务URL下载
                String serverUrl = "http://localhost:8080/" + filePath;
                Map<String, Object> serverUrlTest = new HashMap<>();
                serverUrlTest.put("pathType", "serverUrl");
                serverUrlTest.put("fullPath", serverUrl);
                try {
                    URL url = new URL(serverUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.setConnectTimeout(3000);
                    int responseCode = connection.getResponseCode();
                    boolean exists = (responseCode == HttpURLConnection.HTTP_OK);
                    serverUrlTest.put("exists", exists);
                    if (exists) {
                        serverUrlTest.put("contentLength", connection.getContentLength());
                        serverUrlTest.put("contentType", connection.getContentType());
                    }
                } catch (Exception e) {
                    serverUrlTest.put("exists", false);
                    serverUrlTest.put("error", e.getMessage());
                }
                pathTests.add(serverUrlTest);
                
                // 1. 直接使用路径
                Path directPath = Path.of(filePath);
                Map<String, Object> directTest = new HashMap<>();
                directTest.put("pathType", "direct");
                directTest.put("fullPath", directPath.toString());
                directTest.put("exists", Files.exists(directPath));
                if (Files.exists(directPath)) {
                    directTest.put("size", Files.size(directPath));
                    directTest.put("isReadable", Files.isReadable(directPath));
                }
                pathTests.add(directTest);
                
                // 2. 相对于项目根目录
                Path projectRoot = Path.of(System.getProperty("user.dir"));
                Path relativePath = projectRoot.resolve(filePath);
                Map<String, Object> relativeTest = new HashMap<>();
                relativeTest.put("pathType", "projectRelative");
                relativeTest.put("fullPath", relativePath.toString());
                relativeTest.put("exists", Files.exists(relativePath));
                if (Files.exists(relativePath)) {
                    relativeTest.put("size", Files.size(relativePath));
                    relativeTest.put("isReadable", Files.isReadable(relativePath));
                }
                pathTests.add(relativeTest);
                
                // 3. 相对于uploads目录
                Path uploadsPath = projectRoot.resolve("uploads").resolve(filePath);
                Map<String, Object> uploadsTest = new HashMap<>();
                uploadsTest.put("pathType", "uploadsRelative");
                uploadsTest.put("fullPath", uploadsPath.toString());
                uploadsTest.put("exists", Files.exists(uploadsPath));
                if (Files.exists(uploadsPath)) {
                    uploadsTest.put("size", Files.size(uploadsPath));
                    uploadsTest.put("isReadable", Files.isReadable(uploadsPath));
                }
                pathTests.add(uploadsTest);
                
                // 4. 相对于resources/uploads目录
                Path resourcesPath = projectRoot.resolve("src/main/resources/uploads").resolve(filePath);
                Map<String, Object> resourcesTest = new HashMap<>();
                resourcesTest.put("pathType", "resourcesRelative");
                resourcesTest.put("fullPath", resourcesPath.toString());
                resourcesTest.put("exists", Files.exists(resourcesPath));
                if (Files.exists(resourcesPath)) {
                    resourcesTest.put("size", Files.size(resourcesPath));
                    resourcesTest.put("isReadable", Files.isReadable(resourcesPath));
                }
                pathTests.add(resourcesTest);
                
                // 5. 如果路径以uploads/开头，尝试去掉前缀
                if (filePath.startsWith("uploads/")) {
                    String fileName = filePath.substring("uploads/".length());
                    Path fileNamePath = projectRoot.resolve("uploads").resolve(fileName);
                    Map<String, Object> fileNameTest = new HashMap<>();
                    fileNameTest.put("pathType", "removeUploadsPrefix");
                    fileNameTest.put("fullPath", fileNamePath.toString());
                    fileNameTest.put("exists", Files.exists(fileNamePath));
                    if (Files.exists(fileNamePath)) {
                        fileNameTest.put("size", Files.size(fileNamePath));
                        fileNameTest.put("isReadable", Files.isReadable(fileNamePath));
                    }
                    pathTests.add(fileNameTest);
                }
                
                // 6. 仅使用文件名
                String fileName = Path.of(filePath).getFileName().toString();
                Path fileOnlyPath = projectRoot.resolve("uploads").resolve(fileName);
                Map<String, Object> fileOnlyTest = new HashMap<>();
                fileOnlyTest.put("pathType", "fileNameOnly");
                fileOnlyTest.put("fullPath", fileOnlyPath.toString());
                fileOnlyTest.put("exists", Files.exists(fileOnlyPath));
                if (Files.exists(fileOnlyPath)) {
                    fileOnlyTest.put("size", Files.size(fileOnlyPath));
                    fileOnlyTest.put("isReadable", Files.isReadable(fileOnlyPath));
                }
                pathTests.add(fileOnlyTest);
                
                // 添加当前工作目录信息
                result.put("workingDir", System.getProperty("user.dir"));
                result.put("pathTests", pathTests);
                
                // 检查是否有任何一个路径存在
                boolean anyExists = pathTests.stream().anyMatch(test -> (boolean) test.getOrDefault("exists", false));
                result.put("fileFound", anyExists);
                
                // 尝试使用AiScoringService中的方法解析路径
                try {
                    Path resolvedPath = aiScoringService.tryResolveFilePath(filePath);
                    result.put("serviceResolvedPath", resolvedPath.toString());
                    result.put("serviceResolvedExists", Files.exists(resolvedPath));
                    if (Files.exists(resolvedPath)) {
                        result.put("serviceResolvedSize", Files.size(resolvedPath));
                    }
                } catch (Exception e) {
                    result.put("serviceResolveError", e.getMessage());
                }
                
            } catch (Exception e) {
                result.put("pathError", e.getMessage());
            }
            
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(5001, "检查文件路径失败: " + e.getMessage());
        }
    }
} 