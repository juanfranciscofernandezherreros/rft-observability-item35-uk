package com.sixgroup.refit.observability;


import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.DatasourceKuduProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver.DatasourceMssqlProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver.DatasourceSQLServerConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableConfigurationProperties({CsvProperties.class, DatasourceMssqlProperties.class, DatasourceKuduProperties.class})
public class ApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }
}
