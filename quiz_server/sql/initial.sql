-- ============================================
-- 刷题系统 — 数据库初始化脚本（可重复执行）
-- MySQL 8.0
-- ============================================

DROP DATABASE IF EXISTS quiz;
CREATE DATABASE quiz DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE quiz;

-- ==================== 基础表 ====================

DROP TABLE IF EXISTS login_attempts;
DROP TABLE IF EXISTS email_codes;
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS announcements;
DROP TABLE IF EXISTS exam_answers;
DROP TABLE IF EXISTS exam_records;
DROP TABLE IF EXISTS wrong_questions;
DROP TABLE IF EXISTS favorites;
DROP TABLE IF EXISTS questions;
DROP TABLE IF EXISTS question_set_classes;
DROP TABLE IF EXISTS question_sets;
DROP TABLE IF EXISTS question_types;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS preset_students;
DROP TABLE IF EXISTS schools;

-- ==================== 建表 ====================

-- 学校表（为多校扩展预留）
CREATE TABLE schools (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '学校名称',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='学校预设表';

-- 学校人员预设表（管理员导入，学生注册时比对）
CREATE TABLE preset_students (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_no      VARCHAR(50)  NOT NULL COMMENT '学号',
    name            VARCHAR(50)  NOT NULL COMMENT '姓名',
    class_name      VARCHAR(100) NOT NULL COMMENT '班级名',
    school_id       BIGINT       NOT NULL COMMENT '学校ID',
    is_registered   TINYINT      DEFAULT 0 COMMENT '是否已注册 0=否 1=是',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_school_class (school_id, class_name),
    INDEX idx_student_no (student_no)
) ENGINE=InnoDB COMMENT='学校人员预设表';

-- 用户表
CREATE TABLE users (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码(bcrypt)',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    role            ENUM('student','teacher','admin') NOT NULL DEFAULT 'student' COMMENT '角色',
    nickname        VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    student_no      VARCHAR(50)  DEFAULT NULL COMMENT '学号',
    real_name       VARCHAR(50)  DEFAULT NULL COMMENT '真实姓名',
    class_name      VARCHAR(100) DEFAULT NULL COMMENT '班级',
    school_id       BIGINT       DEFAULT NULL COMMENT '学校ID',
    token           VARCHAR(255) DEFAULT NULL COMMENT '登录token',
    avatar_url      VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    status          TINYINT      DEFAULT 1 COMMENT '状态 1=正常 0=禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_token (token)
) ENGINE=InnoDB COMMENT='用户表';

-- 题型表
CREATE TABLE question_types (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    type_code       VARCHAR(30)  NOT NULL UNIQUE COMMENT '题型编码',
    type_name       VARCHAR(30)  NOT NULL COMMENT '题型中文名',
    has_options     TINYINT      DEFAULT 1 COMMENT '是否有选项',
    option_type     VARCHAR(20)  DEFAULT 'single' COMMENT 'single/multiple/none',
    answer_format   VARCHAR(20)  DEFAULT 'single_key' COMMENT 'single_key/multi_key/text_list/text/code',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='题型表';

-- 试题集表
CREATE TABLE question_sets (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_id      BIGINT       NOT NULL COMMENT '创建者(教师)ID',
    title           VARCHAR(200) NOT NULL COMMENT '试题集名称',
    description     VARCHAR(500) DEFAULT NULL COMMENT '描述',
    remark          VARCHAR(500) DEFAULT NULL COMMENT '备注',
    set_type        ENUM('practice','exam') NOT NULL DEFAULT 'practice' COMMENT '类型(预留考试)',
    is_public       TINYINT      DEFAULT 0 COMMENT '是否公开 0=仅授权班级 1=公开',
    open_time       DATETIME     DEFAULT NULL COMMENT '开放时间',
    close_time      DATETIME     DEFAULT NULL COMMENT '关闭时间',
    question_count  INT          DEFAULT 0 COMMENT '题目总数',
    favorite_count  INT          DEFAULT 0 COMMENT '收藏人数',
    status          TINYINT      DEFAULT 1 COMMENT '状态 1=正常 0=禁用',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_teacher (teacher_id),
    INDEX idx_type (set_type)
) ENGINE=InnoDB COMMENT='试题集表';

-- 试题集-班级关联表
CREATE TABLE question_set_classes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_set_id BIGINT       NOT NULL COMMENT '试题集ID',
    class_name      VARCHAR(100) NOT NULL COMMENT '班级名',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_set (question_set_id),
    UNIQUE KEY uk_set_class (question_set_id, class_name)
) ENGINE=InnoDB COMMENT='试题集-班级关联表';

-- 题目表
CREATE TABLE questions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_set_id BIGINT       NOT NULL COMMENT '所属试题集ID',
    question_type_id INT         NOT NULL COMMENT '题型ID',
    content         TEXT         NOT NULL COMMENT '题干(支持HTML)',
    options         JSON         DEFAULT NULL COMMENT '选项 [{label:"A",content:"..."}]',
    answer          JSON         NOT NULL COMMENT '答案(格式由题型决定)',
    explanation     TEXT         DEFAULT NULL COMMENT '解析(支持HTML含img)',
    difficulty      ENUM('easy','medium','hard') DEFAULT 'medium' COMMENT '难度',
    tags            VARCHAR(500) DEFAULT NULL COMMENT '知识点标签 逗号分隔',
    sort_order      INT          DEFAULT 0 COMMENT '排序',
    status          TINYINT      DEFAULT 1 COMMENT '状态',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_set (question_set_id),
    INDEX idx_type (question_type_id),
    INDEX idx_difficulty (difficulty)
) ENGINE=InnoDB COMMENT='题目表';

-- 收藏表
CREATE TABLE favorites (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '学生ID',
    question_set_id BIGINT       NOT NULL COMMENT '试题集ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_set (user_id, question_set_id),
    INDEX idx_user (user_id)
) ENGINE=InnoDB COMMENT='收藏表(收藏即创建该集错题集)';

-- 错题表
CREATE TABLE wrong_questions (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '学生ID',
    question_id     BIGINT       NOT NULL COMMENT '题目ID',
    question_set_id BIGINT       NOT NULL COMMENT '来源试题集ID',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question (user_id, question_id),
    INDEX idx_user (user_id),
    INDEX idx_user_set (user_id, question_set_id)
) ENGINE=InnoDB COMMENT='错题表';

-- 答题记录表
CREATE TABLE exam_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '学生ID',
    question_set_id BIGINT       NOT NULL COMMENT '试题集ID',
    mode            ENUM('practice','exam') NOT NULL COMMENT '答题模式',
    total_count     INT          DEFAULT 0 COMMENT '总题数',
    correct_count   INT          DEFAULT 0 COMMENT '正确数',
    wrong_count     INT          DEFAULT 0 COMMENT '错误数',
    duration        INT          DEFAULT 0 COMMENT '用时(秒)',
    is_finished     TINYINT      DEFAULT 0 COMMENT '是否完成 0=进行中 1=已完成',
    finished_at     DATETIME     DEFAULT NULL COMMENT '完成时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_user_set (user_id, question_set_id)
) ENGINE=InnoDB COMMENT='答题记录表(考试+普通)';

-- 答题明细表
CREATE TABLE exam_answers (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_id       BIGINT       NOT NULL COMMENT '答题记录ID',
    question_id     BIGINT       NOT NULL COMMENT '题目ID',
    user_answer     JSON         DEFAULT NULL COMMENT '用户答案',
    is_correct      TINYINT      DEFAULT NULL COMMENT '是否正确 0=错 1=对',
    answered_at     DATETIME     DEFAULT NULL COMMENT '作答时间',
    INDEX idx_record (record_id)
) ENGINE=InnoDB COMMENT='答题明细表';

-- 系统公告表
CREATE TABLE announcements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL COMMENT '标题',
    content         TEXT         NOT NULL COMMENT '内容',
    publisher_id    BIGINT       NOT NULL COMMENT '发布者(管理员)ID',
    is_active       TINYINT      DEFAULT 1 COMMENT '是否展示',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB COMMENT='系统公告';

-- 审计日志表
CREATE TABLE audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT       NOT NULL COMMENT '操作用户ID',
    username        VARCHAR(50)  DEFAULT NULL COMMENT '用户名快照',
    action          VARCHAR(100) NOT NULL COMMENT '操作描述',
    target_type     VARCHAR(50)  DEFAULT NULL COMMENT '操作对象类型',
    target_id       BIGINT       DEFAULT NULL COMMENT '操作对象ID',
    detail          TEXT         DEFAULT NULL COMMENT '操作详情(JSON)',
    ip              VARCHAR(50)  DEFAULT NULL COMMENT 'IP地址',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user (user_id),
    INDEX idx_created (created_at)
) ENGINE=InnoDB COMMENT='审计日志';

-- 邮箱验证码表
CREATE TABLE email_codes (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(100) NOT NULL COMMENT '邮箱',
    code            VARCHAR(10)  NOT NULL COMMENT '验证码',
    purpose         ENUM('register','login','reset','modify') NOT NULL COMMENT '用途',
    is_used         TINYINT      DEFAULT 0 COMMENT '是否已使用',
    expires_at      DATETIME     NOT NULL COMMENT '过期时间',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email_purpose (email, purpose)
) ENGINE=InnoDB COMMENT='邮箱验证码';

-- 登录尝试记录表
CREATE TABLE login_attempts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip              VARCHAR(50)  NOT NULL COMMENT 'IP地址',
    username        VARCHAR(50)  DEFAULT NULL COMMENT '尝试的用户名',
    success         TINYINT      DEFAULT 0 COMMENT '是否成功',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_ip_time (ip, created_at)
) ENGINE=InnoDB COMMENT='登录尝试记录(防暴力破解)';
