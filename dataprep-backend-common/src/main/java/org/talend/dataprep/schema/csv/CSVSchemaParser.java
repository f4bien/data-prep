//  ============================================================================
//
//  Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
//  This source code is available under agreement available at
//  https://github.com/Talend/data-prep/blob/master/LICENSE
//
//  You should have received a copy of the agreement
//  along with this program; if not, write to Talend SA
//  9 rue Pages 92150 Suresnes, France
//
//  ============================================================================

package org.talend.dataprep.schema.csv;

import static org.talend.dataprep.api.dataset.ColumnMetadata.Builder.column;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.exception.error.DataSetErrorCodes;
import org.talend.dataprep.format.CSVFormatFamily;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.SchemaParserResult;

import au.com.bytecode.opencsv.CSVReader;

@Service("parser#csv")
public class CSVSchemaParser implements SchemaParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(CSVSchemaParser.class);

    private static final String META_KEY = "key";

    @Autowired
    protected CSVFormatUtils csvFormatUtils;

    /**
     *
     * @param request container with information needed to parse the raw data.
     * @return
     */
    @Override
    public SchemaParserResult parse(Request request) {
        List<SchemaParserResult.SheetContent> sheetContents = new ArrayList<>();
        sheetContents.add(new SchemaParserResult.SheetContent(META_KEY, new ArrayList<>()));
        try {
            final DataSetMetadata metadata = request.getMetadata();
            final Map<String, String> parameters = metadata.getContent().getParameters();
            final char separator = parameters.get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            List<String> header = csvFormatUtils.retrieveHeader(parameters);

            if (header == null || header.isEmpty()) {
                try (CSVReader reader = new CSVReader(new InputStreamReader(request.getContent(), metadata.getEncoding()), separator)) {
                    String[] columns = reader.readNext();
                    if (columns == null) { // Empty content?
                        return SchemaParserResult.Builder.parserResult() //
                                .sheetContents(sheetContents).build();
                    } else {
                        throw new TDPException(DataSetErrorCodes.UNABLE_TO_READ_DATASET_CONTENT);
                    }
                }
            }
            LOGGER.debug("Columns found: {}", header);
            // By default, consider all columns as Strings (to be refined by deeper analysis).
            LOGGER.debug("Setting default type for columns...");
            int i = 0;
            for (String column : header) {
                sheetContents.stream().filter(sheetContent -> META_KEY.equals(sheetContent.getName())).findFirst() //
                        .get().getColumnMetadatas() //
                        .add(column().id(i++).name(column).type(Type.STRING).build());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        }
        return SchemaParserResult.Builder.parserResult() //
                .sheetContents(sheetContents) //
                .draft(false).build();
    }

    /**
     * @see FormatGuesser#guess(SchemaParser.Request, String)
     */
    public FormatGuesser.Result guess(SchemaParser.Request request, String encoding) {
        if (request == null || request.getContent() == null) {
            throw new IllegalArgumentException("Content cannot be null.");
        }

        // if the dataset metadata is updated, let's use the separator set as the one to use
        Optional<Character> specifiedSeparator = Optional.empty();
        final String temp = request.getMetadata().getContent().getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER);
        if (temp != null && StringUtils.isNotEmpty(temp)) {
            specifiedSeparator = Optional.of(temp.charAt(0));
        }

        Separator sep = guessSeparator(request.getContent(), encoding, specifiedSeparator);


        Map<String, String> parameters = csvFormatUtils.compileSeparatorProperties(sep);
        return new FormatGuesser.Result(csvFormatGuess, encoding, parameters);
    }

    /**
     * Try to guess the separator used in the CSV.
     *
     * @param is the input stream to read the CSV from.
     * @param encoding the encoding to use for the reading.
     * @param forcedSeparator if the separator is forced.
     * @return the guessed CSV separator or null if none found.
     */
    private Separator guessSeparator(InputStream is, String encoding, Optional<Character> forcedSeparator) {
        try {
            Reader reader = encoding != null ? new InputStreamReader(is, encoding) : new InputStreamReader(is);
            try (LineNumberReader lineNumberReader = new LineNumberReader(reader)) {
                Map<Character, Separator> separatorMap = new HashMap<>();
                long totalChars = 0;
                int lineCount = 0;
                boolean inQuote = false;
                String s;
                List<String> sampleLines = new ArrayList<>();

                // Detectors used to check the encoding.
                List<WrongEncodingDetector> detectors = Arrays.asList( //
                        new WrongEncodingDetector(65533), //
                        new WrongEncodingDetector(0) //
                );

                while (totalChars < SIZE_LIMIT && lineCount < LINE_LIMIT && (s = lineNumberReader.readLine()) != null) {
                    totalChars += s.length() + 1; // count the new line character
                    if (s.isEmpty()) {
                        continue;
                    }
                    if (!inQuote) {
                        lineCount++;
                        if (lineCount < SMALL_SAMPLE_LIMIT) {
                            sampleLines.add(s);
                        }
                    }
                    for (int i = 0; i < s.length(); i++) {

                        char c = s.charAt(i);

                        // check the encoding
                        try {
                            checkEncoding(c, totalChars, detectors);
                        } catch (IOException e) {
                            LOGGER.debug(encoding + " is assumed wrong" + e);
                            return null;
                        }

                        if ('"' == c) {
                            inQuote = !inQuote;
                        }

                        if (!inQuote && filterSeparator(forcedSeparator, c)) {
                            processCharAsSeparatorCandidate(c, separatorMap, lineCount);
                        }

                    }
                }
                return chooseSeparator(new ArrayList<>(separatorMap.values()), lineCount, sampleLines, forcedSeparator);
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNABLE_TO_READ_CONTENT, e);
        } catch (Exception e) {
            LOGGER.debug("Unable to read content from content using encoding '{}'.", encoding, e);
            return null;
        }
    }

    /**
     * Return true if the given char match the optional forced separator.
     *
     * @param forcedSeparator the optional forced separator.
     * @param c the char to analyze.
     * @return true if the given char match the optional forced separator.
     */
    private boolean filterSeparator(Optional<Character> forcedSeparator, char c) {
        // if there's a forced separator and it matches the given char
        return !forcedSeparator.isPresent() || forcedSeparator.get() == c;
    }

    /**
     * Check the encoding with every WrongEncodingDetector.
     *
     * @param c the current char to check.
     * @param totalChars the total number of chars so far.
     * @throws IOException if the encoding is assumed wrong.
     */
    private void checkEncoding(char c, long totalChars, List<WrongEncodingDetector> detectors) throws IOException {
        for (WrongEncodingDetector detector : detectors) {
            detector.checkChar(c, totalChars);
        }
    }

    /**
     * Detects if the given char is a separator candidate. If true, the separator is added within the separators map.
     *
     * @param candidate the candidate to analyse.
     * @param separatorMap the map of current candidates.
     * @param lineNumber the current line number.
     */
    protected void processCharAsSeparatorCandidate(char candidate, Map<Character, Separator> separatorMap, int lineNumber) {
        if (!Character.isLetterOrDigit(candidate)) {
            Separator separator = separatorMap.get(candidate);
            if (separator == null) {
                separator = new Separator(candidate);
                separatorMap.put(candidate, separator);
            }
            separator.incrementCount(lineNumber);
        }
    }

    /**
     * Choose the best separator out of the given ones.
     *
     * @param separators the list of separators found in the CSV (may be empty but not null.
     * @param lineCount number of lines in the CSV.
     * @param forcedSeparator
     * @return the separator to use to read the CSV or null if none found.
     */
    private Separator chooseSeparator(List<Separator> separators, int lineCount, List<String> sampleLines, Optional<Character> forcedSeparator) {

        // filter separators
        final List<Separator> filteredSeparators = separators.stream() //
                .filter(sep ->
                        (forcedSeparator.isPresent() && forcedSeparator.get()== sep.getSeparator()) || validSeparators.contains(sep.getSeparator())
                ) // filter out invalid separators
                .collect(Collectors.toList());

        // easy case where there's no choice
        if (filteredSeparators.isEmpty()) {
            if (lineCount > 0) {
                // There are some lines processed, but no separator (a one-column content?), so pick a default
                // separator.
                Separator result = new Separator(',');
                filteredSeparators.add(result);
            } else {
                return null;
            }
        }

        // compute each separator score
        SeparatorAnalyzer separatorAnalyzer = new SeparatorAnalyzer(lineCount, sampleLines);
        filteredSeparators.forEach(separatorAnalyzer::accept); // analyse separators and set header info and score

        // sort separator and return the first
        return filteredSeparators.stream() //
                .sorted(separatorAnalyzer::compare).findFirst() //
                .get();
    }

}