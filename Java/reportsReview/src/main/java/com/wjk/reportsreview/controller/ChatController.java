package com.wjk.reportsreview.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjk.reportsreview.common.Result;
import com.wjk.reportsreview.entity.ChatMessage;
import com.wjk.reportsreview.mapper.ChatMessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatMessageMapper chatMessageMapper;
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Value("${qianwen.api.key:defaultkey}")
    private String apiKey;
    
    @Value("${qianwen.api.url:https://api.example.com}")
    private String apiUrl;
    
    // 本地对话系统的问题-答案对
    private final Map<String, String> knowledgeBase = initKnowledgeBase();
    
    /**
     * 初始化知识库
     */
    private Map<String, String> initKnowledgeBase() {
        Map<String, String> kb = new HashMap<>();
        
        // 系统相关
        kb.put("系统", "这是一个教学报告管理系统，用于学生提交报告、教师评审报告及AI辅助评分。");
        kb.put("功能", "系统主要功能包括：报告上传与管理、报告评审与评分、AI辅助分析、用户管理等。");
        kb.put("帮助", "您可以在顶部导航栏找到帮助中心，或者直接通过此对话助手获取帮助。");
        
        // 报告管理
        kb.put("上传报告", "您可以在'报告管理'页面点击'上传报告'按钮，选择文件并填写相关信息后提交。支持PDF、Word、Excel等常见格式，单个文件大小不超过50MB。");
        kb.put("下载报告", "在报告列表中找到需要下载的报告，点击'下载'按钮即可将文件保存到本地。");
        kb.put("修改报告", "如果报告状态为'待审核'，您可以在报告详情页点击'编辑'按钮修改报告内容或重新上传文件。如果报告已被审核，需联系教师申请重新提交。");
        kb.put("删除报告", "只有管理员和报告创建者可以删除报告，且仅限于'待审核'状态的报告。删除后无法恢复，请谨慎操作。");
        kb.put("支持格式", "系统支持多种文件格式，包括PDF、DOC、DOCX、XLS、XLSX、PPT、PPTX、TXT等。建议上传PDF格式，以确保内容显示一致。");
        
        // 评分相关
        kb.put("评分", "教师可以在报告详情页查看学生提交的报告内容，根据评分标准给予分数和反馈意见。系统支持多维度评分，可以按照不同指标分别打分。");
        kb.put("评分标准", "评分标准通常包括：内容完整性、论述深度、格式规范性、语言表达等多个维度。具体标准以课程要求为准。");
        kb.put("查看成绩", "学生可以在'我的报告'列表中查看已评分的报告，点击查看详情可以看到具体评分和教师反馈。");
        kb.put("申诉", "如果对评分结果有异议，可以在报告详情页点击'申请复核'按钮，填写申诉理由后提交给教师。");
        
        // AI功能
        kb.put("ai功能", "系统集成了AI辅助功能，可以帮助分析报告内容、提供写作建议、自动评分等。AI评分仅供参考，最终成绩仍由教师决定。");
        kb.put("ai评分", "AI评分基于机器学习模型，分析报告的内容、结构、格式等方面。当前版本支持文本内容分析和基础语法检查。");
        
        // 账户管理
        kb.put("修改密码", "点击右上角用户头像，在下拉菜单中选择'账户设置'，即可修改密码和个人信息。");
        kb.put("忘记密码", "在登录页面点击'忘记密码'，通过验证邮箱后可以重置密码。");
        
        return kb;
    }

    /**
     * 发送消息到智能助手
     * @param messageMap 消息内容
     * @return 回复结果
     */
    @PostMapping("/message")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, String> messageMap) {
        try {
            // 获取消息内容
            String message = messageMap.get("message");
            if (message == null || message.isEmpty()) {
                return Result.error(1001, "消息不能为空");
            }
            
            // 获取当前时间
            String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // 获取消息来源用户ID - 这里要么使用当前登录用户ID，要么使用null
            Integer userId = null; // 设置为null，让数据库使用默认值或null
            
            // 保存用户消息
            ChatMessage userChatMessage = new ChatMessage();
            userChatMessage.setUserId(userId != null ? userId : null); // 如果userId为null，设置为null
            userChatMessage.setType("user");
            userChatMessage.setContent(message);
            userChatMessage.setTime(currentTime);
            userChatMessage.setCreatedAt(currentTime);
            userChatMessage.setUpdatedAt(currentTime);
            
            try {
                int result = chatMessageMapper.insert(userChatMessage);
                System.out.println("保存用户消息成功，影响行数: " + result + ", 消息ID: " + userChatMessage.getId());
            } catch (Exception e) {
                System.err.println("保存用户消息失败: " + e.getMessage());
                e.printStackTrace();
                // 即使保存失败也继续处理，避免用户体验中断
            }
            
            // 获取AI回复 - 先尝试使用通义千问API
            Map<String, Object> aiResponse;
            try {
                // 尝试调用通义千问API
                aiResponse = callQianwenApi(message);
                System.out.println("成功使用通义千问API获取回复");
            } catch (Exception e) {
                // 如果API调用失败，使用本地对话系统
                System.out.println("通义千问API调用失败，使用本地对话系统: " + e.getMessage());
                aiResponse = getLocalResponse(message);
            }
            
            String reply = (String) aiResponse.get("reply");
            List<String> suggestions = (List<String>) aiResponse.get("suggestions");
            
            // 保存AI回复 - AI不关联到任何用户
            ChatMessage aiChatMessage = new ChatMessage();
            aiChatMessage.setUserId(null); // AI不关联到任何用户
            aiChatMessage.setType("ai");
            aiChatMessage.setContent(reply);
            aiChatMessage.setTime(currentTime);
            aiChatMessage.setCreatedAt(currentTime);
            aiChatMessage.setUpdatedAt(currentTime);
            
            try {
                int result = chatMessageMapper.insert(aiChatMessage);
                System.out.println("保存AI回复成功，影响行数: " + result + ", 消息ID: " + aiChatMessage.getId());
            } catch (Exception e) {
                System.err.println("保存AI回复失败: " + e.getMessage());
                e.printStackTrace();
                // 即使保存失败也继续处理，避免用户体验中断
            }
            
            // 返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("reply", reply);
            resultMap.put("suggestions", suggestions);
            
            return Result.success(resultMap);
        } catch (Exception e) {
            // 记录异常详细信息
            System.err.println("聊天处理出错: " + e.getMessage());
            e.printStackTrace();
            
            // 返回友好错误信息
            return Result.error(500, "处理您的消息时出现问题，请稍后再试。");
        }
    }
    
    /**
     * 获取聊天历史记录
     * @param page 页码
     * @param pageSize 每页数量
     * @return 聊天历史记录
     */
    @GetMapping("/history")
    public Result<Map<String, Object>> getChatHistory(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        try {
            // 查询聊天记录，不使用用户ID筛选，获取所有消息
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            // 按时间倒序排序
            queryWrapper.orderByDesc("created_at");
            
            // 执行分页查询
            Page<ChatMessage> pageResult = new Page<>(page, pageSize);
            Page<ChatMessage> chatMessagePage = chatMessageMapper.selectPage(pageResult, queryWrapper);
            
            // 组装返回结果
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("total", chatMessagePage.getTotal());
            resultMap.put("list", chatMessagePage.getRecords());
            
            return Result.success(resultMap);
        } catch (Exception e) {
            System.err.println("获取聊天历史出错: " + e.getMessage());
            e.printStackTrace();
            return Result.error(500, "获取聊天历史失败。");
        }
    }
    
    /**
     * 本地对话系统，使用关键词匹配
     * @param message 用户消息
     * @return 回复和建议
     */
    private Map<String, Object> getLocalResponse(String message) {
        String reply;
        String lowerMessage = message.toLowerCase();
        
        // 尝试精确匹配
        reply = findExactMatch(lowerMessage);
        
        // 如果没有精确匹配，尝试关键词匹配
        if (reply == null) {
            reply = findKeywordMatch(lowerMessage);
        }
        
        // 如果仍然没有匹配，使用默认回复
        if (reply == null) {
            reply = "感谢您的提问。我是系统助手，可以回答关于报告提交、评审流程、系统功能等问题。请尝试更具体的提问，或浏览系统帮助文档获取详细指南。";
        }
        
        // 生成相关的后续问题建议
        List<String> suggestions = generateRelatedSuggestions(lowerMessage);
        
        Map<String, Object> result = new HashMap<>();
        result.put("reply", reply);
        result.put("suggestions", suggestions);
        
        return result;
    }
    
    /**
     * 尝试在知识库中查找精确匹配
     */
    private String findExactMatch(String message) {
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            if (message.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    /**
     * 尝试在知识库中查找关键词匹配
     */
    private String findKeywordMatch(String message) {
        // 关键词匹配逻辑
        if (message.contains("报告") && message.contains("上传")) {
            return knowledgeBase.get("上传报告");
        } else if (message.contains("报告") && message.contains("下载")) {
            return knowledgeBase.get("下载报告");
        } else if ((message.contains("评分") || message.contains("打分")) && !message.contains("标准")) {
            return knowledgeBase.get("评分");
        } else if (message.contains("评分") && message.contains("标准")) {
            return knowledgeBase.get("评分标准");
        } else if (message.contains("修改") && message.contains("报告")) {
            return knowledgeBase.get("修改报告");
        } else if (message.contains("删除")) {
            return knowledgeBase.get("删除报告");
        } else if (message.contains("格式") || (message.contains("文件") && message.contains("类型"))) {
            return knowledgeBase.get("支持格式");
        } else if (message.contains("ai") || message.contains("人工智能")) {
            return knowledgeBase.get("ai功能");
        } else if (message.contains("密码") && (message.contains("修改") || message.contains("更改") || message.contains("更新"))) {
            return knowledgeBase.get("修改密码");
        } else if (message.contains("密码") && message.contains("忘")) {
            return knowledgeBase.get("忘记密码");
        } else if (message.contains("成绩") || message.contains("分数")) {
            return knowledgeBase.get("查看成绩");
        }
        
        return null;
    }
    
    /**
     * 生成相关的后续问题建议
     */
    private List<String> generateRelatedSuggestions(String message) {
        List<String> allSuggestions = new ArrayList<>();
        allSuggestions.add("如何上传报告？");
        allSuggestions.add("如何查看评分结果？");
        allSuggestions.add("报告支持哪些文件格式？");
        allSuggestions.add("我可以修改已提交的报告吗？");
        allSuggestions.add("如何使用AI辅助功能？");
        allSuggestions.add("如何查看历史报告？");
        allSuggestions.add("评分标准是什么？");
        allSuggestions.add("如何修改密码？");
        
        // 根据用户消息选择相关的建议
        List<String> suggestions = new ArrayList<>();
        if (message.contains("报告")) {
            suggestions.add("报告支持哪些文件格式？");
            suggestions.add("我可以修改已提交的报告吗？");
        } 
        if (message.contains("评分") || message.contains("成绩")) {
            suggestions.add("如何查看评分结果？");
            suggestions.add("评分标准是什么？");
        }
        if (message.contains("ai")) {
            suggestions.add("如何使用AI辅助功能？");
        }
        
        // 如果没有根据上下文找到相关建议，随机选择几个
        if (suggestions.isEmpty()) {
            Random random = new Random();
            for (int i = 0; i < 3 && !allSuggestions.isEmpty(); i++) {
                int index = random.nextInt(allSuggestions.size());
                suggestions.add(allSuggestions.get(index));
                allSuggestions.remove(index);
            }
        }
        
        // 确保建议不超过3个
        while (suggestions.size() > 3) {
            suggestions.remove(suggestions.size() - 1);
        }
        
        return suggestions;
    }
    
    /**
     * 调用通义千问API获取回复
     * @param message 用户消息
     * @return AI回复
     */
    private Map<String, Object> callQianwenApi(String message) {
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        // 构建请求体 - 通义千问的格式
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-max"); // 使用通义千问大模型
        
        JSONObject input = new JSONObject();
        // 构建系统提示词，加入知识库内容
        StringBuilder systemPromptBuilder = new StringBuilder();
        systemPromptBuilder.append("你是教学管理系统老师端的智能助手，名为'学习助手'。你可以回答关于系统使用、报告提交、评分标准等方面的问题，帮助老师使用该系统。")
                .append("请提供简洁、准确、有帮助的回答。回答后，提供2-3个可能的后续问题建议。")
                .append("你的回答应该友善、专业，避免过长的回复。\n\n")
                .append("以下是关于系统的重要知识，请在回答用户问题时参考：\n\n");
        
        // 添加知识库内容
        for (Map.Entry<String, String> entry : knowledgeBase.entrySet()) {
            systemPromptBuilder.append("【").append(entry.getKey()).append("】: ")
                    .append(entry.getValue()).append("\n");
        }
        
        String systemPrompt = systemPromptBuilder.toString();
        
        // DashScope API格式
        JSONArray messages = new JSONArray();
        
        // 添加系统消息
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemPrompt);
        messages.add(systemMessage);
        
        // 添加用户消息
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        messages.add(userMessage);
        
        input.put("messages", messages);
        requestBody.put("input", input);
        
        JSONObject parameters = new JSONObject();
        parameters.put("temperature", 0.7);
        parameters.put("top_p", 0.9);
        parameters.put("result_format", "message");
        requestBody.put("parameters", parameters);
        
        // 打印请求体，方便调试
        System.out.println("请求API: " + apiUrl);
        // 不打印完整系统提示词，太长
        System.out.println("请求体: " + requestBody.toJSONString().substring(0, Math.min(200, requestBody.toJSONString().length())) + "...(已截断)");
        
        try {
            // 发送请求
            HttpEntity<String> entity = new HttpEntity<>(requestBody.toJSONString(), headers);
            ResponseEntity<String> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, String.class);
            
            // 记录响应体，方便调试
            System.out.println("API响应状态: " + response.getStatusCode());
            System.out.println("API响应体: " + response.getBody());
            
            // 解析响应
            JSONObject responseJson = JSON.parseObject(response.getBody());
            
            // 提取输出内容，根据通义千问API的响应格式
            String content;
            if (responseJson.containsKey("output")) {
                JSONObject output = responseJson.getJSONObject("output");
                if (output.containsKey("choices") && output.getJSONArray("choices").size() > 0) {
                    content = output.getJSONArray("choices").getJSONObject(0)
                            .getJSONObject("message").getString("content");
                } else if (output.containsKey("text")) {
                    content = output.getString("text");
                } else {
                    content = "收到您的消息，但AI无法生成适当的回复。";
                    System.out.println("通义千问API返回了意外格式: " + output);
                }
            } else if (responseJson.containsKey("error")) {
                // 处理错误情况
                JSONObject error = responseJson.getJSONObject("error");
                String errorMessage = error.getString("message");
                content = "抱歉，AI服务遇到了问题: " + errorMessage;
                System.err.println("通义千问API错误: " + error);
            } else {
                // 无法识别的格式
                content = "收到您的消息，但我无法生成适当的回复。请以不同方式提问。";
                System.out.println("无法从API响应中提取内容: " + responseJson);
            }
            
            // 尝试从回复中提取建议问题
            List<String> suggestions = extractSuggestionsFromReply(content);
            if (suggestions.isEmpty()) {
                // 如果没有提取到，使用默认的建议问题
                suggestions = generateDefaultSuggestions(message);
            }
            
            // 返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("reply", content);
            result.put("suggestions", suggestions);
            
            return result;
        } catch (Exception e) {
            // 记录异常详细信息
            System.err.println("调用通义千问API错误: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            
            // 抛出异常，让调用方回退到本地对话系统
            throw new RuntimeException("通义千问API调用失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 尝试从AI回复中提取建议问题
     */
    private List<String> extractSuggestionsFromReply(String content) {
        List<String> suggestions = new ArrayList<>();
        
        // 尝试寻找常见的格式标记
        String[] possibleDelimiters = {
            "后续问题：", "建议问题：", "您可能还想了解：", "相关问题：",
            "Questions:", "Follow-up:", "You might also want to ask:"
        };
        
        for (String delimiter : possibleDelimiters) {
            if (content.contains(delimiter)) {
                String suggestionsText = content.substring(content.indexOf(delimiter) + delimiter.length());
                // 尝试按数字或短横线分割
                if (suggestionsText.contains("1.") || suggestionsText.contains("1、")) {
                    String[] parts = suggestionsText.split("[1-9][.、)]\\s*");
                    for (int i = 1; i < parts.length && suggestions.size() < 3; i++) {
                        String question = parts[i].trim();
                        // 如果包含下一个问题的编号，截取
                        int nextQuestionIndex = question.indexOf((i+1) + ".");
                        if (nextQuestionIndex == -1) {
                            nextQuestionIndex = question.indexOf((i+1) + "、");
                        }
                        if (nextQuestionIndex != -1) {
                            question = question.substring(0, nextQuestionIndex).trim();
                        }
                        if (!question.isEmpty() && !question.equals("\n")) {
                            suggestions.add(question);
                        }
                    }
                } else if (suggestionsText.contains("-") || suggestionsText.contains("•")) {
                    String[] parts = suggestionsText.split("[-•]\\s*");
                    for (int i = 1; i < parts.length && suggestions.size() < 3; i++) {
                        String question = parts[i].trim();
                        if (!question.isEmpty() && !question.equals("\n")) {
                            suggestions.add(question);
                        }
                    }
                }
                break;
            }
        }
        
        return suggestions;
    }
    
    /**
     * 生成默认的建议问题
     */
    private List<String> generateDefaultSuggestions(String message) {
        // 这里与generateRelatedSuggestions方法相同
        List<String> allSuggestions = new ArrayList<>();
        allSuggestions.add("如何上传报告？");
        allSuggestions.add("如何查看评分结果？");
        allSuggestions.add("报告支持哪些文件格式？");
        allSuggestions.add("我可以修改已提交的报告吗？");
        allSuggestions.add("如何使用AI辅助功能？");
        allSuggestions.add("如何查看历史报告？");
        allSuggestions.add("评分标准是什么？");
        allSuggestions.add("如何修改密码？");
        
        // 根据用户消息选择相关的建议
        List<String> suggestions = new ArrayList<>();
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("报告")) {
            suggestions.add("报告支持哪些文件格式？");
            suggestions.add("我可以修改已提交的报告吗？");
        } 
        if (lowerMessage.contains("评分") || lowerMessage.contains("成绩")) {
            suggestions.add("如何查看评分结果？");
            suggestions.add("评分标准是什么？");
        }
        if (lowerMessage.contains("ai")) {
            suggestions.add("如何使用AI辅助功能？");
        }
        
        // 如果没有根据上下文找到相关建议，随机选择几个
        if (suggestions.isEmpty()) {
            Random random = new Random();
            for (int i = 0; i < 3 && !allSuggestions.isEmpty(); i++) {
                int index = random.nextInt(allSuggestions.size());
                suggestions.add(allSuggestions.get(index));
                allSuggestions.remove(index);
            }
        }
        
        // 确保建议不超过3个
        while (suggestions.size() > 3) {
            suggestions.remove(suggestions.size() - 1);
        }
        
        return suggestions;
    }
} 