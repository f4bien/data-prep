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
package org.talend.dataprep.transformation.api.action.metadata.math;

import java.math.RoundingMode;

import org.springframework.stereotype.Component;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;

/**
 * Returns the closest long to the argument, with ties rounding to positive infinity.
 *
 * @see RoundingMode#HALF_UP
 */
@Component(RoundHalfUp.ACTION_BEAN_PREFIX + RoundHalfUp.ACTION_NAME)
public class RoundHalfUp extends AbstractRound {

    /**
     * The action name.
     */
    public static final String ACTION_NAME = "round"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return ACTION_NAME;
    }

    @Override
    protected RoundingMode getRoundingMode() {
        return RoundingMode.HALF_UP;
    }

}
