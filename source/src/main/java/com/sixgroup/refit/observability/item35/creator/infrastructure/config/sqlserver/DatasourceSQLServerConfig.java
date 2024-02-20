
package com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "sqlserver-em",
        transactionManagerRef = "sqlserver-trm",
        basePackages = "com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver"
)
@EnableTransactionManagement
public class DatasourceSQLServerConfig {

    @Value("${spring.datasource.sqlserverdb.url}")
    private String sqlServerDbUrl;

    @Value("${spring.datasource.sqlserverdb.hibernate.ddl-auto}")
    private String ddlAuto;

    @Value("${spring.datasource.sqlserverdb.hibernate.default_schema}")
    private String schema;

    @Value("${spring.datasource.sqlserverdb.hibernate.dialect}")
    private String dialect;


    @Bean("sqlserver-ds")
    @ConfigurationProperties(prefix = "spring.datasource.sqlserverdb")
    public DataSource sqlserverDataSource() {
        return DataSourceBuilder
                .create()
                .url(sqlServerDbUrl)
                .build();
    }

    @Bean("sqlserver-em")
    LocalContainerEntityManagerFactoryBean sqlserverDbEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("sqlserver-ds") DataSource dataSource) {
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put("hibernate.dialect", dialect);
        objectObjectHashMap.put("hibernate.hbm2ddl.auto", ddlAuto);
        objectObjectHashMap.put("hibernate.default_schema", schema);

        return builder
                .dataSource(dataSource)
                .packages(ItemReportingEntity.class)
                .persistenceUnit("sqlserverdb")
                .properties(objectObjectHashMap)
                .build();
    }

    @Bean("sqlserver-trm")
    PlatformTransactionManager sqlserverTransactionManager(@Qualifier("sqlserver-em") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}

