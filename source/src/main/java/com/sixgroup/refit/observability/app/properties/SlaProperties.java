package com.sixgroup.refit.observability.app.properties;

import com.sixgroup.refit.observability.modules.validate.domain.data.Report;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "component-config.sla")
public class SlaProperties {
    private Map<String, List<Report>> entity = new HashMap<>();
}
