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

import java.util.function.Predicate;

import org.apache.tika.metadata.Metadata;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class represents a metadata handler that forces the parser to stop whenever a predicate is satisfied
 */
public class PredicateMetadataHandler extends DefaultHandler {

    /**
     * The predicate that will used to stop the handler
     */
    private final Predicate<Metadata> predicate;

    /**
     * The metadata that will be the argument of the supplied predicate
     */
    private final Metadata metadata;

    /**
     * Constructs a PredicateMetadataHandler from the specified arguments. The parsing will stop as soon as the predicate is verified.
     * @param metadata the metadata
     * @param predicate the predicate condition
     */
    PredicateMetadataHandler(Metadata metadata, Predicate<Metadata> predicate) {
        this.predicate = predicate;
        this.metadata = metadata;
    }

    /**
     * Stops by throwing a runtime exception
     *
     * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or if Namespace processing
     * is not being performed.
     * @param localName The local name (without prefix), or the empty string if Namespace processing is not being
     * performed.
     * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
     * @throws SAXException
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
        if (predicate.test(metadata)) {
            throw new BreakParsingException("Stopping the parsing since the predicate is now true");
        }
    }
}