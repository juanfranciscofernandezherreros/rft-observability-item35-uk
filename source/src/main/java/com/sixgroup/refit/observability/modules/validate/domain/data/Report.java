package com.sixgroup.refit.observability.modules.validate.domain.data;

import lombok.Data;

@Data
public class Report {
    private String name;
    private String slaInit;
    private String slaEnd;
}
