-- ============================================
-- 刷题系统 — 初始数据脚本
-- ============================================

USE quiz;

-- ==================== 学校 ====================
INSERT INTO schools (id, name) VALUES (1, '测试第一中学');

-- ==================== 预设学生 ====================
INSERT INTO preset_students (student_no, name, class_name, school_id) VALUES
('2024001', '张三', '2024级计算机1班', 1),
('2024002', '李四', '2024级计算机1班', 1),
('2024003', '王五', '2024级计算机2班', 1);

-- ==================== 题型 ====================
INSERT INTO question_types (type_code, type_name, has_options, option_type, answer_format, sort_order) VALUES
('single_choice', '单选题', 1, 'single',   'single_key',  1),
('multi_choice',  '多选题', 1, 'multiple', 'multi_key',   2),
('true_false',    '判断题', 1, 'single',   'single_key',  3),
('fill_blank',    '填空题', 0, 'none',     'text_list',   4),
('essay',         '简答题', 0, 'none',     'text',        5),
('coding',        '编程题', 0, 'none',     'code',        6);

-- ==================== 用户（密码均为 123456，MD5 加密） ====================
-- MD5("123456") = e10adc3949ba59abbe56e057f20f883e
INSERT INTO users (username, password, email, role, nickname, real_name, class_name, school_id, token) VALUES
('admin',     'e10adc3949ba59abbe56e057f20f883e', 'admin@quiz.com',   'admin',   '系统管理员', NULL, NULL, NULL, NULL),
('teacher1',  'e10adc3949ba59abbe56e057f20f883e', 'teacher@quiz.com', 'teacher', '张老师',     '张伟', NULL, NULL, NULL),
('student1',  'e10adc3949ba59abbe56e057f20f883e', 'student@quiz.com', 'student', '张三',       '张三', '2024级计算机1班', 1, NULL);

-- ==================== 示例试题集 ====================
INSERT INTO question_sets (id, teacher_id, title, description, is_public, question_count, favorite_count) VALUES
(1, 2, 'Java基础题库', '涵盖Java基础语法、面向对象等知识点', 1, 4, 0),
(2, 2, '计算机网络',   'OSI模型、TCP/IP协议栈等',          0, 2, 0);

-- 试题集1授权班级
INSERT INTO question_set_classes (question_set_id, class_name) VALUES
(1, '2024级计算机1班'),
(1, '2024级计算机2班'),
(2, '2024级计算机1班');

-- ==================== 示例题目（试题集1） ====================
INSERT INTO questions (question_set_id, question_type_id, content, options, answer, explanation, difficulty, tags) VALUES
-- 单选题
(1, 1,
 'Java中，以下哪个关键字用于定义类？',
 '[{"label":"A","content":"extends"},{"label":"B","content":"implement"},{"label":"C","content":"class"},{"label":"D","content":"interface"}]',
 '"C"',
 'class是Java中用于定义类的关键字。extends用于继承，interface用于定义接口。',
 'easy', 'Java基础,关键字'),

-- 多选题
(1, 2,
 '以下哪些是Java的基本数据类型？',
 '[{"label":"A","content":"int"},{"label":"B","content":"String"},{"label":"C","content":"boolean"},{"label":"D","content":"float"}]',
 '["A","C","D"]',
 'String是引用类型，不是基本数据类型。Java的8种基本类型：byte,short,int,long,float,double,char,boolean。',
 'easy', 'Java基础,数据类型'),

-- 判断题
(1, 3,
 'Java中一个类可以实现多个接口。',
 '[{"label":"A","content":"正确"},{"label":"B","content":"错误"}]',
 '"A"',
 'Java支持多接口实现，但只支持单继承。',
 'easy', 'Java基础,接口'),

-- 填空题
(1, 4,
 '在Java中，使用_____关键字定义常量（不可修改的变量）。',
 NULL,
 '"final"',
 'final关键字修饰的变量一旦赋值后不可修改。',
 'easy', 'Java基础,关键字'),

-- 试题集2题目
(2, 1,
 'OSI参考模型中，哪一层负责路由选择？',
 '[{"label":"A","content":"物理层"},{"label":"B","content":"数据链路层"},{"label":"C","content":"网络层"},{"label":"D","content":"传输层"}]',
 '"C"',
 '网络层（第三层）负责路由选择和数据包的转发。',
 'medium', '计算机网络,OSI模型'),

(2, 3,
 'TCP和UDP都属于传输层协议。',
 '[{"label":"A","content":"正确"},{"label":"B","content":"错误"}]',
 '"A"',
 'TCP和UDP都工作于OSI模型的传输层（第四层）。',
 'easy', '计算机网络,传输层');
