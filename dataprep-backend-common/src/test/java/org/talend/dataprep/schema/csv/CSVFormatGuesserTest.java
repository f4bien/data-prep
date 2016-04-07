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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tools.ant.filters.StringInputStream;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.talend.dataprep.format.CSVFormatFamily;
import org.talend.dataprep.schema.AbstractSchemaTestUtils;
import org.talend.dataprep.schema.FormatGuesser;
import org.talend.dataprep.format.UnsupportedFormatFamily;

/**
 * Unit test for the LineBasedFormatGuesser.
 * 
 * @see CSVFormatGuesser
 */
public class CSVFormatGuesserTest extends AbstractSchemaTestUtils {

    /** The format guesser to test. */
    @Autowired
    CSVFormatGuesser guesser;

    @Autowired
    private CSVFormatUtils csvFormatUtils;

    @Test
    public void format_guesser_accept() throws Exception {
        for (Charset charset : Charset.availableCharsets().values()) {
            assertTrue(guesser.accept(charset.name()));
        }
    }

    /**
     * Text file
     */
    @Test
    public void should_not_guess() throws IOException {
        FormatGuesser.Result actual = guesser.guess(getRequest(new ByteArrayInputStream(new byte[0]), "#1"), "UTF-8");
        Assert.assertNotNull(actual);
        assertTrue(actual.getFormat() instanceof UnsupportedFormatFamily);
    }

    @Test(expected = IllegalArgumentException.class)
    public void read_null_csv_file() throws Exception {
        guesser.guess(null, "UTF-8").getFormat();
    }

    /**
     * Standard csv file.
     */
    @Test
    public void should_guess_CSV() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("standard.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#2"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
        }
    }

    /**
     * csv file with 2 possible separators : ';' or '/', ';' should be selected
     */
    @Test
    public void should_guess_best_separator() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("mixed_separators.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#3"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(separator, ';');
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-181
     */
    @Test
    public void should_guess_best_separator_out_of_two() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-181.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#4"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-258
     */
    @Test
    public void should_guess_separator_with_ISO_8859_1_encoded_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("iso-8859-1.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#5"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(separator, ';');
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-863
     */
    @Test
    public void should_guess_valid_separator_when_most_likely_separator_is_not_valid() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-863.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#6"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-832
     */
    @Test
    public void should_guess_valid_separator_from_access_log_file() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-832.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#7"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(' ', separator);
        }
    }

    @Test
    public void should_not_detect_char_or_digit_separator_candidate() {
        Map<Character, Separator> separatorMap = new HashMap<>();
        char[] cases = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        for (char candidate : cases) {
            guesser.processCharAsSeparatorCandidate(candidate, separatorMap, 0);
        }
        assertTrue(separatorMap.isEmpty());
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1060
     */
    @Test
    public void TDP_1060() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-1060.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#8"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1060
     */
    @Test
    public void consistency_test() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("consistency_example.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#9"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void TDP_1240_should_detect_separator_if_wrong_characters_do_not_exceed_the_threshold() throws IOException {

        char[] chars = new char[50];
        for (int i = 0; i < 48; i++) {
            chars[i] = 0;
        }
        chars[48] = ';';
        chars[49] = '\n';
        try (InputStream inputStream = new StringInputStream(new String(chars))) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#10"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', separator);
        }

        chars = new char[50];
        for (int i = 0; i < 48; i++) {
            chars[i] = 65533;
        }
        chars[48] = ';';
        chars[49] = '\n';
        try (InputStream inputStream = new StringInputStream(new String(chars))) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#11"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(';', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void TDP_1240_should_detect_separator_if_wrong_characters_exceed_threshold() throws IOException {

        // null character
        char[] chars = new char[51];
        for (int i = 0; i < 49; i++) {
            chars[i] = 0;
        }
        chars[49] = ';';
        chars[50] = '\n';
        try (InputStream inputStream = new StringInputStream(new String(chars))) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#12"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof UnsupportedFormatFamily);
        }

        // wrong character
        chars = new char[51];
        for (int i = 0; i < 49; i++) {
            chars[i] = 65533;
        }
        chars[49] = ';';
        chars[50] = '\n';
        try (InputStream inputStream = new StringInputStream(new String(chars))) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#13"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof UnsupportedFormatFamily);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1240 Randomly separators were not detected for files with
     * wrong characters.
     *
     */
    @Test
    public void should_detect_comma_separator_when_no_separator_detected() throws IOException {

        char[] chars = new char[5];
        for (int i = 0; i < 5; i++) {
            chars[i] = 'A';
        }
        try (InputStream inputStream = new StringInputStream(new String(chars))) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#13"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            assertEquals(',', separator);
        }
    }

    /**
     * Have a look at https://jira.talendforge.org/browse/TDP-1259 We should import whole header of csv files even if
     * some fields are duplicated.
     *
     */
    @Test
    public void TDP_1259() throws IOException {
        try (InputStream inputStream = this.getClass().getResourceAsStream("tdp-1259.csv")) {
            FormatGuesser.Result actual = guesser.guess(getRequest(inputStream, "#14"), "UTF-8");

            Assert.assertNotNull(actual);
            assertTrue(actual.getFormat() instanceof CSVFormatFamily);
            char separator = actual.getParameters().get(CSVFormatFamily.SEPARATOR_PARAMETER).charAt(0);
            List<String> header = csvFormatUtils.retrieveHeader(actual.getParameters());
            assertEquals(';', separator);
            List<String> expected = Arrays.asList("id", "first_name", "last_name", "email", "job_title", "company", "city", "state",
                    "country", "date", "campaign_id", "lead_score", "registration", "city", "birth", "nbCommands", "id",
                    "first_name", "last_name", "email", "job_title", "company", "city", "state", "country", "date",
                    "campaign_id", "lead_score", "registration", "city", "birth", "nbCommands");
            assertEquals(expected, header);
        }
    }

}