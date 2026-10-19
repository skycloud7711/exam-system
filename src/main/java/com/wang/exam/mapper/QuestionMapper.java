package com.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.exam.entity.Question;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}