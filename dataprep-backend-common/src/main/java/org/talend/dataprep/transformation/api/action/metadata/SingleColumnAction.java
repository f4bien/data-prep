// ============================================================================
//
// Copyright (C) 2006-2015 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.dataprep.transformation.api.action.metadata;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.type.Type;

/**
 * created by sgandon on 25 mars 2015 Detailled comment
 *
 */
public abstract class SingleColumnAction implements ActionMetadata {

    public static final String COLUMN_NAME_PARAMETER_NAME = "column_name"; //$NON-NLS-1$

    public static final Parameter COLUMN_NAME_PARAMETER = new Parameter(COLUMN_NAME_PARAMETER_NAME, Type.STRING.getName(),
            StringUtils.EMPTY);

    @Override
    public Parameter[] getParameters() {
        return new Parameter[] { COLUMN_NAME_PARAMETER };
    }

}