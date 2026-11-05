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
        basePackages = "com.ptetext.repository.system",
        entityManagerFactoryRef = "systemEntityManagerFactory",
        transactionManagerRef = "systemTransactionManager")
public class SystemDbConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.system")
    public DataSourceProperties systemDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource systemDataSource() {
        return systemDataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean systemEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(systemDataSource())
                .packages("com.ptetext.entity.system")
                .persistenceUnit("system")
                .build();
    }

    @Bean
    public PlatformTransactionManager systemTransactionManager(
            @Qualifier("systemEntityManagerFactory") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
