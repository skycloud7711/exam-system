package com.wang.exam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
//创建调用判卷服务的RestClient
public class RestClientConfig {

    @Bean
    public RestClient scoreRestClient() {

        //请求工厂
        var factory = new JdkClientHttpRequestFactory();

        //3 秒后抛异常，熔断器接管，标记 PENDING_SCORE
        factory.setReadTimeout(Duration.ofSeconds(3));

        //创建RestClient的构建器
        return RestClient.builder()
                .baseUrl("http://localhost:8081")
                .requestFactory(factory)
                .build();
    }
}