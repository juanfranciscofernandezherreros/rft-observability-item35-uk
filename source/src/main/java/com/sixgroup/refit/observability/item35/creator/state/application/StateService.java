package com.sixgroup.refit.observability.item35.creator.state.application;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.state.domain.State;
import com.sixgroup.refit.observability.item35.creator.state.domain.StateRequest;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class StateService {

    private static final String EXTERNAL_REQUEST_RECEIVED_STATE = "external_request_received";

    private final ItemFileFinderRepository itemFileFinderRepository;

    public List<ItemReportingDto> nextStep(final List<StateRequest> stateRequests) {
        List<ItemReportingDto> reportingDtos = new ArrayList<>();
        for (StateRequest stateRequest : stateRequests) {
            ItemReportingDto itemReportingDto = nextStepTo(stateRequest, Boolean.FALSE);
            reportingDtos.add(itemReportingDto);
        }
        return reportingDtos;
    }

    public ItemReportingDto setError(final ItemFileFinderRequest finderRequest) {
        final ItemReportingDto fileFound = itemFileFinderRepository.findByItemTypeAndFileName(finderRequest.getItemType(), finderRequest.getFileName());

        if (Objects.nonNull(fileFound)) {
            return updateItemRepotingRecord(fileFound, State.ERROR);
        }
        return null;
    }

    public ItemReportingDto nextStep(final StateRequest stateRequest) {
        return nextStepTo(stateRequest, Boolean.FALSE);
    }

    public ItemReportingDto nextStepAlternative(final StateRequest stateRequest) {
        return nextStepTo(stateRequest, Boolean.TRUE);
    }

    private ItemReportingDto nextStepTo(StateRequest stateRequest, final Boolean alternative) {
        // Find file into DB
        final ItemReportingDto fileFound = itemFileFinderRepository
            .findByItemTypeAndFileName(stateRequest.getItemType(), stateRequest.getFileName());

        if (Objects.isNull(fileFound)) {
            // If not exists create with state "external_request_received"
            return createItemReportingRecord(stateRequest);
        } else {
            // If exists
            // 1. get next step state:
            State state = State.nextStep(fileFound.getStateName(), alternative);
            // 2. set fileUrl if not exist in database and exist in request
            if (StringUtils.isBlank(fileFound.getFileUrl())
                && StringUtils.isNotBlank(stateRequest.getFileUrl())) {
                fileFound.setFileUrl(stateRequest.getFileUrl());
            }
            // 3. update state in sql-server
            return updateItemRepotingRecord(fileFound, state);
        }
    }

    private ItemReportingDto createItemReportingRecord(StateRequest stateRequest) {
        final LocalDateTime localDate = LocalDateTime.now();
        ItemReportingDto createItemReporting = ItemReportingDto.builder()
            .itemType(stateRequest.getItemType())
            .fileName(stateRequest.getFileName())
            .fileUrl(stateRequest.getFileUrl())
            .stateName(EXTERNAL_REQUEST_RECEIVED_STATE)
            .fileCreationDate(LocalDate.from(localDate))
            .fileUpdateDate(LocalDate.from(localDate))
            .stateUpdateDate(LocalDate.from(localDate))
            .build();
        return itemFileFinderRepository.save(createItemReporting);
    }

    private ItemReportingDto updateItemRepotingRecord(ItemReportingDto fileFound, State state) {
        final LocalDateTime localDate = LocalDateTime.now();

        ItemReportingDto updatedData = ItemReportingDto.builder()
            .id(fileFound.getId())
            .itemType(fileFound.getItemType())
            .fileName(fileFound.getFileName())
            .fileUrl(fileFound.getFileUrl())
            .stateName(state.getName())
            .fileCreationDate(fileFound.getFileCreationDate())
            .fileUpdateDate(LocalDate.from(localDate))
            .stateUpdateDate(LocalDate.from(localDate))
            .build();
        return itemFileFinderRepository.save(updatedData);
    }
}
