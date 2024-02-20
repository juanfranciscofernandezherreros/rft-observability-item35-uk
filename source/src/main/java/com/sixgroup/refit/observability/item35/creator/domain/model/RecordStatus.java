package com.sixgroup.refit.observability.item35.creator.domain.model;

public record RecordStatus(
        String reportingDate,
        String messageType,
        String submissionChannel,
        long noMessagesOnGiveDate){
}
