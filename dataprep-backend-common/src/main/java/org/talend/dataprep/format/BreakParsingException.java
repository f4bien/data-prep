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

package org.talend.dataprep.format;

/**
 * This exception is used to break parsing during format detection.
 */
public class BreakParsingException extends RuntimeException {

    /**
     * @see RuntimeException
     */
    public BreakParsingException() {
    }

    /**
     *
     * @see RuntimeException
     * @param message  the detail message
     */
    public BreakParsingException(String message) {
        super(message);
    }
}