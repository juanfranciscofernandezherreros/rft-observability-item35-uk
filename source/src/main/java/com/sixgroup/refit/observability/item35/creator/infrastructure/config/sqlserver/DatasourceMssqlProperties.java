package com.sixgroup.refit.observability.item35.creator.infrastructure.config.sqlserver;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "spring.datasource.sqlserverdb")
public class DatasourceMssqlProperties {

    public static final String HIBERNATE_DIALECT = "hibernate.dialect";
    public static final String HIBERNATE_DDL_AUTO = "hibernate.ddl-auto";
    private String jdbcUrl;
    private String username;
    private String password;
    private String driverClassName;
    private String dialect;
    private String ddlAuto;
    private String persistenceUnit;

}
