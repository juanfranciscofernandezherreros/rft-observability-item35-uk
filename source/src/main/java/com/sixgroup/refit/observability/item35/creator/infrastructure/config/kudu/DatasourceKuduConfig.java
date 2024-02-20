package com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu;


import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.kudu.RecordStatusEntity;
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
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "kudu-em",
        transactionManagerRef = "kudu-trm",
        basePackages = "com.sixgroup.refit.observability.item35.creator.infrastructure.repository.kudu"
)
@EnableTransactionManagement
public class DatasourceKuduConfig {

    @Value("${spring.datasource.kududb.url}")
    private String kuduDbUrl;

    @Bean("kudu-ds")
    @ConfigurationProperties(prefix = "spring.datasource.kududb")
    public DataSource kuduDataSource(){
        return DataSourceBuilder.create()
                .driverClassName("com.cloudera.impala.jdbc.Driver")
                .url(kuduDbUrl)
                .build();
    }

    @Bean("kudu-em")
    LocalContainerEntityManagerFactoryBean kuduDbEntityManagerFactory (EntityManagerFactoryBuilder builder, @Qualifier("kudu-ds") DataSource dataSource){
        return builder
                .dataSource(dataSource)
                .packages(RecordStatusEntity.class)
                .persistenceUnit("kududb")
                .properties(Collections.singletonMap(
                        "hibernate.dialect",
                        "org.hibernate.dialect.HSQLDialect"
                ))

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
