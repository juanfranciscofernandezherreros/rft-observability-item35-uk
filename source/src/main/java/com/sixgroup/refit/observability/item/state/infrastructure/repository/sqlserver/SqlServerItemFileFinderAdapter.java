package com.sixgroup.refit.observability.item.state.infrastructure.repository.sqlserver;

import com.sixgroup.refit.observability.item.state.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item.state.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item.state.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item.state.infrastructure.entity.ItemReportingEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SqlServerItemFileFinderAdapter implements ItemFileFinderRepository {
    private final SqlServerItemReportingRepository repository;

    @Override
    public List<ItemReportingDto> findAllByItemTypeAndFileName(List<ItemFileFinderRequest> requests) {
        return requests.stream()
            .map(request -> findByItemTypeAndFileName(request.getItemType(), request.getFileName()))
            .filter(java.util.Objects::nonNull)
            .toList();
    }

    @Override
    public ItemReportingDto findByItemTypeAndFileName(String itemType, String fileName) {
        return toDto(repository.findFirstByItemTypeAndFileName(itemType, fileName));
    }

    @Override
    public ItemReportingDto save(ItemReportingDto itemReporting) {
        return toDto(repository.save(toEntity(itemReporting)));
    }

    private ItemReportingDto toDto(ItemReportingEntity entity) {
        if (entity == null) {
            return null;
        }
        return ItemReportingDto.builder().id(entity.getId()).itemType(entity.getItemType())
            .fileName(entity.getFileName()).fileUrl(entity.getFileUrl())
            .fileCreationDate(entity.getFileCreationDate()).fileUpdateDate(entity.getFileUpdateDate())
            .stateName(entity.getStateName()).stateUpdateDate(entity.getStateUpdateDate()).build();
    }

    private ItemReportingEntity toEntity(ItemReportingDto dto) {
        return ItemReportingEntity.builder().id(dto.getId()).itemType(dto.getItemType())
            .fileName(dto.getFileName()).fileUrl(dto.getFileUrl())
            .fileCreationDate(dto.getFileCreationDate()).fileUpdateDate(dto.getFileUpdateDate())
            .stateName(dto.getStateName()).stateUpdateDate(dto.getStateUpdateDate()).build();
    }
}
