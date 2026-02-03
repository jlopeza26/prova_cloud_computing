package com.institut.cloudapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.boot.jdbc.DataSourceBuilder;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class DataSourceConfig {

    @Value("${spring.datasource.driver-class-name:org.postgresql.Driver}")
    private String driverClassName;

    @Bean
    @Primary
    public DataSource dataSource(Environment env) {
        // Prefer explicit DB_URL if provided (expects a JDBC URL)
        String dbUrl = env.getProperty("DB_URL");
        String dbUser = env.getProperty("DB_USER");
        String dbPassword = env.getProperty("DB_PASSWORD");

        if (dbUrl != null && !dbUrl.isBlank()) {
            return DataSourceBuilder.create()
                    .url(dbUrl)
                    .username(dbUser)
                    .password(dbPassword)
                    .driverClassName(driverClassName)
                    .build();
        }

        // Fallback: check JDBC_DATABASE_URL (already in JDBC format), e.g. provided by some PaaS
        String jdbcDatabaseUrl = env.getProperty("JDBC_DATABASE_URL");
        if (jdbcDatabaseUrl != null && !jdbcDatabaseUrl.isBlank()) {
            String jdbcUser = env.getProperty("JDBC_DATABASE_USERNAME", dbUser);
            String jdbcPass = env.getProperty("JDBC_DATABASE_PASSWORD", dbPassword);
            return DataSourceBuilder.create()
                    .url(jdbcDatabaseUrl)
                    .username(jdbcUser)
                    .password(jdbcPass)
                    .driverClassName(driverClassName)
                    .build();
        }

        // Fallback: parse DATABASE_URL (e.g. postgres://user:pass@host:port/dbname)
        String databaseUrl = env.getProperty("DATABASE_URL");
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            try {
                URI uri = new URI(databaseUrl);

                String userInfo = uri.getUserInfo();
                String username = null;
                String password = null;
                if (userInfo != null) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    if (parts.length > 1) password = parts[1];
                }

                StringBuilder url = new StringBuilder();
                url.append("jdbc:postgresql://")
                        .append(uri.getHost())
                        .append( uri.getPort() == -1 ? "" : ":" + uri.getPort() )
                        .append(uri.getPath());

                if (uri.getQuery() != null && !uri.getQuery().isBlank()) {
                    url.append("?").append(uri.getQuery());
                }

                return DataSourceBuilder.create()
                        .url(url.toString())
                        .username(username)
                        .password(password)
                        .driverClassName(driverClassName)
                        .build();

            } catch (URISyntaxException e) {
                throw new IllegalStateException("Invalid DATABASE_URL format", e);
            }
        }

        // If nothing is provided, return default builder which will let Spring Boot try auto-config
        return DataSourceBuilder.create().driverClassName(driverClassName).build();
    }
}
