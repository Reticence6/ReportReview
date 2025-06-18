# 教学管理系统后端接口文档

## 基础信息
- 基础URL: `/api`
- 响应格式: JSON
- 认证方式: JWT Token（在请求头中添加 `Authorization: Bearer {token}`）

## 通用响应格式
```json
{
  "code": 0,       // 0表示成功，非0表示错误
  "message": "成功", // 响应消息
  "data": {}       // 响应数据
}
```

## 1. 用户认证接口

### 1.1 登录
- **URL**: `/auth/login`
- **方法**: POST
- **描述**: 用户登录获取token
- **请求参数**:
  ```json
  {
    "username": "admin",
    "password": "password"
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "登录成功",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "id": 1,
        "username": "admin",
        "name": "管理员",
        "role": "admin"
      }
    }
  }
  ```

### 1.2 登出
- **URL**: `/auth/logout`
- **方法**: POST
- **描述**: 用户登出
- **响应**:
  ```json
  {
    "code": 0,
    "message": "登出成功"
  }
  ```

## 2. 报告管理接口

### 2.1 获取报告列表
- **URL**: `/reports`
- **方法**: GET
- **描述**: 获取报告列表，支持分页和筛选
- **请求参数**:
  - `page`: 页码，默认1
  - `pageSize`: 每页数量，默认10
  - `status`: 状态筛选（pending/reviewed/rejected/ai）
  - `date`: 日期筛选（today/week/month）
  - `keyword`: 搜索关键字（报告标题或学生姓名）
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "total": 100,
      "list": [
        {
          "id": 1,
          "title": "期末报告",
          "student": "李明",
          "studentId": "2020001",
          "submitTime": "2023-06-10 14:30",
          "status": "pending"
        }
      ]
    }
  }
  ```

### 2.2 获取报告详情
- **URL**: `/reports/{id}`
- **方法**: GET
- **描述**: 获取报告详情
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "id": 1,
      "title": "期末报告：人工智能在教育领域的应用",
      "student": "李明",
      "studentId": "2020001",
      "submitTime": "2023-06-10 14:30",
      "course": "人工智能导论",
      "teacher": "张教授",
      "status": "pending",
      "score": 85,
      "content": ["报告内容第一段", "报告内容第二段"],
      "attachments": [
        {"name": "期末报告.pdf", "size": "2.5MB", "url": "/uploads/reports/1/report.pdf"},
        {"name": "研究数据.xlsx", "size": "1.2MB", "url": "/uploads/reports/1/data.xlsx"}
      ],
      "scoreDetails": [
        {"criterion": "内容完整性", "score": 25, "maxScore": 30},
        {"criterion": "论述深度", "score": 25, "maxScore": 30},
        {"criterion": "语言表达", "score": 18, "maxScore": 20},
        {"criterion": "格式规范", "score": 17, "maxScore": 20}
      ],
      "feedback": "报告内容全面，论述有深度..."
    }
  }
  ```

### 2.3 上传报告
- **URL**: `/reports/upload`
- **方法**: POST
- **描述**: 上传新报告
- **请求参数**: FormData
  - `title`: 报告标题
  - `studentId`: 学生ID
  - `courseId`: 课程ID
  - `file`: 报告文件
- **响应**:
  ```json
  {
    "code": 0,
    "message": "上传成功",
    "data": {
      "id": 10,
      "title": "新上传的报告",
      "status": "pending"
    }
  }
  ```

### 2.4 提交报告评分
- **URL**: `/reports/{id}/score`
- **方法**: POST
- **描述**: 提交报告评分
- **请求参数**:
  ```json
  {
    "score": 85,
    "scoreDetails": [
      {"criterion": "内容完整性", "score": 25, "maxScore": 30},
      {"criterion": "论述深度", "score": 25, "maxScore": 30},
      {"criterion": "语言表达", "score": 18, "maxScore": 20},
      {"criterion": "格式规范", "score": 17, "maxScore": 20}
    ],
    "feedback": "评语内容..."
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "评分成功",
    "data": {
      "id": 1,
      "score": 85,
      "status": "reviewed"
    }
  }
  ```

## 3. 学生管理接口

### 3.1 获取学生列表
- **URL**: `/students`
- **方法**: GET
- **描述**: 获取学生列表，支持分页和筛选
- **请求参数**:
  - `page`: 页码，默认1
  - `pageSize`: 每页数量，默认10
  - `grade`: 年级筛选（freshman/sophomore/junior/senior）
  - `major`: 专业筛选（cs/se/ai/ds）
  - `class`: 班级筛选
  - `keyword`: 搜索关键字（学生姓名、学号或班级）
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "total": 100,
      "list": [
        {
          "id": "2020001",
          "name": "李明",
          "gender": "male",
          "grade": "junior",
          "major": "cs",
          "class": "1班",
          "email": "liming@example.com"
        }
      ]
    }
  }
  ```

### 3.2 获取学生详情
- **URL**: `/students/{id}`
- **方法**: GET
- **描述**: 获取学生详情
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "id": "2020001",
      "name": "李明",
      "gender": "male",
      "grade": "junior",
      "major": "cs",
      "class": "1班",
      "email": "liming@example.com",
      "phone": "13812345678",
      "gpa": 3.6,
      "reportCompletionRate": 85,
      "courses": [
        {"name": "数据结构", "credit": 4, "score": 92, "grade": "A", "semester": "2021-2022-1"},
        {"name": "计算机网络", "credit": 3, "score": 88, "grade": "B+", "semester": "2021-2022-2"}
      ],
      "reports": [
        {"id": 1, "title": "数据结构课程设计报告", "course": "数据结构", "submitTime": "2021-12-15 14:30", "status": "reviewed", "score": 90},
        {"id": 2, "title": "计算机网络实验报告", "course": "计算机网络", "submitTime": "2022-05-20 10:20", "status": "reviewed", "score": 85}
      ]
    }
  }
  ```

### 3.3 创建学生
- **URL**: `/students`
- **方法**: POST
- **描述**: 创建新学生
- **请求参数**:
  ```json
  {
    "id": "2023001",
    "name": "新学生",
    "gender": "male",
    "grade": "freshman",
    "major": "cs",
    "class": "1班",
    "email": "newstudent@example.com",
    "phone": "13812345678"
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "创建成功",
    "data": {
      "id": "2023001"
    }
  }
  ```

### 3.4 更新学生信息
- **URL**: `/students/{id}`
- **方法**: PUT
- **描述**: 更新学生信息
- **请求参数**:
  ```json
  {
    "name": "李明（修改）",
    "gender": "male",
    "grade": "junior",
    "major": "cs",
    "class": "1班",
    "email": "liming@example.com",
    "phone": "13812345678"
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "更新成功"
  }
  ```

### 3.5 删除学生
- **URL**: `/students/{id}`
- **方法**: DELETE
- **描述**: 删除学生
- **响应**:
  ```json
  {
    "code": 0,
    "message": "删除成功"
  }
  ```

### 3.6 导入学生
- **URL**: `/students/import`
- **方法**: POST
- **描述**: 批量导入学生
- **请求参数**: FormData
  - `file`: Excel文件
- **响应**:
  ```json
  {
    "code": 0,
    "message": "导入成功",
    "data": {
      "success": 10,
      "failed": 0
    }
  }
  ```

### 3.7 导出学生
- **URL**: `/students/export`
- **方法**: GET
- **描述**: 导出学生名单
- **请求参数**: 与获取学生列表相同（支持筛选条件）
- **响应**: 直接下载Excel文件

## 4. AI评分接口

### 4.1 获取AI评分设置
- **URL**: `/ai-scoring/settings`
- **方法**: GET
- **描述**: 获取AI评分设置
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "enabled": true,
      "model": "standard",
      "threshold": 70,
      "humanReview": true,
      "criteria": [
        {"name": "内容完整性", "weight": 30, "description": "报告是否包含所有必要的内容和部分"},
        {"name": "论述深度", "weight": 30, "description": "报告的分析深度和思考质量"},
        {"name": "语言表达", "weight": 20, "description": "语言的准确性、流畅性和专业性"},
        {"name": "格式规范", "weight": 20, "description": "报告格式是否符合规定要求"}
      ],
      "autoFeedback": true,
      "feedbackStyle": "detailed",
      "feedbackLength": 200,
      "includeSuggestions": true,
      "advanced": {
        "languageCheck": true,
        "plagiarismCheck": true,
        "referenceCheck": false,
        "relevanceAnalysis": true
      }
    }
  }
  ```

### 4.2 更新AI评分设置
- **URL**: `/ai-scoring/settings`
- **方法**: PUT
- **描述**: 更新AI评分设置
- **请求参数**: 与获取AI评分设置响应数据结构相同
- **响应**:
  ```json
  {
    "code": 0,
    "message": "更新成功"
  }
  ```

### 4.3 AI评分报告
- **URL**: `/ai-scoring/score`
- **方法**: POST
- **描述**: 使用AI评分报告
- **请求参数**: FormData
  - `files`: 报告文件数组
- **响应**:
  ```json
  {
    "code": 0,
    "message": "评分任务已创建",
    "data": {
      "taskId": "task123456"
    }
  }
  ```

### 4.4 获取AI评分状态
- **URL**: `/ai-scoring/task/{taskId}`
- **方法**: GET
- **描述**: 获取AI评分任务状态
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "taskId": "task123456",
      "status": "processing",  // processing/completed/error
      "progress": 50,
      "message": "正在处理第2个文件",
      "results": [
        {
          "id": 1,
          "filename": "report1.pdf",
          "student": "李明",
          "score": 85,
          "confidence": 90,
          "reportId": 10
        }
      ]
    }
  }
  ```

### 4.5 确认AI评分结果
- **URL**: `/ai-scoring/confirm/{reportId}`
- **方法**: POST
- **描述**: 确认AI评分结果
- **响应**:
  ```json
  {
    "code": 0,
    "message": "确认成功"
  }
  ```

## 5. 智能助手接口

### 5.1 发送消息
- **URL**: `/chat/message`
- **方法**: POST
- **描述**: 发送消息到智能助手
- **请求参数**:
  ```json
  {
    "message": "如何上传报告？"
  }
  ```
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "reply": "您可以在报告管理页面点击"上传报告"按钮，选择要上传的文件，填写相关信息后提交即可。如果您想使用AI自动评分功能，可以在AI评分页面上传报告。",
      "suggestions": [
        "如何修改已上传的报告？",
        "报告上传有大小限制吗？",
        "支持哪些文件格式？",
        "如何批量上传报告？"
      ]
    }
  }
  ```

### 5.2 获取聊天历史
- **URL**: `/chat/history`
- **方法**: GET
- **描述**: 获取聊天历史记录
- **请求参数**:
  - `page`: 页码，默认1
  - `pageSize`: 每页数量，默认20
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "total": 100,
      "list": [
        {
          "id": 1,
          "type": "ai",
          "content": "您好！我是教学管理系统的智能助手，有什么可以帮助您的吗？",
          "time": "14:30"
        },
        {
          "id": 2,
          "type": "user",
          "content": "如何上传报告？",
          "time": "14:31"
        },
        {
          "id": 3,
          "type": "ai",
          "content": "您可以在报告管理页面点击"上传报告"按钮...",
          "time": "14:31"
        }
      ]
    }
  }
  ```

## 6. 统计分析接口

### 6.1 获取首页统计数据
- **URL**: `/stats/dashboard`
- **方法**: GET
- **描述**: 获取首页统计数据
- **响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "studentCount": 120,
      "reportCount": 350,
      "pendingReportCount": 25,
      "aiScoredCount": 180,
      "recentActivities": [
        {"content": "张老师批改了李明的期末报告", "time": "2023-06-10 14:30", "type": "success"},
        {"content": "系统使用AI自动评分了15份报告", "time": "2023-06-10 10:20", "type": "primary"}
      ]
    }
  }
  ```

## 数据模型

### 用户（User）
- id: 用户ID
- username: 用户名
- password: 密码（加密存储）
- name: 姓名
- role: 角色（admin/teacher）

### 学生（Student）
- id: 学号
- name: 姓名
- gender: 性别（male/female）
- grade: 年级（freshman/sophomore/junior/senior）
- major: 专业（cs/se/ai/ds）
- class: 班级
- email: 邮箱
- phone: 电话

### 课程（Course）
- id: 课程ID
- name: 课程名称
- teacher: 教师ID
- semester: 学期
- credit: 学分

### 学生课程（StudentCourse）
- id: 记录ID
- studentId: 学生ID
- courseId: 课程ID
- score: 成绩
- grade: 等级（A+/A/A-/B+/B/...）

### 报告（Report）
- id: 报告ID
- title: 标题
- studentId: 学生ID
- courseId: 课程ID
- submitTime: 提交时间
- content: 内容
- status: 状态（pending/reviewed/rejected/ai）
- score: 分数
- feedback: 评语
- scoreDetails: 评分详情（JSON）

### AI评分设置（AiScoringSettings）
- id: 设置ID
- enabled: 是否启用
- model: 评分模型
- threshold: 评分阈值
- humanReview: 是否需要人工复核
- criteria: 评分标准（JSON）
- autoFeedback: 是否自动生成评语
- feedbackStyle: 评语风格
- feedbackLength: 评语长度
- includeSuggestions: 是否包含改进建议
- advanced: 高级设置（JSON）

### 聊天记录（ChatMessage）
- id: 消息ID
- userId: 用户ID
- type: 消息类型（user/ai）
- content: 消息内容
- time: 发送时间

## 开发说明

1. 所有接口需要进行参数验证
2. 使用统一的错误处理机制
3. 文件上传接口需要限制文件大小和类型
4. AI评分相关接口可以使用异步任务处理
5. 实现接口访问权限控制
6. 提供详细的API文档和测试用例 