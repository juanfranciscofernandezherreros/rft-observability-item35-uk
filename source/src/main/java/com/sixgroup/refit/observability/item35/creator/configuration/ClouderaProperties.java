package com.sixgroup.refit.observability.item35.creator.configuration;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties("component-config.cloudera")
public class ClouderaProperties {

    @NotNull
    private boolean knoxAuth;
    @NotNull
    private final Cpu cpu = new Cpu();
    @NotNull
    private final Ram ram = new Ram();
    @NotNull
    private final Storage storage = new Storage();

    @Getter
    @Setter
    public static class Cpu {
        @NotNull
        private boolean enabled;
        @NotNull
        private String selectCpu;
        @NotNull
        private String desiredRollup;
    }

    @Getter
    @Setter
    public static class Ram {
        @NotNull
        private boolean enabled;
        @NotNull
        private String selectRam;
        @NotNull
        private String desiredRollup;
    }

    @Getter
    @Setter
    public static class Storage {
        @NotNull
        private String selectTotalApi;
        @NotNull
        private String selectFreeApi;
        @NotNull
        private String desiredRollup;
        @NotNull
        private boolean enabled;
        @NotNull
        private String entityName;
    }
}
