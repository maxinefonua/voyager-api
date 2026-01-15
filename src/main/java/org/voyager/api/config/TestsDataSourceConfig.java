package org.voyager.api.config;

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
import org.springframework.transaction.annotation.EnableTransactionManagement;
import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "org.voyager.api.repository.tests",
        entityManagerFactoryRef = "testsEntityManagerFactory",
        transactionManagerRef = "testsTransactionManager"
)
public class TestsDataSourceConfig {
    @Bean
    @ConfigurationProperties("spring.tests.datasource")
    public DataSourceProperties testsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "testsDataSource")
    public DataSource testsDataSource() {
        return testsDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean(name = "testsEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean testsEntityManagerFactory(
            EntityManagerFactoryBuilder builder) {
        return builder
                .dataSource(testsDataSource())
                .packages("org.voyager.api.model.entity")  // Same entities
                .persistenceUnit("testsPersistenceUnit")
                .build();
    }

    @Bean(name = "testsTransactionManager")
    public PlatformTransactionManager testsTransactionManager(
            @Qualifier("testsEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
