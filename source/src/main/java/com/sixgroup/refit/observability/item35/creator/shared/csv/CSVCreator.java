package com.sixgroup.refit.observability.item35.creator.shared.csv;

import com.opencsv.CSVWriter;

import java.io.FileWriter;

public final class CSVCreator {

    public static CSVWriter create(final FileWriter fileWriter){
        return new CSVWriter(fileWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, '"', "\n");
    }
}
