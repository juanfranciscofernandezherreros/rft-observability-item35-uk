package com.sixgroup.refit.observability.item35.creator.infrastructure.file;

import com.sixgroup.refit.observability.item35.creator.configuration.CsvProperties;
import com.sixgroup.refit.observability.item35.creator.configuration.ReportItemProperties;
import com.sixgroup.refit.observability.item35.creator.domain.enums.Command;
import com.sixgroup.refit.observability.item35.creator.domain.enums.ItemType;
import com.sixgroup.refit.observability.item35.creator.domain.model.ItemCommandDTO;
import com.sixgroup.refit.observability.item35.creator.domain.model.StorageCapacityDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.math.BigDecimal;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WriteFileStorageCapacityTest {

    private static final String TEST_CSV_FILE_PATH = "src/test/resources/work-repository-observability/upload/";
    @InjectMocks
    private WriteFileStorageCapacity writeFileStorageCapacity;
    @Mock
    private CsvProperties csvProperties;
    @Mock
    private ReportItemProperties reportProperties;

    private static ItemCommandDTO getItemCommandDTO() {
        ItemCommandDTO itemCommandDTO = ItemCommandDTO.builder()
            .itemDate("20240915")
            .itemType(ItemType.STORAGE_CAPACITY.getName())
            .command(Command.REQUEST.getDescription())
            .build();
        return itemCommandDTO;
    }

    private static StorageCapacityDto createStorageCapacity(String reportDay, String date, String timeStamp, float capacity,
                                                            float usedCapacity, float availableCapacity, float utilization) {
        StorageCapacityDto storageCapacityDto = new StorageCapacityDto();
        storageCapacityDto.setReportingDate(reportDay);
        storageCapacityDto.setDate(date);
        storageCapacityDto.setTimeStamp(timeStamp);
        storageCapacityDto.setCapacity(BigDecimal.valueOf(capacity));
        storageCapacityDto.setUsedCapacity(BigDecimal.valueOf(usedCapacity));
        storageCapacityDto.setAvailableCapacity(BigDecimal.valueOf(availableCapacity));
        storageCapacityDto.setUtilization(BigDecimal.valueOf(utilization));
        return storageCapacityDto;
    }

    @Test
    void writeFile() throws IOException {

        doReturn(TEST_CSV_FILE_PATH)
            .when(csvProperties).getOutputPath();
        when(reportProperties.getTrCode()).thenReturn("TRRGS");
        when(reportProperties.getRegulationReference()).thenReturn("EMIR");
        when(reportProperties.getIncidentIdHeader()).thenReturn("TR_INCIDENT_ID");

        Path locationPath = FileSystems.getDefault().getPath("src/test/resources/work-repository-observability/upload/");

        if (!Files.exists(locationPath)) {
            Files.createDirectories(locationPath);
        }

        StorageCapacityDto storageCapacityDto_1 = createStorageCapacity("20240915", "2023/09/01",
            "2023-09-01T00:00:00.000Z", 16.703632f, 0.23867607f, 16.464956f,
            0.0142888725f);

        StorageCapacityDto storageCapacityDto_2 = createStorageCapacity("20240915", "2023/09/02",
            "2023-09-02T00:00:00.000Z", 16.700466f, 0.21350479f, 16.486961f,
            0.012784361f);

        List<StorageCapacityDto> storageCapacityDtoList = List.of(storageCapacityDto_1, storageCapacityDto_2);
        File file = writeFileStorageCapacity.writeFile(storageCapacityDtoList, getItemCommandDTO(), "TRRGS_EMIR_PR_FU_ND_ITEM35C_20240915.csv");

        assertNotNull(file);

        String expectedResult = "TR_CODE;REPORTING_DATE;REGULATION_REFERENCE;DATA_CENTER_LOCATION;" +
            "DATABASE_SERVER_OR_PLATFORM;DATE;CAPACITY;USED_CAPACITY;AVAILABLE_CAPACITY;UTILIZATION" +
            ";INCIDENT_RELATED;TR_INCIDENT_ID\n" +
            "TRRGS;20240915;EMIR;Cloudera;Cloudera data warehouse;2023/09/01;16.7036;" +
            "0.2387;16.4650;0.0143;NO;\n" +
            "TRRGS;20240915;EMIR;Cloudera;Cloudera data warehouse;2023/09/02;16.7005" +
            ";0.2135;16.4870;0.0128;NO;";

        InputStream inputStream = new FileInputStream(file);
        String stringFile = readFromInputStream(inputStream);

        assertEquals(stringFile.trim(), expectedResult.trim());

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
}
