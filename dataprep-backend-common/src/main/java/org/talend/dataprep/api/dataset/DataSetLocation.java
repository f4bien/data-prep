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

package org.talend.dataprep.api.dataset;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.talend.dataprep.api.dataset.location.LocalStoreLocation;

import java.io.Serializable;

/**
 * Information about the dataset location.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", defaultImpl = LocalStoreLocation.class)
public interface DataSetLocation extends Serializable {

    /** Return the location name (e.g local, http...). */
    @JsonIgnore
    // ignored not to duplicate with the @JsonTypeInfo defined in the class
    String getLocationType();

}
