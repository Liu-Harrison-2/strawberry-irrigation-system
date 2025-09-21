package com.strawberry.irrigation.common.config;


/*

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

//
// * 说明（简短）：通过注入 DataSourceProperties（绑定 spring.datasource.*）并构建 HikariDataSource，保持与 Spring Boot 自动配置兼容，同时便于日后按需调整连接池参数或支持多数据源。
// *
// * DataSource 配置：使用 Spring Boot 的 DataSourceProperties（绑定 spring.datasource.*）
// * 仍然保留一个显式的 DataSource Bean，方便将来扩展（多数据源或自定义连接池参数）
//

@Configuration
public class DataSourceConfig {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties, Environment env) {
        HikariDataSource ds = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        // 可在此处根据需要覆盖/补充 Hikari 参数，例如最大连接数、连接超时等
        // 示例（可选）：
        String maxPoolSize = env.getProperty("app.datasource.hikari.maximum-pool-size");
        if (maxPoolSize != null) {
            ds.setMaximumPoolSize(Integer.parseInt(maxPoolSize));
        }
        return ds;
    }
}

 */