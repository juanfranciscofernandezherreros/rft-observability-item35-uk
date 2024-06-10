package com.sixgroup.refit.observability.item35.creator.infrastructure.config.kudu.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.datasource.kududb")
public class DatasourceKuduProperties {

    public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_DDL_AUTO = "hibernate.ddl-auto";
    public static final String HIBERNATE_DEFAULT_SCHEMA = "hibernate.default_schema";
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String dialect;
    private String ddlAuto;
    private String persistenceUnit;
    private String schema;

}
