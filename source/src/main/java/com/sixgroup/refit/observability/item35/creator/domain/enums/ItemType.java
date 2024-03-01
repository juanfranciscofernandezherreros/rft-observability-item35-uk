package com.sixgroup.refit.observability.item35.creator.domain.enums;

public enum ItemType {


    SUBMISSION_VOLUMES("submissionVolumes", "TRRGS_EMIR_PR_IN_ND_ITEM35A_");

    private final String name;
    private final String namePattern;

    ItemType(final String name, final String namePattern) {
        this.name = name;
        this.namePattern = namePattern;
    }

    public String getName() {
        return name;
    }

    public String getNamePattern() {
        return namePattern;
    }
}
