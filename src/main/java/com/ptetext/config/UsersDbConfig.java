package com.ptetext.config;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
        basePackages = "com.ptetext.repository.users",
        entityManagerFactoryRef = "usersEntityManagerFactory",
        transactionManagerRef = "usersTransactionManager")
public class UsersDbConfig {

    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.users")
    public DataSourceProperties usersDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource usersDataSource() {
        return usersDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Primary
    @Bean
    public LocalContainerEntityManagerFactoryBean usersEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(usersDataSource())
                .packages("com.ptetext.entity.users")
                .persistenceUnit("users")
                .build();
    }

    @Primary
    @Bean
    public PlatformTransactionManager usersTransactionManager(
            @Qualifier("usersEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
