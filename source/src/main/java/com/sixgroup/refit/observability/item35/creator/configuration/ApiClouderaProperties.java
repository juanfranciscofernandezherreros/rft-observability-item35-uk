package com.sixgroup.refit.observability.item35.creator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("api.cloudera")
public class ApiClouderaProperties {

    private String host;
    private String url;
    private String username;
    private String password;
    private String port;
}
