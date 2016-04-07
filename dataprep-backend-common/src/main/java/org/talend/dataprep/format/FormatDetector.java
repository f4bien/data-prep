// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.lang.StringUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * This class is used to detect format of specified dataset. This class is based upon TIKA.
 */
@Component
public class FormatDetector {

    private static final Logger LOGGER = LoggerFactory.getLogger(FormatDetector.class);

    @Autowired
    private XlsFormatFamily xlsFormatFamily;

    @Autowired
    private HtmlFormatFamily htmlFormatFamily;

    /** The csv format guesser. */
    @Autowired
    private CSVFormatFamily csvFormatFamily;

    /** The fallback guess if the input is not CSV compliant. */
    @Autowired
    private UnsupportedFormatFamily unsupportedFormatFamily;

    /**
     * The TIKA AutodetectParser used to discover format
     */
    private final AutoDetectParser autoDetectParser = new AutoDetectParser();

    /**
     * String flag used to detect if the content type of the a format-family metadata has been found
     */
    private static final String CONTENT_TYPE = "Content-Type";

    /**
     * The default charset, for instance it is used for xls format
     */
    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /**
     * The XLS MIME type returned by TIKA for XLS format
     */
    private static final String XLS_MEDIA_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    /**
     * The HTML MIME type returned by TIKA for HTML/Salesforce format
     */
    private static final String HTML_MEDIA_TYPE = "text/html";

    /**
     * The CSV MIME type returned by TIKA for CSV format
     */
    private static final String CSV_MEDIA_TYPE = "text/plain";

    /**
     * Detects and returns the format of the specified input stream. This method closes the stream in any case except if
     * an exception is thrown.
     * 
     * @param inputStream the specified input stream
     * @return the format of the specified input stream
     * @throws IOException
     */

    public Format detectFormat(InputStream inputStream) throws Exception {
        final Format result;
        final Metadata metadata = new Metadata();
        final PredicateMetadataHandler fastHandler = new PredicateMetadataHandler(metadata,
                m -> StringUtils.isNotEmpty(m.get(CONTENT_TYPE)));
        final ParseContext pcontext = new ParseContext();

        try {
            autoDetectParser.parse(inputStream, fastHandler, metadata, pcontext);
        } catch (BreakParsingException e) {
            LOGGER.debug("The metadata has been found {}", e.getMessage());
        } finally {
            inputStream.close();
        }
        result = parseMediaType(metadata.get(CONTENT_TYPE));
        return result;
    }

    /**
     * Parses a media type and returns its format which corresponds to a couple of format family and a charset.
     * 
     * @param mediaType the specified media type
     * @return returns the format corresponding to the specified media type
     */
    private Format parseMediaType(final String mediaType) {

        final Format result;
        final String filteredMediaType;
        final Charset charset;

        final int charsetSeparator = mediaType.indexOf(";");
        final int charsetStart = mediaType.indexOf("=");
        if (charsetSeparator > 0 && charsetStart > 0) {
            filteredMediaType = mediaType.substring(0, charsetSeparator);
            charset = Charset.forName(mediaType.substring(charsetStart + 1));
        } else {
            filteredMediaType = mediaType;
            charset = DEFAULT_CHARSET;
        }
        result = get(filteredMediaType, charset);

        return result;
    }

    /**
     * Returns a format corresponding to the specified arguments.
     * 
     * @param mediaType the media type as returned by the detector
     * @param charset the specified charset
     * @return
     */
    private Format get(String mediaType, Charset charset) {
        final Format result;
        switch (mediaType) {
        case XLS_MEDIA_TYPE:

            result = new Format(xlsFormatFamily, charset.name());
            break;
        case HTML_MEDIA_TYPE:
            result = new Format(htmlFormatFamily, charset.name());
            break;
        case CSV_MEDIA_TYPE:
            result = new Format(csvFormatFamily, charset.name());
            break;
        default:
            result = new Format(unsupportedFormatFamily, charset.name());
        }
        return result;
    }
}
