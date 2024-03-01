package com.sixgroup.refit.observability.item35.creator.infrastructure.repository.sqlserver;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sixgroup.refit.observability.item35.creator.domain.model.ItemFileFinderRequest;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemReportingDto;
import com.sixgroup.refit.observability.item35.creator.domain.repository.ItemFileFinderRepository;
import com.sixgroup.refit.observability.item35.creator.infrastructure.entity.sqlserver.ItemReportingEntity;
import com.sixgroup.refit.observability.item35.creator.infrastructure.mappper.FileFinderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class SqlServerItemFileFinderRepository implements ItemFileFinderRepository {

    private final MSSQLItemFileRepositoryAdapter mssqlItemFileRepositoryAdapter;
    private final FileFinderMapper fileFinderMapper;

    @Override
    public List<ItemReportingDto> findAllByItemTypeAndFileName(final List<ItemFileFinderRequest> request) {
        List<String> itemTypeList = request.stream().map(ItemFileFinderRequest::getItemType).toList();
        List<String> fileNameList = request.stream().map(ItemFileFinderRequest::getFileName).toList();
        return mssqlItemFileRepositoryAdapter.findByItemTypeInAndFileNameIn(itemTypeList, fileNameList)
            .stream().map(fileFinderMapper::entityMssqlToDomain)
            .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ItemReportingDto findByItemTypeAndFileName(final String itemType, final String fileName) {
        return fileFinderMapper.entityMssqlToDomain(mssqlItemFileRepositoryAdapter
            .findByItemTypeAndFileName(itemType, fileName));
    }

    @Override
    public ItemReportingDto save(ItemReportingDto request) {
        ItemReportingEntity itemReportingEntity = fileFinderMapper.itemReportingDtoToEntity(request);
        return fileFinderMapper.entityMssqlToDomain(mssqlItemFileRepositoryAdapter
            .save(itemReportingEntity));
    }


}
