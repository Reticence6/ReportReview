package com.wjk.reportsreview.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjk.reportsreview.common.Result;
import com.wjk.reportsreview.entity.Course;
import com.wjk.reportsreview.entity.Report;
import com.wjk.reportsreview.entity.Student;
import com.wjk.reportsreview.mapper.CourseMapper;
import com.wjk.reportsreview.mapper.ReportMapper;
import com.wjk.reportsreview.mapper.StudentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private CourseMapper courseMapper;
    
    @Autowired
    private ReportMapper reportMapper;

    /**
     * 获取学生列表
     * @param page 页码
     * @param pageSize 每页数量
     * @param grade 年级筛选
     * @param major 专业筛选
     * @param className 班级筛选
     * @param keyword 搜索关键字
     * @return 学生列表
     */
    @GetMapping
    public Result<Map<String, Object>> getStudents(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String keyword) {
        
        // 构建查询条件
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        
        // 添加筛选条件
        if (grade != null && !grade.isEmpty()) {
            queryWrapper.eq("grade", grade);
        }
        
        if (major != null && !major.isEmpty()) {
            queryWrapper.eq("major", major);
        }
        
        if (className != null && !className.isEmpty()) {
            queryWrapper.eq("class_name", className);
        }
        
        // 添加关键字搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                    .like("name", keyword)
                    .or()
                    .like("id", keyword)
                    .or()
                    .like("class_name", keyword)
            );
        }
        
        // 执行分页查询
        Page<Student> pageResult = new Page<>(page, pageSize);
        Page<Student> studentPage = studentMapper.selectPage(pageResult, queryWrapper);
        
        // 组装返回结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("total", studentPage.getTotal());
        resultMap.put("list", studentPage.getRecords());
        
        return Result.success(resultMap);
    }
    
    /**
     * 获取学生详情
     * @param id 学生ID
     * @return 学生详情
     */
    @GetMapping("/{id}")
    public Result<Student> getStudentById(@PathVariable String id) {
        // 查询学生基本信息
        Student student = studentMapper.selectById(id);
        if (student == null) {
            return Result.error(1001, "学生不存在");
        }
        
        // 查询学生课程信息
        QueryWrapper<Course> courseQueryWrapper = new QueryWrapper<>();
        courseQueryWrapper.inSql("id", "SELECT course_id FROM student_course WHERE student_id = '" + id + "'");
        List<Course> courses = courseMapper.selectList(courseQueryWrapper);
        student.setCourses(courses);
        
        // 查询学生报告信息
        QueryWrapper<Report> reportQueryWrapper = new QueryWrapper<>();
        reportQueryWrapper.eq("student_id", id);
        List<Report> reports = reportMapper.selectList(reportQueryWrapper);
        student.setReports(reports);
        
        return Result.success(student);
    }
    
    /**
     * 创建学生
     * @param student 学生信息
     * @return 创建结果
     */
    @PostMapping
    public Result<Map<String, Object>> createStudent(@RequestBody Student student) {
        // 检查学号是否已存在
        Student existStudent = studentMapper.selectById(student.getId());
        if (existStudent != null) {
            return Result.error(1002, "学号已存在");
        }
        
        // 插入学生记录
        int result = studentMapper.insert(student);
        if (result > 0) {
            Map<String, Object> data = new HashMap<>();
            data.put("id", student.getId());
            return Result.success("创建成功", data);
        } else {
            return Result.error(1003, "创建失败");
        }
    }
    
    /**
     * 更新学生信息
     * @param id 学生ID
     * @param student 学生信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<?> updateStudent(@PathVariable String id, @RequestBody Student student) {
        // 检查学生是否存在
        Student existStudent = studentMapper.selectById(id);
        if (existStudent == null) {
            return Result.error(1001, "学生不存在");
        }
        
        // 设置ID
        student.setId(id);
        
        // 更新学生记录
        int result = studentMapper.updateById(student);
        if (result > 0) {
            return Result.success("更新成功");
        } else {
            return Result.error(1004, "更新失败");
        }
    }
    
    /**
     * 删除学生
     * @param id 学生ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteStudent(@PathVariable String id) {
        // 检查学生是否存在
        Student existStudent = studentMapper.selectById(id);
        if (existStudent == null) {
            return Result.error(1001, "学生不存在");
        }
        
        // 删除学生记录
        int result = studentMapper.deleteById(id);
        if (result > 0) {
            return Result.success("删除成功");
        } else {
            return Result.error(1005, "删除失败");
        }
    }
    
    /**
     * 导入学生
     * @param file Excel文件
     * @return 导入结果
     */
    @PostMapping("/import")
    public Result<Map<String, Object>> importStudents(@RequestParam("file") MultipartFile file) {
        // 实际项目中应该解析Excel文件并批量导入学生
        // 这里简单模拟导入成功
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("success", 10);
        resultMap.put("failed", 0);
        
        return Result.success("导入成功", resultMap);
    }
    
    /**
     * 导出学生
     * @param grade 年级筛选
     * @param major 专业筛选
     * @param className 班级筛选
     * @param keyword 搜索关键字
     * @return 导出结果
     */
    @GetMapping("/export")
    public Result<?> exportStudents(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className,
            @RequestParam(required = false) String keyword) {
        // 实际项目中应该根据筛选条件查询学生并导出Excel文件
        // 这里简单返回成功信息
        return Result.success("导出成功");
    }
}
