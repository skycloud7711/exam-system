1.初始化数据库
CREATE DATABASE exam_system DEFAULT CHARSET utf8mb4;

USE exam_system;

CREATE TABLE `user` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `username` VARCHAR(50) UNIQUE NOT NULL,
  `password` VARCHAR(100) NOT NULL,
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE `exam` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `start_time` DATETIME NOT NULL,
  `end_time` DATETIME NOT NULL
);

CREATE TABLE `question` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `exam_id` BIGINT NOT NULL,
  `question_no` VARCHAR(20) NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `options` VARCHAR(1000) NOT NULL,
  `answer` VARCHAR(50) NOT NULL,
  `score` INT NOT NULL
);

CREATE TABLE `answer_record` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
  `exam_id` BIGINT NOT NULL,
  `student_id` BIGINT NOT NULL,
  `score` INT,
  `scored_time` DATETIME,
  `answers` TEXT,
  `submit_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
  `status` VARCHAR(20) NOT NULL,
  `retry_count` INT DEFAULT 0,
  UNIQUE KEY `uk_exam_student` (`exam_id`, `student_id`)
);

2.
src/main/resources/application.yaml 填上你自己的数据库账户名和密码

3.
src/main/java/com/wang/exam/common/JwtUtil.java 填上你自己的密钥

4.
开启redis

5.
开启判卷微服务
