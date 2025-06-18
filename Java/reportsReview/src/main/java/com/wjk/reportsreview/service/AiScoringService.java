package com.wjk.reportsreview.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.*;
import com.wjk.reportsreview.entity.AiScoringTask;
import com.wjk.reportsreview.entity.Report;
import com.wjk.reportsreview.mapper.AiScoringTaskMapper;
import com.wjk.reportsreview.mapper.ReportMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiScoringService {
    
    @Autowired
    private AiScoringTaskMapper aiScoringTaskMapper;
    
    @Autowired
    private ReportMapper reportMapper;
    
    @Value("${dashscope.api.key:${qianwen.api.key}}")
    private String dashscopeApiKey;
    
    /**
     * 获取默认评分标准
     */
    private JSONArray getDefaultCriteria() {
        JSONArray criteria = new JSONArray();
        
        JSONObject criterion1 = new JSONObject();
        criterion1.put("name", "内容完整性");
        criterion1.put("weight", 30);
        criterion1.put("description", "报告是否包含所有必要的内容和部分");
        criteria.add(criterion1);
        
        JSONObject criterion2 = new JSONObject();
        criterion2.put("name", "论述深度");
        criterion2.put("weight", 30);
        criterion2.put("description", "报告的分析深度和思考质量");
        criteria.add(criterion2);
        
        JSONObject criterion3 = new JSONObject();
        criterion3.put("name", "语言表达");
        criterion3.put("weight", 20);
        criterion3.put("description", "语言的准确性、流畅性和专业性");
        criteria.add(criterion3);
        
        JSONObject criterion4 = new JSONObject();
        criterion4.put("name", "格式规范");
        criterion4.put("weight", 20);
        criterion4.put("description", "报告格式是否符合规定要求");
        criteria.add(criterion4);
        
        return criteria;
    }
    
    /**
     * 异步处理AI评分任务
     * @param taskId 任务ID
     * @param reportIds 需要评分的报告ID列表
     */
    @Async("taskExecutor")
    public void processAiScoringTask(String taskId, List<Integer> reportIds) {
        System.out.println("处理AI评分任务开始，taskId: " + taskId + ", reportIds: " + reportIds);
        try {
            // 查询任务
            AiScoringTask task = aiScoringTaskMapper.selectById(taskId);
            if (task == null) {
                System.err.println("任务不存在: " + taskId);
                return;
            }
            
            // 更新任务状态
            task.setMessage("开始处理文件");
            task.setProgress(10);
            aiScoringTaskMapper.updateById(task);
            System.out.println("任务状态更新为：进度10%, " + task.getMessage());
            
            // 报告ID列表，用于最终结果返回
            List<Integer> processedReportIds = new ArrayList<>();
            
            // 处理每个报告
            for (int i = 0; i < reportIds.size(); i++) {
                Integer reportId = reportIds.get(i);
                // 查询报告
                System.out.println("查询报告，reportId: " + reportId);
                Report report = reportMapper.selectById(reportId);
                if (report == null) {
                    System.err.println("未找到报告: " + reportId);
                    continue;
                }
                
                // 更新任务进度
                int progress = 10 + ((i + 1) * 90 / reportIds.size());
                task.setProgress(progress);
                task.setMessage("正在处理第" + (i + 1) + "个报告: " + report.getTitle());
                aiScoringTaskMapper.updateById(task);
                System.out.println("任务状态更新为：进度" + progress + "%, " + task.getMessage());
                
                try {
                    System.out.println("开始AI批改报告: " + report.getId() + ", 标题: " + report.getTitle());
                    // 使用AI对报告进行批改
                    Map<String, Object> scoreResult = scoreReportWithAI(report);
                    System.out.println("AI批改完成，得分: " + scoreResult.get("score"));
                    
                    // 更新报告的评分信息
                    report.setScore((Float) scoreResult.get("score"));
                    report.setFeedback((String) scoreResult.get("feedback"));
                    report.setScoreDetails((String) scoreResult.get("scoreDetails"));
                    report.setStatus(3); // 3表示AI评分状态
                    report.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    
                    // 更新报告
                    System.out.println("更新报告评分信息到数据库");
                    reportMapper.updateById(report);
                    
                    // 添加报告ID到列表
                    processedReportIds.add(report.getId());
                    System.out.println("报告处理完成: " + report.getId());
                    
                } catch (Exception e) {
                    // 处理单个报告失败，继续处理下一个
                    System.err.println("处理报告出错: " + report.getId() + ", 错误: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            // 更新任务状态为完成
            task.setStatus("2"); // 2表示已完成
            task.setProgress(100);
            task.setMessage("评分完成");
            task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            aiScoringTaskMapper.updateById(task);
            System.out.println("任务完成，taskId: " + taskId);
            
        } catch (Exception e) {
            // 更新任务状态为错误
            System.err.println("处理任务出错，taskId: " + taskId + ", 错误: " + e.getMessage());
            e.printStackTrace();
            AiScoringTask task = aiScoringTaskMapper.selectById(taskId);
            if (task != null) {
                task.setStatus("3"); // 3表示出错
                task.setMessage("处理出错: " + e.getMessage());
                task.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                aiScoringTaskMapper.updateById(task);
                System.out.println("已更新任务状态为错误");
            }
        }
    }
    
    /**
     * 使用AI对报告进行批改和评分
     * @param report 报告
     * @return 评分结果，包含score(Float)、feedback(String)和scoreDetails(String)
     */
    private Map<String, Object> scoreReportWithAI(Report report) {
        System.out.println("开始AI评分流程，报告ID: " + report.getId());
        // 创建通义千问API客户端
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(dashscopeApiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从report.content中获取文件路径
            if (report.getContent() == null || report.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("报告内容为空，无法获取文件路径");
            }
            
            // report.content应该存储的是上传文件的路径
            String filePath = report.getContent().trim();
            System.out.println("从报告中获取文件路径: " + filePath);
            
            // 检查文件是否存在
            Path reportFilePath = getAbsoluteFilePath(filePath);
            if (!Files.exists(reportFilePath)) {
                throw new IllegalArgumentException("文件不存在: " + reportFilePath);
            }
            
            System.out.println("文件存在，大小: " + Files.size(reportFilePath) + " 字节");
            
            // 创建AI评分提示
            String aiPrompt = buildScoringPrompt();
            
            // 上传文件到通义千问
            System.out.println("开始上传文件到通义千问...");
            FileCreateParams fileParams = FileCreateParams.builder()
                    .file(reportFilePath)
                    .purpose(FilePurpose.of("file-extract"))
                    .build();
            
            FileObject fileObject = client.files().create(fileParams);
            String fileId = fileObject.id();
            System.out.println("文件上传成功，fileId: " + fileId);
            
            // 使用已上传的文件ID进行评分
            System.out.println("使用fileId发送评分请求...");
            ChatCompletionCreateParams chatParams = ChatCompletionCreateParams.builder()
                    .addSystemMessage("You are a helpful assistant specializing in grading academic reports.")
                    .addSystemMessage("fileid://" + fileId) // 使用文件ID
                    .addUserMessage(aiPrompt)
                    .model("qwen-long")
                    .build();
            
            System.out.println("发送请求到通义千问API，使用模型: qwen-long");
            // 发送请求并获取回复
            ChatCompletion completion = client.chat().completions().create(chatParams);
            String responseContent = completion.choices().get(0).message().content()
                    .orElse("{\"totalScore\": 0, \"feedback\": \"评分失败，请重试\", \"scores\": []}");
            System.out.println("AI响应成功，响应长度: " + responseContent.length() + " 字符");
            
            try {
                // 检查是否返回了JSON格式的响应
                if (!responseContent.trim().startsWith("{")) {
                    System.out.println("AI返回的不是JSON格式，尝试提取JSON部分");
                    // 尝试从响应中提取JSON部分
                    int jsonStart = responseContent.indexOf('{');
                    int jsonEnd = responseContent.lastIndexOf('}');
                    
                    if (jsonStart >= 0 && jsonEnd > jsonStart) {
                        responseContent = responseContent.substring(jsonStart, jsonEnd + 1);
                        System.out.println("提取的JSON内容: " + responseContent);
                    } else {
                        // 如果无法提取JSON，则生成一个默认的JSON响应
                        JSONObject defaultResponse = new JSONObject();
                        defaultResponse.put("totalScore", 70.0f);
                        defaultResponse.put("feedback", "AI未能以规定格式返回评分，已生成默认评分。AI回复内容: " + responseContent);
                        
                        JSONArray scores = new JSONArray();
                        JSONArray criteriaArray = getDefaultCriteria();
                        for (int i = 0; i < criteriaArray.size(); i++) {
                            JSONObject criterion = criteriaArray.getJSONObject(i);
                            JSONObject scoreDetail = new JSONObject();
                            scoreDetail.put("criterion", criterion.getString("name"));
                            float defaultScore = criterion.getIntValue("weight") * 0.7f;
                            scoreDetail.put("score", defaultScore);
                            scoreDetail.put("maxScore", criterion.getIntValue("weight"));
                            scores.add(scoreDetail);
                        }
                        defaultResponse.put("scores", scores);
                        
                        responseContent = defaultResponse.toJSONString();
                        System.out.println("生成默认JSON响应: " + responseContent);
                    }
                }
                
                // 解析响应JSON
                System.out.println("解析AI响应结果...");
                JSONObject responseJson = JSONObject.parseObject(responseContent);
                
                // 提取总分
                float totalScore = responseJson.getFloatValue("totalScore");
                result.put("score", totalScore);
                
                // 提取反馈
                String feedback = responseJson.getString("feedback");
                result.put("feedback", feedback);
                
                // 提取详细评分
                JSONArray scores = responseJson.getJSONArray("scores");
                result.put("scoreDetails", scores.toJSONString());
                
                System.out.println("解析成功，总分: " + totalScore + ", 反馈长度: " + feedback.length() + " 字符");
                
            } catch (Exception e) {
                // JSON解析失败，可能是AI返回的不是预期格式
                // 直接使用原始响应内容创建默认结果
                System.err.println("解析AI评分结果失败: " + e.getMessage());
                System.err.println("原始响应内容: " + responseContent);
                
                // 根据评分标准生成默认评分详情
                JSONArray scoreDetailsArray = new JSONArray();
                float defaultScore = 0;
                JSONArray criteriaArray = getDefaultCriteria();
                
                for (int i = 0; i < criteriaArray.size(); i++) {
                    JSONObject criterion = criteriaArray.getJSONObject(i);
                    JSONObject scoreDetail = new JSONObject();
                    scoreDetail.put("criterion", criterion.getString("name"));
                    
                    int weight = criterion.getIntValue("weight");
                    int actualScore = (int) (weight * 0.7); // 默认给70%的分数
                    defaultScore += actualScore;
                    
                    scoreDetail.put("score", actualScore);
                    scoreDetail.put("maxScore", weight);
                    
                    scoreDetailsArray.add(scoreDetail);
                }
                
                result.put("score", defaultScore);
                result.put("feedback", "AI批改未能生成规范评分。以下是AI的反馈：\n\n" + responseContent);
                result.put("scoreDetails", scoreDetailsArray.toJSONString());
            }
            
        } catch (Exception e) {
            System.err.println("调用AI评分服务失败: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认结果
            System.out.println("生成默认评分结果");
            result.put("score", 60.0f);
            result.put("feedback", "AI评分服务暂时不可用: " + e.getMessage());
            
            // 构建默认评分详情
            JSONArray scoreDetailsArray = new JSONArray();
            JSONArray criteriaArray = getDefaultCriteria();
            
            for (int i = 0; i < criteriaArray.size(); i++) {
                JSONObject criterion = criteriaArray.getJSONObject(i);
                JSONObject scoreDetail = new JSONObject();
                scoreDetail.put("criterion", criterion.getString("name"));
                
                int weight = criterion.getIntValue("weight");
                int actualScore = (int) (weight * 0.6); // 默认给60%的分数
                
                scoreDetail.put("score", actualScore);
                scoreDetail.put("maxScore", weight);
                
                scoreDetailsArray.add(scoreDetail);
            }
            
            result.put("scoreDetails", scoreDetailsArray.toJSONString());
        }
        
        System.out.println("AI评分流程完成，报告ID: " + report.getId());
        return result;
    }
    
    /**
     * 获取文件的绝对路径或从URL下载文件
     * 根据配置的上传路径，将相对路径转换为绝对路径，或从URL下载文件到临时目录
     */
    private Path getAbsoluteFilePath(String pathOrUrl) throws IOException {
        System.out.println("处理文件路径或URL: " + pathOrUrl);
        
        // 如果以http://或https://开头，则是URL，需要下载
        if (pathOrUrl.toLowerCase().startsWith("http://") || pathOrUrl.toLowerCase().startsWith("https://")) {
            System.out.println("检测到URL，将下载文件: " + pathOrUrl);
            return downloadFileFromUrl(pathOrUrl);
        }
        
        // 如果是相对路径，可能需要添加服务器基础URL
        if (!pathOrUrl.startsWith("/") && !pathOrUrl.contains("://")) {
            // 尝试添加服务器基础URL
            String baseUrl = "http://localhost:8080/";
            String fullUrl = baseUrl + pathOrUrl;
            System.out.println("尝试转换为完整URL: " + fullUrl);
            
            try {
                return downloadFileFromUrl(fullUrl);
            } catch (Exception e) {
                System.out.println("从URL下载失败: " + e.getMessage() + "，尝试本地文件路径");
                // 继续尝试本地文件路径
            }
        }
        
        // 处理文件路径，确保能找到正确的文件
        if (pathOrUrl.startsWith("/")) {
            // 已经是绝对路径
            Path path = Path.of(pathOrUrl);
            if (Files.exists(path)) {
                System.out.println("找到绝对路径文件: " + path);
                return path;
            }
        }
        
        // 相对路径，尝试多种可能的基础路径
        // 1. 首先尝试项目根目录下的uploads文件夹
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        Path uploadsPath = projectRoot.resolve("uploads").resolve(pathOrUrl);
        if (Files.exists(uploadsPath)) {
            System.out.println("找到文件在项目根目录/uploads下: " + uploadsPath);
            return uploadsPath;
        }
        
        // 2. 尝试src/main/resources/uploads
        Path resourcesPath = projectRoot.resolve("src/main/resources/uploads").resolve(pathOrUrl);
        if (Files.exists(resourcesPath)) {
            System.out.println("找到文件在resources/uploads下: " + resourcesPath);
            return resourcesPath;
        }
        
        // 3. 尝试直接使用相对路径
        Path directPath = Path.of(pathOrUrl);
        if (Files.exists(directPath)) {
            System.out.println("使用直接相对路径找到文件: " + directPath);
            return directPath;
        }
        
        // 4. 尝试去掉路径中的uploads/前缀
        if (pathOrUrl.startsWith("uploads/")) {
            String fileName = pathOrUrl.substring("uploads/".length());
            Path fileNamePath = projectRoot.resolve("uploads").resolve(fileName);
            if (Files.exists(fileNamePath)) {
                System.out.println("去掉uploads/前缀后找到文件: " + fileNamePath);
                return fileNamePath;
            }
        }
        
        // 5. 尝试直接在服务器上下载该文件
        try {
            String baseUrl = "http://localhost:8080/uploads/";
            String fileName = Path.of(pathOrUrl).getFileName().toString();
            String fullUrl = baseUrl + fileName;
            System.out.println("尝试从服务器下载文件: " + fullUrl);
            return downloadFileFromUrl(fullUrl);
        } catch (Exception e) {
            System.out.println("从服务器下载失败: " + e.getMessage());
        }
        
        // 所有尝试失败，最后抛出异常
        System.err.println("尝试所有可能的路径均未找到文件: " + pathOrUrl);
        throw new IOException("文件不存在: " + pathOrUrl);
    }
    
    /**
     * 从URL下载文件到临时目录
     * @param url 文件URL
     * @return 临时文件路径
     */
    private Path downloadFileFromUrl(String url) throws IOException {
        System.out.println("开始从URL下载文件: " + url);
        
        // 创建临时文件
        String fileName = url.substring(url.lastIndexOf("/") + 1);
        Path tempFilePath = Files.createTempFile("downloaded_", "_" + fileName);
        
        // 使用java.net.URL下载文件
        URL fileUrl = new URL(url);
        try (InputStream in = fileUrl.openStream()) {
            Files.copy(in, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        
        System.out.println("文件下载成功，保存到临时文件: " + tempFilePath + ", 文件大小: " + Files.size(tempFilePath) + " 字节");
        return tempFilePath;
    }
    
    /**
     * 构建评分提示
     */
    private String buildScoringPrompt() {
        JSONArray criteriaArray = getDefaultCriteria();
        
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("请您作为一位经验丰富的教师，对上述实验报告进行全面评价和打分。\n");
        promptBuilder.append("评分标准如下：\n");
        
        float totalWeight = 0;
        for (int i = 0; i < criteriaArray.size(); i++) {
            JSONObject criterion = criteriaArray.getJSONObject(i);
            String name = criterion.getString("name");
            int weight = criterion.getIntValue("weight");
            String description = criterion.getString("description");
            
            promptBuilder.append((i+1)).append(". ").append(name)
                    .append("（").append(weight).append("分）：")
                    .append(description).append("\n");
                    
            totalWeight += weight;
        }
        
        // 添加输出格式要求
        promptBuilder.append("\n请提供详细的评价，严格按照以下JSON格式返回评分结果（必须是标准的JSON格式且仅返回JSON，不要有其他文字说明）：\n");
        promptBuilder.append("{\n");
        promptBuilder.append("  \"scores\": [\n");
        
        for (int i = 0; i < criteriaArray.size(); i++) {
            JSONObject criterion = criteriaArray.getJSONObject(i);
            String name = criterion.getString("name");
            
            promptBuilder.append("    {\n");
            promptBuilder.append("      \"criterion\": \"").append(name).append("\",\n");
            promptBuilder.append("      \"score\": 分数,\n");
            promptBuilder.append("      \"maxScore\": ").append(criterion.getIntValue("weight")).append("\n");
            
            if (i < criteriaArray.size() - 1) {
                promptBuilder.append("    },\n");
            } else {
                promptBuilder.append("    }\n");
            }
        }
        
        promptBuilder.append("  ],\n");
        promptBuilder.append("  \"totalScore\": 总分,\n");
        promptBuilder.append("  \"feedback\": \"详细评语，包括优点、问题和改进建议\"\n");
        promptBuilder.append("}\n");
        
        // 添加一些默认的反馈设置
        promptBuilder.append("\n反馈应包括200字左右的详细评语，并提供具体的改进建议。");
        promptBuilder.append("\n记住：必须严格按照上述JSON格式返回，不要返回任何额外的文字说明。");
        
        return promptBuilder.toString();
    }
    
    /**
     * 测试用 - 同步执行AI批改（不使用@Async）
     * 此方法仅用于诊断问题
     */
    public Map<String, Object> testScoreReportWithAI(Report report) {
        System.out.println("开始同步测试AI评分流程，报告ID: " + report.getId());
        // 创建通义千问API客户端
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(dashscopeApiKey)
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
        
        System.out.println("API密钥: " + (dashscopeApiKey != null ? (dashscopeApiKey.substring(0, 3) + "..." + dashscopeApiKey.substring(Math.max(0, dashscopeApiKey.length() - 3))) : "null"));
        System.out.println("基础URL: https://dashscope.aliyuncs.com/compatible-mode/v1");
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 从report.content中获取文件路径
            if (report.getContent() == null || report.getContent().trim().isEmpty()) {
                throw new IllegalArgumentException("报告内容为空，无法获取文件路径");
            }
            
            // report.content应该存储的是上传文件的路径
            String filePath = report.getContent().trim();
            System.out.println("从报告中获取文件路径: " + filePath);
            
            // 使用改进的文件路径处理方法
            Path reportFilePath = getAbsoluteFilePath(filePath);
            System.out.println("文件存在，大小: " + Files.size(reportFilePath) + " 字节，文件路径: " + reportFilePath);
            
            // 创建AI评分提示
            String aiPrompt = buildScoringPrompt();
            
            // 上传文件到通义千问
            System.out.println("开始上传文件到通义千问...");
            FileCreateParams fileParams = FileCreateParams.builder()
                    .file(reportFilePath)
                    .purpose(FilePurpose.of("file-extract"))
                    .build();
            
            FileObject fileObject = client.files().create(fileParams);
            String fileId = fileObject.id();
            System.out.println("文件上传成功，fileId: " + fileId);
            
            // 使用已上传的文件ID进行评分
            System.out.println("使用fileId发送评分请求...");
            ChatCompletionCreateParams chatParams = ChatCompletionCreateParams.builder()
                    .addSystemMessage("You are a helpful assistant specializing in grading academic reports.")
                    .addSystemMessage("fileid://" + fileId) // 使用文件ID
                    .addUserMessage(aiPrompt)
                    .model("qwen-long")
                    .build();
            
            System.out.println("发送请求到通义千问API，使用模型: qwen-long");
            
            try {
                // 发送请求并获取回复
                ChatCompletion completion = client.chat().completions().create(chatParams);
                String responseContent = completion.choices().get(0).message().content()
                        .orElse("{\"totalScore\": 0, \"feedback\": \"评分失败，请重试\", \"scores\": []}");
                System.out.println("AI响应成功，响应长度: " + responseContent.length() + " 字符");
                
                try {
                    // 检查是否返回了JSON格式的响应
                    if (!responseContent.trim().startsWith("{")) {
                        System.out.println("AI返回的不是JSON格式，尝试提取JSON部分");
                        // 尝试从响应中提取JSON部分
                        int jsonStart = responseContent.indexOf('{');
                        int jsonEnd = responseContent.lastIndexOf('}');
                        
                        if (jsonStart >= 0 && jsonEnd > jsonStart) {
                            responseContent = responseContent.substring(jsonStart, jsonEnd + 1);
                            System.out.println("提取的JSON内容: " + responseContent);
                        } else {
                            // 如果无法提取JSON，则生成一个默认的JSON响应
                            JSONObject defaultResponse = new JSONObject();
                            defaultResponse.put("totalScore", 70.0f);
                            defaultResponse.put("feedback", "AI未能以规定格式返回评分，已生成默认评分。AI回复内容: " + responseContent);
                            
                            JSONArray scores = new JSONArray();
                            JSONArray criteriaArray = getDefaultCriteria();
                            for (int i = 0; i < criteriaArray.size(); i++) {
                                JSONObject criterion = criteriaArray.getJSONObject(i);
                                JSONObject scoreDetail = new JSONObject();
                                scoreDetail.put("criterion", criterion.getString("name"));
                                float defaultScore = criterion.getIntValue("weight") * 0.7f;
                                scoreDetail.put("score", defaultScore);
                                scoreDetail.put("maxScore", criterion.getIntValue("weight"));
                                scores.add(scoreDetail);
                            }
                            defaultResponse.put("scores", scores);
                            
                            responseContent = defaultResponse.toJSONString();
                            System.out.println("生成默认JSON响应: " + responseContent);
                        }
                    }
                    
                    // 解析响应JSON
                    System.out.println("解析AI响应结果...");
                    JSONObject responseJson = JSONObject.parseObject(responseContent);
                    
                    // 提取总分
                    float totalScore = responseJson.getFloatValue("totalScore");
                    result.put("score", totalScore);
                    
                    // 提取反馈
                    String feedback = responseJson.getString("feedback");
                    result.put("feedback", feedback);
                    
                    // 提取详细评分
                    JSONArray scores = responseJson.getJSONArray("scores");
                    result.put("scoreDetails", scores.toJSONString());
                    
                    System.out.println("解析成功，总分: " + totalScore + ", 反馈长度: " + feedback.length() + " 字符");
                } catch (Exception e) {
                    System.err.println("解析AI响应内容失败: " + e.getMessage());
                    System.err.println("原始响应内容: " + responseContent);
                    
                    // 根据评分标准生成默认评分详情
                    JSONArray scoreDetailsArray = new JSONArray();
                    float defaultScore = 0;
                    JSONArray criteriaArray = getDefaultCriteria();
                    
                    for (int i = 0; i < criteriaArray.size(); i++) {
                        JSONObject criterion = criteriaArray.getJSONObject(i);
                        JSONObject scoreDetail = new JSONObject();
                        scoreDetail.put("criterion", criterion.getString("name"));
                        
                        int weight = criterion.getIntValue("weight");
                        int actualScore = (int) (weight * 0.7); // 默认给70%的分数
                        defaultScore += actualScore;
                        
                        scoreDetail.put("score", actualScore);
                        scoreDetail.put("maxScore", weight);
                        
                        scoreDetailsArray.add(scoreDetail);
                    }
                    
                    result.put("score", defaultScore);
                    result.put("feedback", "AI批改未能生成规范评分。以下是AI的反馈：\n\n" + responseContent);
                    result.put("scoreDetails", scoreDetailsArray.toJSONString());
                    result.put("rawResponse", responseContent);
                }
            } catch (Exception e) {
                System.err.println("调用AI API时出错: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("调用通义千问API失败: " + e.getMessage(), e);
            }
            
        } catch (Exception e) {
            System.err.println("整体处理过程失败: " + e.getMessage());
            e.printStackTrace();
            
            // 返回默认结果
            System.out.println("生成默认评分结果");
            result.put("score", 60.0f);
            result.put("feedback", "AI评分服务暂时不可用: " + e.getMessage());
            
            // 构建默认评分详情
            JSONArray scoreDetailsArray = new JSONArray();
            JSONArray criteriaArray = getDefaultCriteria();
            
            for (int i = 0; i < criteriaArray.size(); i++) {
                JSONObject criterion = criteriaArray.getJSONObject(i);
                JSONObject scoreDetail = new JSONObject();
                scoreDetail.put("criterion", criterion.getString("name"));
                
                int weight = criterion.getIntValue("weight");
                int actualScore = (int) (weight * 0.6); // 默认给60%的分数
                
                scoreDetail.put("score", actualScore);
                scoreDetail.put("maxScore", weight);
                
                scoreDetailsArray.add(scoreDetail);
            }
            
            result.put("scoreDetails", scoreDetailsArray.toJSONString());
            result.put("error", e.getMessage());
        }
        
        System.out.println("同步测试AI评分流程完成，报告ID: " + report.getId());
        return result;
    }
    
    /**
     * 尝试解析文件路径，供外部调用
     * @param pathOrUrl 文件路径或URL
     * @return 解析后的路径
     * @throws IOException 如果文件不存在或无法访问
     */
    public Path tryResolveFilePath(String pathOrUrl) throws IOException {
        return getAbsoluteFilePath(pathOrUrl);
    }
} 