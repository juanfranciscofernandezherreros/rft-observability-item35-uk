package com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu;


import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusEntity;
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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;

import static com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.DatasourceKuduProperties.HIBERNATE_DDL_AUTO;
import static com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.DatasourceKuduProperties.HIBERNATE_DIALECT;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "kudu-em",
        transactionManagerRef = "kudu-trm",
        basePackages = "com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu"
)
@EnableTransactionManagement
@RequiredArgsConstructor
public class DatasourceKuduConfig {

    private final DatasourceKuduProperties properties;

    @Bean("kudu-ds")
    public DataSource kuduDataSource(){
        return DataSourceBuilder.create()
                .url(properties.getJdbcUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .driverClassName(properties.getDriverClassName())
                .build();
    }

    @Bean("kudu-em")
    LocalContainerEntityManagerFactoryBean kuduDbEntityManagerFactory (EntityManagerFactoryBuilder builder, @Qualifier("kudu-ds") DataSource dataSource){
        final HashMap<String, String> objectObjectHashMap = new HashMap<>();
        objectObjectHashMap.put(HIBERNATE_DIALECT, properties.getDialect());
        objectObjectHashMap.put(HIBERNATE_DDL_AUTO, properties.getDdlAuto());
        return builder
                .dataSource(dataSource)
                .packages(RecordStatusEntity.class)
                .persistenceUnit(properties.getPersistenceUnit())
                .properties(objectObjectHashMap)
                .build();
    }

    @Bean("kudu-trm")
    PlatformTransactionManager kuduTransactionManager(@Qualifier("kudu-em") EntityManagerFactory entityManagerFactory){
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public EntityManagerFactoryBuilder entityManagerFactoryBuilder() {
        return new EntityManagerFactoryBuilder(new HibernateJpaVendorAdapter(), new HashMap<>(), null);
    }

}
