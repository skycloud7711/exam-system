package com.wang.exam.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
//Redis的java化使用，往Redis里存JSON形式的可读化数据
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        //连接Redis
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        //创建JSON序列化器，
        //Spring Data Redis 从3.3升到3.5，GenericJackson2JsonRedisSerializer的行为变了
        //旧版用new构造器加自定义ObjectMapper，需要手动activateDefaultTyping记录类型信息，而3.内部会强制加类型信息
        GenericJackson2JsonRedisSerializer jsonSerializer =
                GenericJackson2JsonRedisSerializer.builder()
                        .objectMapper(objectMapper())
                        .build();

        //构造JSON序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        //Redis有普通key-value和hash两种结构，各有一套key和value
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        //手动new出来的template必须调它
        template.afterPropertiesSet();
        return template;
    }

    private ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        //让Json认识Java 8的时间类型
        om.registerModule(new JavaTimeModule());
        //禁用时间戳
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //让json能访问所有字段，包括private
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        return om;
    }
}