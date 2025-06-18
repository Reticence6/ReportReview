# 教学管理系统数据库设计

## 数据库表结构

根据系统需求，我们设计了以下数据库表结构：

### 1. 用户表（users）
存储系统用户信息，包括教师和管理员。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 用户ID                 |
| username   | VARCHAR(50)  | UNIQUE, NOT NULL | 用户名               |
| password   | VARCHAR(255) | NOT NULL      | 密码（加密存储）        |
| name       | VARCHAR(50)  | NOT NULL      | 姓名                   |
| role       | VARCHAR(20)  | NOT NULL      | 角色（admin/teacher）   |
| email      | VARCHAR(100) |               | 邮箱                   |
| phone      | VARCHAR(20)  |               | 电话                   |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 2. 学生表（students）
存储学生基本信息。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | VARCHAR(20)  | PK            | 学号                   |
| name       | VARCHAR(50)  | NOT NULL      | 姓名                   |
| gender     | TINYINT(1) | NOT NULL      | 性别（0-male/1-female） |
| grade      | VARCHAR(20)  | NOT NULL      | 年级                   |
| major      | VARCHAR(50)  | NOT NULL      | 专业                   |
| class      | VARCHAR(20)  | NOT NULL      | 班级                   |
| email      | VARCHAR(100) |               | 邮箱                   |
| phone      | VARCHAR(20)  |               | 电话                   |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 3. 课程表（courses）
存储课程信息。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 课程ID                 |
| name       | VARCHAR(100) | NOT NULL      | 课程名称                |
| teacher_id | INT          | FK            | 教师ID，关联users表     |
| semester   | VARCHAR(20)  | NOT NULL      | 学期                   |
| credit     | FLOAT        | NOT NULL      | 学分                   |
| description| TEXT         |               | 课程描述                |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 4. 学生课程表（student_courses）
存储学生选课信息，建立学生和课程的多对多关系。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 记录ID                 |
| student_id | VARCHAR(20)  | FK, NOT NULL  | 学生ID，关联students表  |
| course_id  | INT          | FK, NOT NULL  | 课程ID，关联courses表   |
| score      | FLOAT        |               | 成绩                   |
| grade      | VARCHAR(5)   |               | 等级（A+/A/A-/B+...）   |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 5. 报告表（reports）
存储学生提交的报告。

| 字段名          | 类型         | 约束           | 描述                   |
|----------------|--------------|---------------|------------------------|
| id             | INT          | PK, AUTO_INC  | 报告ID                 |
| title          | VARCHAR(200) | NOT NULL      | 报告标题                |
| student_id     | VARCHAR(20)  | FK, NOT NULL  | 学生ID，关联students表  |
| course_id      | INT          | FK, NOT NULL  | 课程ID，关联courses表   |
| submit_time    | TIMESTAMP    | NOT NULL      | 提交时间                |
| content        | TEXT         |               | 报告内容                |
| status         | INT | NOT NULL      | 状态（0-pending/1-reviewed/2-rejected/3-ai） |
| score          | FLOAT        |               | 分数                   |
| feedback       | TEXT         |               | 评语                   |
| score_details  | JSON         |               | 评分详情                |
| reviewer_id    | INT          | FK            | 评审人ID，关联users表   |
| review_time    | TIMESTAMP    |               | 评审时间                |
| created_at     | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at     | TIMESTAMP    | NOT NULL      | 更新时间                |

### 6. 报告附件表（report_attachments）
存储报告的附件文件。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 附件ID                 |
| report_id  | INT          | FK, NOT NULL  | 报告ID，关联reports表   |
| name       | VARCHAR(100) | NOT NULL      | 文件名                 |
| path       | VARCHAR(255) | NOT NULL      | 文件路径                |
| size       | INT          | NOT NULL      | 文件大小（字节）        |
| type       | VARCHAR(50)  | NOT NULL      | 文件类型                |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 7. AI评分设置表（ai_scoring_settings）
存储AI评分的配置信息。

| 字段名            | 类型         | 约束           | 描述                   |
|------------------|--------------|---------------|------------------------|
| id               | INT          | PK, AUTO_INC  | 设置ID                 |
| enabled          | BOOLEAN      | NOT NULL      | 是否启用                |
| model            | VARCHAR(50)  | NOT NULL      | 评分模型                |
| threshold        | INT          | NOT NULL      | 评分阈值                |
| human_review     | BOOLEAN      | NOT NULL      | 是否需要人工复核        |
| criteria         | JSON         | NOT NULL      | 评分标准                |
| auto_feedback    | BOOLEAN      | NOT NULL      | 是否自动生成评语        |
| feedback_style   | VARCHAR(20)  | NOT NULL      | 评语风格                |
| feedback_length  | INT          | NOT NULL      | 评语长度                |
| include_suggestions | BOOLEAN   | NOT NULL      | 是否包含改进建议        |
| advanced         | JSON         | NOT NULL      | 高级设置                |
| created_at       | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at       | TIMESTAMP    | NOT NULL      | 更新时间                |

### 8. AI评分任务表（ai_scoring_tasks）
存储AI评分任务信息。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | VARCHAR(50)  | PK            | 任务ID                 |
| status     | VARCHAR(20)  | NOT NULL      | 状态（processing/completed/error） |
| progress   | INT          | NOT NULL      | 进度（0-100）          |
| message    | VARCHAR(255) |               | 状态消息                |
| created_by | INT          | FK, NOT NULL  | 创建人ID，关联users表   |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 9. AI评分结果表（ai_scoring_results）
存储AI评分结果。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 结果ID                 |
| task_id    | VARCHAR(50)  | FK, NOT NULL  | 任务ID，关联ai_scoring_tasks表 |
| report_id  | INT          | FK            | 报告ID，关联reports表   |
| filename   | VARCHAR(100) | NOT NULL      | 文件名                 |
| student_id | VARCHAR(20)  | FK            | 学生ID，关联students表  |
| score      | FLOAT        | NOT NULL      | 得分                   |
| confidence | FLOAT        | NOT NULL      | 置信度                 |
| confirmed  | BOOLEAN      | NOT NULL      | 是否已确认              |
| confirmed_by | INT        | FK            | 确认人ID，关联users表   |
| confirmed_at | TIMESTAMP  |               | 确认时间                |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

### 10. 聊天消息表（chat_messages）
存储智能助手的聊天记录。

| 字段名      | 类型         | 约束           | 描述                   |
|------------|--------------|---------------|------------------------|
| id         | INT          | PK, AUTO_INC  | 消息ID                 |
| user_id    | INT          | FK            | 用户ID，关联users表     |
| type       | VARCHAR(10)  | NOT NULL      | 消息类型（user/ai）     |
| content    | TEXT         | NOT NULL      | 消息内容                |
| time       | TIMESTAMP    | NOT NULL      | 发送时间                |
| created_at | TIMESTAMP    | NOT NULL      | 创建时间                |
| updated_at | TIMESTAMP    | NOT NULL      | 更新时间                |

## 表关系图

```
users ----|--< courses
          |
          |--< reports (reviewer_id)
          |
          |--< ai_scoring_tasks (created_by)
          |
          |--< ai_scoring_results (confirmed_by)
          |
          |--< chat_messages

students -|--< student_courses >--| courses
          |
          |--< reports
          |
          |--< ai_scoring_results

reports --|--< report_attachments
          |
          |--< ai_scoring_results

ai_scoring_tasks --< ai_scoring_results
```