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

import org.springframework.stereotype.Component;
import org.talend.dataprep.schema.DraftValidator;
import org.talend.dataprep.schema.SchemaParser;
import org.talend.dataprep.schema.Serializer;

/**
 * A special implementation of {@link FormatFamily} to serve as fallback and indicates the provided content is not
 * supported in data prep.
 */
@Component(UnsupportedFormatFamily.BEAN_ID)
public class UnsupportedFormatFamily implements FormatFamily {

    protected static final String BEAN_ID = "formatGuess#any";

    @Override
    public String getMediaType() {
        return "*/*"; //$NON-NLS-1$
    }

    @Override
    public Serializer getSerializer() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SchemaParser getSchemaParser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DraftValidator getDraftValidator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBeanId() {
        return BEAN_ID;
    }
}
