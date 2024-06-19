package com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.account;


import com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.properties.DatasourceKuduProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.account.ReguIdentityEntity;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;

import static com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.properties.DatasourceKuduProperties.*;

@Configuration
@EnableJpaRepositories(
    entityManagerFactoryRef = "kuduac-em",
    transactionManagerRef = "kuduac-trm",
    basePackages = "com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu.account"
)
@EnableTransactionManagement
@RequiredArgsConstructor
public class DatasourceKuduAccountConfig {

    private final DatasourceKuduProperties properties;

    @Value("${component-config.kududb-account.schema}")
    private String schema;

    @Bean("kuduac-ds")
    public DataSource kuduDataSource() {
        return DataSourceBuilder.create()
            .url(properties.getJdbcUrl())
            .username(properties.getUsername())
            .password(properties.getPassword())
            .driverClassName(properties.getDriverClassName())
            .build();
    }

    @Bean("kuduac-em")
    LocalContainerEntityManagerFactoryBean kuduDbEntityManagerFactory(EntityManagerFactoryBuilder builder, @Qualifier("kuduac-ds") DataSource dataSource) {
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(HIBERNATE_DIALECT, properties.getDialect());
        objectObjectHashMap.put(HIBERNATE_DDL_AUTO, properties.getDdlAuto());
        objectObjectHashMap.put(HIBERNATE_DEFAULT_SCHEMA, schema);
        return builder
            .dataSource(dataSource)
            .packages(ReguIdentityEntity.class)
            .persistenceUnit(properties.getPersistenceUnit())
            .properties(objectObjectHashMap)
            .build();
    }

    @Bean("kuduac-trm")
    PlatformTransactionManager kuduTransactionManager(@Qualifier("kuduac-em") EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

}
