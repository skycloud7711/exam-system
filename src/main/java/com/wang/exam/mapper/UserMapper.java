package com.wang.exam.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.exam.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}