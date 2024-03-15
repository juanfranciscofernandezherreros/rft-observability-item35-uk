package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.sixgroup.refit.observability.item.state.application.StateService;
import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import com.sixgroup.refit.observability.item35.creator.domain.service.WriteFileItem35Service;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WriteFileStorageCapacityTest {

    private static final String TEST_CSV_FILE_PATH = "src/test/resources/work-repository-observability/upload/";
    @InjectMocks
    private WriteFileStorageCapacity writeFileStorageCapacity;
    @Mock
    private StateService stateService;
    @Mock
    private CsvProperties csvProperties;

    @Test
    void writeFile() throws IOException {

        doReturn(TEST_CSV_FILE_PATH)
            .when(csvProperties).getOutputPath();

        Path locationPath = FileSystems.getDefault().getPath("src/test/resources/work-repository-observability/upload/");

        if(!Files.exists(locationPath)) {
            Files.createDirectories(locationPath);
        }

        StorageCapacityDto storageCapacityDto_1 = createStorageCapacity("20240915", "2023/09/01",
            "2023-09-01T00:00:00.000Z", 16.703632f,0.23867607f, 16.464956f,
            0.0142888725f);

        StorageCapacityDto storageCapacityDto_2 = createStorageCapacity("20240915", "2023/09/02",
            "2023-09-02T00:00:00.000Z", 16.700466f,0.21350479f, 16.486961f,
            0.012784361f);

        List<StorageCapacityDto> storageCapacityDtoList = List.of(storageCapacityDto_1, storageCapacityDto_2);
        File file = writeFileStorageCapacity.writeFile(storageCapacityDtoList, getItemCommandDTO());

        assertNotNull(file);

        String expectedResult = "\"TR_CODE\",\"REPORTING_DATE\",\"REGULATION_REFERENCE\",\"DATA_CENTER_LOCATION\"," +
            "\"DATABASE_SERVER_OR_PLATFORM\",\"DATE\",\"CAPACITY\",\"USED_CAPACITY\",\"AVAILABLE_CAPACITY\",\"UTILIZATION\"" +
            ",\"INCIDENT_RELATED\",\"TR_INCIDENT_ID\"\n" +
            "\"TRRGS\",\"20240915\",\"EMIR\",\"Cloudera\",\"Cloudera data warehouse\",\"2023/09/01\",\"16.703632\"," +
            "\"0.23867607\",\"16.464956\",\"0.0142888725\",\"NO\",\"\"\n" +
            "\"TRRGS\",\"20240915\",\"EMIR\",\"Cloudera\",\"Cloudera data warehouse\",\"2023/09/02\",\"16.700466\"" +
            ",\"0.21350479\",\"16.486961\",\"0.012784361\",\"NO\",\"\"";

        InputStream inputStream = new FileInputStream(file);
        String stringFile = readFromInputStream(inputStream);

        assertEquals(stringFile.trim(), expectedResult.trim());

    }

    private static ItemCommandDTO getItemCommandDTO() {
        ItemCommandDTO itemCommandDTO = ItemCommandDTO.builder()
            .itemDate("20240915")
            .itemType(ItemType.STORAGE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
        return itemCommandDTO;
    }

    private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
                 = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

    private static StorageCapacityDto createStorageCapacity(String reportDay, String date, String timeStamp, float capacity,
                                                            float usedCapacity, float availableCapacity, float utilization) {
        StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
        storageCapacityDto.setReportingDate(reportDay);
        storageCapacityDto.setDate(date);
        storageCapacityDto.setTimeStamp(timeStamp);
        storageCapacityDto.setCapacity(capacity);
        storageCapacityDto.setUsedCapacity(usedCapacity);
        storageCapacityDto.setAvailableCapacity(availableCapacity);
        storageCapacityDto.setUtilization(utilization);
        return storageCapacityDto;
    }
}
