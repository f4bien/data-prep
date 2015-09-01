package org.talend.dataprep.transformation.aggregation.operation;

import org.junit.Assert;
import org.junit.Test;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;
import org.talend.dataprep.transformation.aggregation.api.Operator;

/**
 * Unit test for Max aggregator.
 * 
 * @see Max
 */
public class MaxTest extends OperationBaseTest {

    /**
     * Constructor.
     */
    public MaxTest() {
        super(Operator.MAX);
    }

    @Test
    public void shouldComputeMin() {
        // given when
        AggregationResult result = new AggregationResult(Operator.MAX);
        aggregator.accept(getRow("toto", "5123.4"), result);
        aggregator.accept(getRow("tata", "-50.2"), result);
        aggregator.accept(getRow("toto", "786.884"), result);
        aggregator.accept(getRow("tata", "-0.2"), result);
        aggregator.accept(getRow("toto", "41843.453"), result); // <-- max here for toto
        aggregator.accept(getRow("toto", "0"), result);
        aggregator.accept(getRow("tata", "20"), result);
        aggregator.accept(getRow("toto", "-1"), result);
        aggregator.accept(getRow("toto", "8.87"), result);
        aggregator.accept(getRow("tata", "875"), result); // <-- max here for tata
        aggregator.accept(getRow("toto", "-0.01"), result);
        aggregator.accept(getRow("tutu", "sdfs"), result); // <-- should not be part of the result

        // then
        Assert.assertEquals(result.get("toto").getValue(), 41843.453, 0);
        Assert.assertEquals(result.get("tata").getValue(), 875, 0);
        Assert.assertNull(result.get("sdfs"));
    }

}