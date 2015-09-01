package org.talend.dataprep.transformation.aggregation.operation;

import java.util.function.BiConsumer;

import org.apache.commons.lang.StringUtils;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.transformation.aggregation.api.AggregationResult;

/**
 * Sum aggregator.
 */
public class Sum extends AbstractAggregator implements Aggregator {

    /**
     * Sum aggregator constructor. Package visible to ensure the use of the factory.
     *
     * @param groupBy group by key.
     * @param columnId column id to aggregate.
     */
    Sum(String groupBy, String columnId) {
        super(groupBy, columnId);
    }

    /**
     * @see BiConsumer#accept(Object, Object)
     */
    @Override
    public void accept(DataSetRow row, AggregationResult result) {
        final String sumKey = row.get(groupBy);

        // skip value not found
        if (StringUtils.isEmpty(sumKey)) {
            return;
        }

        // get the value
        double toAdd;
        try {
            toAdd = Double.parseDouble(row.get(columnId));
        } catch (NumberFormatException e) {
            // skip non number
            return;
        }

        // init the group by in the result
        if (!result.contains(sumKey)) {
            result.put(sumKey, new NumberContext(0d));
        }

        NumberContext context = (NumberContext) result.get(sumKey);
        context.setValue(context.getValue() + toAdd);

    }
}