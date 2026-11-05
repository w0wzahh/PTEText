package com.ptetext.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ptetext.repository.chat",
        entityManagerFactoryRef = "chatEntityManagerFactory",
        transactionManagerRef = "chatTransactionManager")
public class ChatDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.chat")
    public DataSourceProperties chatDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource chatDataSource() {
        return chatDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean chatEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(chatDataSource())
                .packages("com.ptetext.entity.chat")
                .persistenceUnit("chat")
                .build();
    }

    @Bean
    public PlatformTransactionManager chatTransactionManager(
            @Qualifier("chatEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
