CREATE TABLE IF NOT EXISTS course (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL COMMENT '课程名称',
    description TEXT COMMENT '课程描述',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 添加外键约束到试卷表
ALTER TABLE exam_paper
ADD COLUMN course_id INT COMMENT '关联的课程ID',
ADD CONSTRAINT fk_exam_paper_course 
FOREIGN KEY (course_id) REFERENCES course(id); 