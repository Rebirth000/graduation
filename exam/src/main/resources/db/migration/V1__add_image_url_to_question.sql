-- 向question表添加image_url字段
ALTER TABLE question ADD COLUMN image_url VARCHAR(255) COMMENT '题目图片URL' AFTER options;