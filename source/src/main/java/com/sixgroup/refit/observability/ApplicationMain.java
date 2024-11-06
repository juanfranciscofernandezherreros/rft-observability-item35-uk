package com.sixgroup.refit.observability;


import com.sixgroup.refit.observability.app.properties.SlaProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.*;
import com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.properties.DatasourceKuduProperties;
import com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver.DatasourceMssqlProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({CsvProperties.class, DatasourceMssqlProperties.class, DatasourceKuduProperties.class,
    ApiClouderaProperties.class, ClouderaProperties.class, ParticipantProperties.class, RegulatorProperties.class,
    TrProperties.class, ReportProperties.class, SlaProperties.class})
public class ApplicationMain {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationMain.class, args);
    }
}
