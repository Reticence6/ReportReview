package com.wjk.reportsreview.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    /**
     * 配置跨域请求过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许跨域的头部信息
        config.addAllowedHeader("*");
        // 允许跨域的方法
        config.addAllowedMethod("*");
        // 允许跨域的源
        //config.addAllowedOrigin("*");
        // 允许跨域的源，修改为特定的前端地址
        config.addAllowedOrigin("http://localhost:8081");
        // 允许携带cookie
        config.setAllowCredentials(true);
        // 暴露头部信息
        config.addExposedHeader("Content-Type");
        config.addExposedHeader("X-Requested-With");
        config.addExposedHeader("accept");
        config.addExposedHeader("Origin");
        config.addExposedHeader("Access-Control-Request-Method");
        config.addExposedHeader("Access-Control-Request-Headers");
        config.addExposedHeader("Authorization");
        
        // 添加映射路径，拦截一切请求
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
} 