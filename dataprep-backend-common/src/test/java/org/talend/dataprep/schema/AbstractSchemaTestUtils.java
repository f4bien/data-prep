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

package org.talend.dataprep.schema;

import java.io.InputStream;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadataBuilder;

/**
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = AbstractSchemaTestUtils.class)
@Configuration
@ComponentScan(basePackages = "org.talend.dataprep")
@EnableAutoConfiguration
public abstract class AbstractSchemaTestUtils {

    @Autowired
    protected IoTestUtils ioTestUtils;

    @Autowired
    protected DataSetMetadataBuilder metadataBuilder;

    /**
     * Return the SchemaParser.Request for the given parameters.
     *
     * @param content the dataset con.ent.
     * @param dataSetId the dataset id.
     * @return the SchemaParser.Request for the given parameters.
     */
    protected SchemaParser.Request getRequest(InputStream content, String dataSetId) {
        DataSetMetadata dataSetMetadata = metadataBuilder.metadata().id(dataSetId).build();
        return new SchemaParser.Request(content, dataSetMetadata);
    }

}
