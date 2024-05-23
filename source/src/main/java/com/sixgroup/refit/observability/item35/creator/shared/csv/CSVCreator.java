package com.sixgroup.refit.observability.item35.creator.shared.csv;

import com.opencsv.CSVWriter;

import java.io.FileWriter;

public final class CSVCreator {

    public static final char SEPARATOR = ';';
    public static final char NO_QUOTE_CHARACTER = '\u0000';
    public static final char ESCAPECHAR = '\'';
    public static final String LINE_BREAK = "\n";
    private CSVCreator() {
    }

    public static CSVWriter create(final FileWriter fileWriter) {
        return new CSVWriter(fileWriter, SEPARATOR, NO_QUOTE_CHARACTER, ESCAPECHAR, LINE_BREAK);
    }
}
