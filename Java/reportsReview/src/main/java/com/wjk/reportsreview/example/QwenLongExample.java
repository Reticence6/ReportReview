package com.wjk.reportsreview.example;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 通义大模型Qwen-Long示例代码
 * 用于演示如何使用通义大模型进行文件上传和处理
 */
public class QwenLongExample {
    public static void main(String[] args) {
        // 创建客户端，使用环境变量中的API密钥
        OpenAIClient client = OpenAIOkHttpClient.builder()
                .apiKey(System.getenv("DASHSCOPE_API_KEY"))
                .baseUrl("https://dashscope.aliyuncs.com/compatible-mode/v1")
                .build();
        
        // 设置文件路径,请根据实际需求修改路径与文件名
        Path filePath = Paths.get("src/main/resources/example/report.pdf");
        
        // 创建文件上传参数
        FileCreateParams fileParams = FileCreateParams.builder()
                .file(filePath)
                .purpose(FilePurpose.of("file-extract"))
                .build();

        try {
            // 上传文件并获取fileId
            FileObject fileObject = client.files().create(fileParams);
            String fileId = fileObject.id();
            System.out.println("文件ID: " + fileId);
            
            // 使用文件ID进行报告批改
            batchReportWithQwenLong(client, fileId);
        } catch (Exception e) {
            System.err.println("处理出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 使用通义千问Qwen-Long模型进行报告批改
     * @param client OpenAI客户端
     * @param fileId 文件ID
     */
    private static void batchReportWithQwenLong(OpenAIClient client, String fileId) {
        // 定义批改请求的prompt
        String reviewPrompt = "你是一位经验丰富的教师，请对这份学生提交的实验报告进行全面评价和打分。" +
                "评分标准如下：\n" +
                "1. 内容完整性（30分）：报告是否包含所有必要的内容和部分\n" +
                "2. 论述深度（30分）：报告的分析深度和思考质量\n" +
                "3. 语言表达（20分）：语言的准确性、流畅性和专业性\n" +
                "4. 格式规范（20分）：报告格式是否符合规定要求\n\n" +
                "请提供详细的评价，包括：\n" +
                "- 各项评分及总分（满分100分）\n" +
                "- 报告的优点和亮点\n" +
                "- 存在的问题和不足\n" +
                "- 具体的修改建议和改进方向";
        
        try {
            // 创建聊天请求参数
            ChatCompletionCreateParams chatParams = ChatCompletionCreateParams.builder()
                    .addSystemMessage("You are a helpful assistant.")
                    .addSystemMessage("fileid://" + fileId)
                    .addUserMessage(reviewPrompt)
                    .model("qwen-long")  // 使用qwen-long模型
                    .build();
            
            System.out.println("开始批改报告...");
            
            // 流式输出批改结果
            StringBuilder fullResponse = new StringBuilder();
            try (StreamResponse<ChatCompletionChunk> streamResponse = client.chat().completions().createStreaming(chatParams)) {
                streamResponse.stream().forEach(chunk -> {
                    if (chunk.choices() != null && !chunk.choices().isEmpty()) {
                        String content = chunk.choices().get(0).delta().content().orElse("");
                        if (!content.isEmpty()) {
                            System.out.print(content); // 实时输出批改内容
                            fullResponse.append(content);
                        }
                    }
                });
                
                System.out.println("\n\n批改完成！");
                
                // 这里可以对批改结果进行进一步处理，比如解析分数、反馈等
                parseReviewResult(fullResponse.toString());
            }
        } catch (Exception e) {
            System.err.println("批改报告出错: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 解析批改结果，提取分数和反馈
     * @param reviewResult 批改结果文本
     */
    private static void parseReviewResult(String reviewResult) {
        // 这里只是简单示例，实际应用中可以使用正则表达式或其他方式提取信息
        System.out.println("\n解析批改结果：");
        
        // 提取总分
        if (reviewResult.contains("总分：") || reviewResult.contains("总分:")) {
            int scoreIndex = Math.max(reviewResult.indexOf("总分："), reviewResult.indexOf("总分:"));
            if (scoreIndex != -1) {
                int endIndex = reviewResult.indexOf("\n", scoreIndex);
                if (endIndex == -1) endIndex = reviewResult.length();
                String scoreStr = reviewResult.substring(scoreIndex, endIndex).trim();
                System.out.println("提取的分数信息: " + scoreStr);
            }
        }
        
        // 存储解析结果，可用于后续处理
        // float totalScore = extractScore(reviewResult);
        // String feedback = extractFeedback(reviewResult);
        // ...
    }
}
