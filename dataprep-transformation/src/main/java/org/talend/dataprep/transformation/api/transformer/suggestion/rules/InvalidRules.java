package org.talend.dataprep.transformation.api.transformer.suggestion.rules;

import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.NEGATIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule.POSITIVE;
import static org.talend.dataprep.transformation.api.transformer.suggestion.rules.GenericRule.GenericRuleBuilder.forActions;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.transformation.api.action.metadata.delete.DeleteInvalid;
import org.talend.dataprep.transformation.api.action.metadata.fillinvalid.FillWithBooleanIfInvalid;
import org.talend.dataprep.transformation.api.action.metadata.fillinvalid.FillWithDateIfInvalid;
import org.talend.dataprep.transformation.api.action.metadata.fillinvalid.FillWithNumericIfInvalid;
import org.talend.dataprep.transformation.api.action.metadata.fillinvalid.FillWithStringIfInvalid;
import org.talend.dataprep.transformation.api.transformer.suggestion.SuggestionEngineRule;

@Component
public class InvalidRules extends BasicRules {

    /**
     * Defines the minimum threshold for invalid values corrections. Defaults to 0 (if invalid > 0, returns invalid
     * corrective actions).
     */
    @Value("#{'${invalid.threshold:0}'}")
    private int invalidThreshold;

    private static long getInvalidCount(ColumnMetadata columnMetadata) {
        return Math.max(columnMetadata.getStatistics().getInvalid(), columnMetadata.getQuality().getInvalid());
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides "delete invalid" if no invalid.
     */
    @Bean
    public static SuggestionEngineRule deleteInvalidRule() {
        return forActions(DeleteInvalid.DELETE_INVALID_ACTION_NAME) //
                .then(columnMetadata -> {
                    if (getInvalidCount(columnMetadata) > 0) {
                        return POSITIVE;
                    }
                    return NEGATIVE;
                }) //
                .build();
    }

    /**
     * @return A {@link SuggestionEngineRule rule} that hides "fill invalid" if no invalid.
     */
    @Bean
    public SuggestionEngineRule fillInvalidRule() {
        return forActions(FillWithBooleanIfInvalid.FILL_EMPTY_ACTION_NAME, //
                FillWithDateIfInvalid.FILL_INVALID_ACTION_NAME, //
                FillWithNumericIfInvalid.FILL_INVALID_ACTION_NAME, //
                FillWithStringIfInvalid.FILL_INVALID_ACTION_NAME) //
                        .then(columnMetadata -> {
                            if (getInvalidCount(columnMetadata) > 0) {
                                return POSITIVE;
                            }
                            return NEGATIVE;
                        }) //
                        .build();
    }
}