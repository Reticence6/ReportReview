# 教学管理系统API接口文档

## 基础信息
- 基础URL: `http://localhost:8080/api`
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
- **成功响应**:
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
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "用户名或密码不能为空"
  }
  ```
  或
  ```json
  {
    "code": 1002,
    "message": "用户名或密码错误"
  }
  ```

### 1.2 登出
- **URL**: `/auth/logout`
- **方法**: POST
- **描述**: 用户登出
- **请求参数**: 无
- **成功响应**:
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
- **成功响应**:
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
          "studentId": "2020001",
          "courseId": 1,
          "submitTime": "2023-06-10 14:30",
          "status": 0,
          "score": 0,
          "student": {
            "id": "2020001",
            "name": "李明"
          },
          "course": {
            "id": 1,
            "name": "人工智能导论"
          }
        }
      ]
    }
  }
  ```

### 2.2 获取报告详情
- **URL**: `/reports/{id}`
- **方法**: GET
- **描述**: 获取报告详情
- **请求参数**: 无
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "id": 1,
      "title": "期末报告：人工智能在教育领域的应用",
      "studentId": "2020001",
      "courseId": 1,
      "submitTime": "2023-06-10 14:30",
      "content": "报告内容...",
      "status": 1,
      "score": 85,
      "feedback": "报告内容全面，论述有深度...",
      "scoreDetails": "[{\"criterion\":\"内容完整性\",\"score\":25,\"maxScore\":30},{\"criterion\":\"论述深度\",\"score\":25,\"maxScore\":30},{\"criterion\":\"语言表达\",\"score\":18,\"maxScore\":20},{\"criterion\":\"格式规范\",\"score\":17,\"maxScore\":20}]",
      "reviewerId": 1,
      "reviewTime": "2023-06-12 10:30",
      "student": {
        "id": "2020001",
        "name": "李明",
        "gender": 1,
        "grade": "junior",
        "major": "cs",
        "className": "1班",
        "email": "liming@example.com"
      },
      "course": {
        "id": 1,
        "name": "人工智能导论",
        "teacherId": 1,
        "semester": "2023-2024-1",
        "credit": 4
      },
      "reviewer": {
        "id": 1,
        "username": "teacher1",
        "name": "张教授",
        "role": "teacher"
      }
    }
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "报告不存在"
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
- **成功响应**:
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
- **失败响应**:
  ```json
  {
    "code": 1002,
    "message": "参数不完整"
  }
  ```
  或
  ```json
  {
    "code": 1003,
    "message": "学生不存在"
  }
  ```
  或
  ```json
  {
    "code": 1004,
    "message": "课程不存在"
  }
  ```
  或
  ```json
  {
    "code": 1005,
    "message": "上传失败"
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
- **成功响应**:
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
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "报告不存在"
  }
  ```
  或
  ```json
  {
    "code": 1006,
    "message": "评分失败"
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
  - `className`: 班级筛选
  - `keyword`: 搜索关键字（学生姓名、学号或班级）
- **成功响应**:
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
          "gender": 1,
          "grade": "junior",
          "major": "cs",
          "className": "1班",
          "email": "liming@example.com",
          "phone": "13812345678"
        }
      ]
    }
  }
  ```

### 3.2 获取学生详情
- **URL**: `/students/{id}`
- **方法**: GET
- **描述**: 获取学生详情
- **请求参数**: 无
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "id": "2020001",
      "name": "李明",
      "gender": 1,
      "grade": "junior",
      "major": "cs",
      "className": "1班",
      "email": "liming@example.com",
      "phone": "13812345678",
      "createdAt": "2023-01-01 10:00:00",
      "updatedAt": "2023-06-01 15:30:00",
      "courses": [
        {
          "id": 1,
          "name": "数据结构",
          "teacherId": 1,
          "semester": "2021-2022-1",
          "credit": 4
        }
      ],
      "reports": [
        {
          "id": 1,
          "title": "数据结构课程设计报告",
          "courseId": 1,
          "submitTime": "2021-12-15 14:30",
          "status": 1,
          "score": 90
        }
      ]
    }
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "学生不存在"
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
    "gender": 1,
    "grade": "freshman",
    "major": "cs",
    "className": "1班",
    "email": "newstudent@example.com",
    "phone": "13812345678"
  }
  ```
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "创建成功",
    "data": {
      "id": "2023001"
    }
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1002,
    "message": "学号已存在"
  }
  ```
  或
  ```json
  {
    "code": 1003,
    "message": "创建失败"
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
    "gender": 1,
    "grade": "junior",
    "major": "cs",
    "className": "1班",
    "email": "liming@example.com",
    "phone": "13812345678"
  }
  ```
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "更新成功"
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "学生不存在"
  }
  ```
  或
  ```json
  {
    "code": 1004,
    "message": "更新失败"
  }
  ```

### 3.5 删除学生
- **URL**: `/students/{id}`
- **方法**: DELETE
- **描述**: 删除学生
- **请求参数**: 无
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "删除成功"
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "学生不存在"
  }
  ```
  或
  ```json
  {
    "code": 1005,
    "message": "删除失败"
  }
  ```

### 3.6 导入学生
- **URL**: `/students/import`
- **方法**: POST
- **描述**: 批量导入学生
- **请求参数**: FormData
  - `file`: Excel文件
- **成功响应**:
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
- **成功响应**: 直接下载Excel文件

## 4. AI评分接口

### 4.1 获取AI评分设置
- **URL**: `/ai-scoring/settings`
- **方法**: GET
- **描述**: 获取AI评分设置
- **请求参数**: 无
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "id": 1,
      "enabled": true,
      "model": "standard",
      "threshold": 70,
      "humanReview": true,
      "criteria": "[{\"name\":\"内容完整性\",\"weight\":30,\"description\":\"报告是否包含所有必要的内容和部分\"},{\"name\":\"论述深度\",\"weight\":30,\"description\":\"报告的分析深度和思考质量\"},{\"name\":\"语言表达\",\"weight\":20,\"description\":\"语言的准确性、流畅性和专业性\"},{\"name\":\"格式规范\",\"weight\":20,\"description\":\"报告格式是否符合规定要求\"}]",
      "autoFeedback": true,
      "feedbackStyle": "detailed",
      "feedbackLength": 200,
      "includeSuggestions": true,
      "advanced": "{\"languageCheck\":true,\"plagiarismCheck\":true,\"referenceCheck\":false,\"relevanceAnalysis\":true}"
    }
  }
  ```

### 4.2 更新AI评分设置
- **URL**: `/ai-scoring/settings`
- **方法**: PUT
- **描述**: 更新AI评分设置
- **请求参数**: 与获取AI评分设置响应数据结构相同
- **成功响应**:
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
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "评分任务已创建",
    "data": {
      "taskId": "task123456"
    }
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "请上传报告文件"
  }
  ```

### 4.4 获取AI评分状态
- **URL**: `/ai-scoring/task/{taskId}`
- **方法**: GET
- **描述**: 获取AI评分任务状态
- **请求参数**: 无
- **成功响应**:
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
- **失败响应**:
  ```json
  {
    "code": 1002,
    "message": "任务不存在"
  }
  ```

### 4.5 确认AI评分结果
- **URL**: `/ai-scoring/confirm/{reportId}`
- **方法**: POST
- **描述**: 确认AI评分结果
- **请求参数**: 无
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "确认成功"
  }
  ```
- **失败响应**:
  ```json
  {
    "code": 1003,
    "message": "报告不存在"
  }
  ```
  或
  ```json
  {
    "code": 1004,
    "message": "报告状态不是AI评分"
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
- **成功响应**:
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
- **失败响应**:
  ```json
  {
    "code": 1001,
    "message": "消息不能为空"
  }
  ```

### 5.2 获取聊天历史
- **URL**: `/chat/history`
- **方法**: GET
- **描述**: 获取聊天历史记录
- **请求参数**:
  - `page`: 页码，默认1
  - `pageSize`: 每页数量，默认20
- **成功响应**:
  ```json
  {
    "code": 0,
    "message": "成功",
    "data": {
      "total": 100,
      "list": [
        {
          "id": 1,
          "userId": 0,
          "type": "ai",
          "content": "您好！我是教学管理系统的智能助手，有什么可以帮助您的吗？",
          "time": "14:30",
          "createdAt": "2023-06-10 14:30:00",
          "updatedAt": "2023-06-10 14:30:00"
        },
        {
          "id": 2,
          "userId": 1,
          "type": "user",
          "content": "如何上传报告？",
          "time": "14:31",
          "createdAt": "2023-06-10 14:31:00",
          "updatedAt": "2023-06-10 14:31:00"
        },
        {
          "id": 3,
          "userId": 0,
          "type": "ai",
          "content": "您可以在报告管理页面点击"上传报告"按钮...",
          "time": "14:31",
          "createdAt": "2023-06-10 14:31:10",
          "updatedAt": "2023-06-10 14:31:10"
        }
      ]
    }
  }
  ```

## 6. 数据字典

### 报告状态（Report.status）
- 0: 待审核（pending）
- 1: 已评审（reviewed）
- 2: 已拒绝（rejected）
- 3: AI评分（ai）

### 性别（Student.gender）
- 0: 女（female）
- 1: 男（male）

### 年级（Student.grade）
- freshman: 大一
- sophomore: 大二
- junior: 大三
- senior: 大四

### 专业（Student.major）
- cs: 计算机科学
- se: 软件工程
- ai: 人工智能
- ds: 数据科学

### 用户角色（User.role）
- admin: 管理员
- teacher: 教师

### 消息类型（ChatMessage.type）
- user: 用户消息
- ai: AI消息

### AI评分任务状态（AiScoringTask.status）
- processing: 处理中
- completed: 已完成
- error: 错误 