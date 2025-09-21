// 文件位置: src/main/java/com/strawberry/irrigation/common/config/RedisConfig.java
package com.strawberry.irrigation.common.config;

/*

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.*;


//
// * 说明（简短）：RedisConfig 提供 RedisConnectionFactory 与 RedisTemplate<String,Object>，value 使用 JSON 序列化，便于缓存实体/DTO。若部署使用哨兵/集群，请按实际场景替换 LettuceConnectionFactory 的构造方式。
// * Redis 配置


@Configuration
public class RedisConfig {

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 使用默认 spring.redis.* 配置（lettuce）
        // 如果需要自定义（如哨兵、集群），在此处构建对应的 RedisStandaloneConfiguration / RedisSentinelConfiguration 等
        return new LettuceConnectionFactory();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // key 使用 String
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // value 使用 Jackson JSON 序列化（便于存储对象）
        GenericJackson2JsonRedisSerializer jacksonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setValueSerializer(jacksonSerializer);
        template.setHashValueSerializer(jacksonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}

 */
