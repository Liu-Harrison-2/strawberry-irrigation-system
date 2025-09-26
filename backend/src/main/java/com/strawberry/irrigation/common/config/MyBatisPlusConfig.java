package com.strawberry.irrigation.common.config;


import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * MyBatis-Plus配置类
 */
@Configuration
@MapperScan("com.strawberry.irrigation.**.dao")
public class MyBatisPlusConfig {

    /**
     * 注册自定义类型处理器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();

        // 分页插件
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());

        // 乐观锁插件
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());

        return interceptor;
    }

    /**
     * 自动填充配置
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                // 插入时自动填充创建时间和更新时间
                this.setFieldValByName("createdAt", OffsetDateTime.now(), metaObject);
                this.setFieldValByName("updatedAt", OffsetDateTime.now(), metaObject);
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                // 更新时自动填充更新时间
                this.setFieldValByName("updatedAt", OffsetDateTime.now(), metaObject);
            }
        };
    }
}