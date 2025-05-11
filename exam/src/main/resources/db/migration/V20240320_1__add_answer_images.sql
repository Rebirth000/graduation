-- 添加答题图片字段
ALTER TABLE exam_answer
ADD COLUMN answer_images VARCHAR(1000) DEFAULT NULL COMMENT '答题图片URL列表，JSON格式';
 
-- 更新已有记录的answer_images字段为空JSON数组
UPDATE exam_answer
SET answer_images = '[]'
WHERE answer_images IS NULL; 