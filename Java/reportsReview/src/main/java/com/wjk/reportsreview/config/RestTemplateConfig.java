package com.wjk.reportsreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(
            // 使用BufferingClientHttpRequestFactory允许多次读取响应体
            new BufferingClientHttpRequestFactory(getClientHttpRequestFactory())
        );
        
        // 设置自定义错误处理器
        restTemplate.setErrorHandler(new CustomResponseErrorHandler());
        
        return restTemplate;
    }

    private ClientHttpRequestFactory getClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时时间（15秒）
        factory.setConnectTimeout(15000);
        // 读取超时时间（30秒）
        factory.setReadTimeout(30000);
        // 输出请求体
        factory.setOutputStreaming(true);
        return factory;
    }
    
    /**
     * 自定义响应错误处理器，用于记录详细的API错误信息
     */
    private static class CustomResponseErrorHandler implements ResponseErrorHandler {
        
        private final DefaultResponseErrorHandler defaultHandler = new DefaultResponseErrorHandler();
        
        @Override
        public boolean hasError(ClientHttpResponse response) throws IOException {
            return defaultHandler.hasError(response);
        }
        
        @Override
        public void handleError(ClientHttpResponse response) throws IOException {
            // 记录详细错误信息
            HttpStatusCode statusCode = response.getStatusCode();
            String responseBody = readResponseBody(response);
            
            System.err.println("API调用失败: 状态码=" + statusCode.value() + ", 原因=" + response.getStatusText());
            System.err.println("错误响应体: " + responseBody);
            
            // 交给默认处理器处理
            defaultHandler.handleError(response);
        }
        
        private String readResponseBody(ClientHttpResponse response) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(response.getBody(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            } catch (IOException e) {
                return "无法读取响应体: " + e.getMessage();
            }
        }
    }
} 