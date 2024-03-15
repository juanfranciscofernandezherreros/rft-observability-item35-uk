package com.sixgroup.refit.observability.item35.creator.application.mock;

import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;

public class ItemCommandMock {

    public static ItemCommandDTO builderItemCommand() {

        return ItemCommandDTO.builder()
            .itemDate("20241201")
            .itemType(ItemType.SUBMISSION_VOLUMES.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }

    public static ItemCommandDTO builderItemCommandComputeCapacity() {

        return ItemCommandDTO.builder()
            .itemDate("20241201")
            .itemType(ItemType.COMPUTE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
    }
}
