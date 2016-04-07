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

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Represents a accurate format which is composed in : which is a two-part identifier for file formats (xls, csv, html, ...) and format contents of type that may be supported or not by Data prep. This class is immutable.
 */
public class Format {

    /**
     * The internet mediaType e.g. text/plain
     */
    private final FormatFamily formatFamily;

    /**
     * A charset
     */
    private final Charset charset;

    /**
     * Constructs a format with given arguments
     * @param format
     * @param charset
     */
    public Format(FormatFamily format, Charset charset) {
        this.formatFamily = format;
        this.charset = charset;
    }

    /**
     * Constructs a format with a media type and a string corresponding to a charset.
     *
     * @param format the family of the format (xls, word, ...)
     * @param encoding  the string version of an encoding that is used to retrieve its charset
     * @throws IllegalCharsetNameException If the given charset name is illegal
     * @throws IllegalArgumentException    If the given <tt>charsetName</tt> is null
     * @throws UnsupportedCharsetException If no support for the named charset is available in this instance of the Java
     *                                     virtual machine
     */
    public Format(FormatFamily format, String encoding) {
        this.charset = Charset.forName(encoding);
        this.formatFamily = format;
    }

    /**
     *
     * @return the format family
     */
    public FormatFamily getFormatFamily() {
        return formatFamily;
    }

    /**
     *
     * @return the charset
     */
    public Charset getCharset() {
        return charset;
    }
}
