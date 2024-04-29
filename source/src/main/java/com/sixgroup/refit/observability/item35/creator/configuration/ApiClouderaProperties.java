package com.sixgroup.refit.observability.item35.creator.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ToString
@ConfigurationProperties("api.cloudera")
public class ApiClouderaProperties {

    @NotNull
    private String host;
    @NotNull
    private String url;
    @NotNull
    private String username;
    @NotNull
    private String password;
    @NotNull
    private String port;
}
