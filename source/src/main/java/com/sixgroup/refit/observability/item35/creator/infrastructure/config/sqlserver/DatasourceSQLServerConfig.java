
package com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver;

import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
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

import static com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver.DatasourceMssqlProperties.HIBERNATE_DDL_AUTO;
import static com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver.DatasourceMssqlProperties.HIBERNATE_DIALECT;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "sqlserver-em",
        transactionManagerRef = "sqlserver-trm",
        basePackages = "com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver"
)
@EnableTransactionManagement
@RequiredArgsConstructor
public class DatasourceSQLServerConfig {

    private final DatasourceMssqlProperties properties;

    @Bean("sqlserver-ds")
    public DataSource sqlserverDataSource() {
        return DataSourceBuilder.create()
                .url(properties.getJdbcUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    @Bean("sqlserver-em")
    LocalContainerEntityManagerFactoryBean sqlserverDbEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("sqlserver-ds") DataSource dataSource) {
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(HIBERNATE_DIALECT, properties.getDialect());
        objectObjectHashMap.put(HIBERNATE_DDL_AUTO, properties.getDdlAuto());
        return builder
                .dataSource(dataSource)
                .packages(ItemReportingEntity.class)
                .persistenceUnit(properties.getPersistenceUnit())
                .properties(objectObjectHashMap)
                .build();
    }

    @Bean("sqlserver-trm")
    PlatformTransactionManager sqlserverTransactionManager(@Qualifier("sqlserver-em") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}

