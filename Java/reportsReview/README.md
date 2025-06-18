# AI报告批改系统

基于通义大模型Qwen-Long的实验报告自动批改系统，通过大模型技术对学生提交的报告进行智能评审与批改。

## 目录

- [系统概述](#系统概述)
- [开发环境搭建](#开发环境搭建)
- [基础设施配置](#基础设施配置)
- [系统功能模块](#系统功能模块)
- [使用指南](#使用指南)
- [API接口文档](#API接口文档)
- [常见问题](#常见问题)

## 系统概述

本系统是一个基于Spring Boot和通义千问大模型的报告批改平台，旨在帮助教师高效地对学生提交的实验报告进行批改和评分。系统通过调用通义千问(Qwen-Long)大模型API，实现对文档内容的深度理解和多维度评价。

### 主要特点

- **智能批改**：自动分析报告内容，从多个维度进行评分
- **多格式支持**：支持Word、PDF等主流文档格式
- **灵活配置**：可自定义评分标准和权重
- **详细反馈**：提供具体修改建议和评分详情
- **人机协作**：支持AI初评后人工复核的工作流

## 开发环境搭建

### 软件要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- IntelliJ IDEA（推荐）或Eclipse
- Git

### 本地开发环境配置

1. **克隆代码库**

```bash
git clone git@github.com:Reticence6/ReportReview.git
cd /java/reportsReview
```

2. **配置IDE**

- 使用IntelliJ IDEA打开项目
- 确保Project SDK设置为JDK 17
- 配置Maven（可使用IDE内置Maven或系统安装的Maven）

3. **导入依赖**

```bash
# 使用Maven命令行
mvn clean install

# 或在IDE中刷新Maven项目
```

4. **配置开发环境变量**

```bash
# Linux/Mac
export DASHSCOPE_API_KEY=your_api_key_here

# Windows
set DASHSCOPE_API_KEY=your_api_key_here
```

## 基础设施配置

### 数据库配置

1. **创建MySQL数据库**

```sql
CREATE DATABASE `reports-review` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **初始化数据库表结构**

```bash
# 执行SQL脚本
mysql -u username -p reports-review < docs/create_tables.sql
```

或通过项目首次启动自动创建表结构（确保application.properties中hibernate.hbm2ddl.auto设置适当）

### 应用配置文件

修改`src/main/resources/application.properties`文件:

```properties
# 数据源配置
spring.datasource.url=jdbc:mysql://localhost:3306/reports-review?useSSL=false&useUnicode=true&characterEncoding=utf8
spring.datasource.username=your_username
spring.datasource.password=your_password

# 通义千问API配置
qianwen.api.key=your_api_key_here
qianwen.api.url=https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation

# 文件上传配置
spring.servlet.multipart.max-file-size=50MB
spring.servlet.multipart.max-request-size=100MB
```

### 运行应用

**方式一：使用Maven**

```bash
mvn spring-boot:run
```

**方式二：使用IDE**

在IDE中直接运行`ReportsReviewApplication.java`主类

**方式三：构建JAR包运行**

```bash
mvn package
java -jar target/reportsReview-[版本号].jar
```

## 系统功能模块

### 1. 用户认证模块

- 用户注册、登录、权限管理
- 支持教师和学生两种角色

### 2. 报告管理模块

- 学生：报告提交、查看评分结果
- 教师：报告列表管理、批量操作

### 3. AI批改模块

- 报告文件上传及解析
- 调用通义千问API进行智能评分
- 多维度评分机制
- 评分结果展示与存储



## 使用指南

### 1. 教师使用流程

1. 登录系统
2. 创建课程及报告要求
3. 查看学生提交的报告
4. 审核AI批改结果，调整评分
5. 发布最终评分结果



## API接口文档

### 认证接口

#### 登录

```
POST /api/auth/login
Content-Type: application/json

{
  "username": "string",
  "password": "string"
}
```

#### 获取当前用户信息

```
GET /api/auth/user
Authorization: Bearer {token}
```

### 报告管理接口

#### 上传报告

```
POST /api/reports/upload
Content-Type: multipart/form-data
Authorization: Bearer {token}

file: (文件)
courseId: number
title: string
description: string
```

#### 获取报告列表

```
GET /api/reports
Authorization: Bearer {token}
```

### AI评分接口

#### 获取AI评分设置

```
GET /api/ai-scoring/settings
Authorization: Bearer {token}
```

#### 更新AI评分设置

```
PUT /api/ai-scoring/settings
Content-Type: application/json
Authorization: Bearer {token}

{
  "enabled": true,
  "model": "standard",
  "threshold": 75,
  "humanReview": true,
  "criteria": [...],
  ...
}
```

#### 提交报告进行AI评分

```
POST /api/ai-scoring/score
Content-Type: multipart/form-data
Authorization: Bearer {token}

files: [文件1, 文件2, ...]
```

#### 获取评分任务状态

```
GET /api/ai-scoring/task/{taskId}
Authorization: Bearer {token}
```

#### 确认AI评分结果

```
POST /api/ai-scoring/confirm/{reportId}
Authorization: Bearer {token}
```

## 常见问题

### 1. API密钥如何获取？

需要在阿里云通义平台申请通义千问API密钥，网址：https://dashscope.aliyun.com

### 2. 如何处理大文件上传失败？

- 检查`application.properties`中的文件大小限制设置
- 确认网络连接稳定
- 尝试将大文件分拆上传

### 3. 评分结果不准确怎么办？

- 检查评分标准配置是否合理
- 调整模型参数或提示词模板
- 使用人工复核功能修正评分

### 4. 系统性能优化建议

- 使用多线程处理批量评分任务
- 增加服务器内存配置
- 配置数据库连接池
- 实现结果缓存机制

