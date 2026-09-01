package com.sixgroup.refit.observability.item.state.application;

import com.sixgroup.refit.observability.item.state.domain.enums.State;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.model.StateRequest;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StateService {
    private final ItemFileFinderRepository repository;

    public List<ItemReportingDto> nextStep(List<StateRequest> requests) {
        return requests.stream().map(this::nextStep).toList();
    }

    public ItemReportingDto nextStep(StateRequest request) {
        return update(request, false);
    }

    public ItemReportingDto nextStepAlternative(StateRequest request) {
        return update(request, true);
    }

    public ItemReportingDto setError(StateRequest request) {
        ItemReportingDto reporting = findOrCreate(request);
        reporting.setStateName(State.ERROR.getName());
        reporting.setStateUpdateDate(LocalDateTime.now());
        applyRequest(reporting, request);
        return repository.save(reporting);
    }

    private ItemReportingDto update(StateRequest request, boolean alternative) {
        ItemReportingDto reporting = findOrCreate(request);
        State next = State.nextStep(reporting.getStateName(), alternative);
        if (next == null) {
            next = State.INTERNAL_REQUEST_RECEIVED;
        }
        reporting.setStateName(next.getName());
        reporting.setStateUpdateDate(LocalDateTime.now());
        reporting.setFileUpdateDate(LocalDateTime.now());
        applyRequest(reporting, request);
        return repository.save(reporting);
    }

    private ItemReportingDto findOrCreate(StateRequest request) {
        ItemReportingDto reporting = repository.findByItemTypeAndFileName(request.getItemType(), request.getFileName());
        if (reporting != null) {
            return reporting;
        }
        LocalDateTime now = LocalDateTime.now();
        return ItemReportingDto.builder().itemType(request.getItemType()).fileName(request.getFileName())
            .fileCreationDate(now).fileUpdateDate(now).stateName(State.SENT_REQUEST.getName())
            .stateUpdateDate(now).build();
    }

    private void applyRequest(ItemReportingDto reporting, StateRequest request) {
        if (request.getFileUrl() != null) {
            reporting.setFileUrl(request.getFileUrl());
        }
    }
}
