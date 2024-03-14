package com.sixgroup.refit.observability.item35.creator.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("component-config.api.cloudera")
public class ApiClouderaProperties {

    private String host;
    private String url;
    private String username;
    private String password;
    private String port;
    private Cpu cpu = new Cpu();
    private Ram ram = new Ram();

    @Data
    public static class Cpu {
        private boolean enabled;
        private String selectCpu;
        private String desiredRollup;
    }

    @Data
    public static class Ram {
        private boolean enabled;
        private String selectRam;
        private String desiredRollup;
    }

}
