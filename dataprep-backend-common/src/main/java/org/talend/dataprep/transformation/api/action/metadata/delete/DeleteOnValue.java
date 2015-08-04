package org.talend.dataprep.transformation.api.action.metadata.delete;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.transformation.api.action.metadata.common.ActionMetadata;
import org.talend.dataprep.transformation.api.action.parameters.Parameter;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static org.talend.dataprep.api.type.Type.NUMERIC;
import static org.talend.dataprep.api.type.Type.STRING;

/**
 * Delete row on a given value.
 */
@Component(DeleteOnValue.ACTION_BEAN_PREFIX + DeleteOnValue.DELETE_ON_VALUE_ACTION_NAME)
public class DeleteOnValue extends AbstractDelete {

    /** The action name. */
    public static final String DELETE_ON_VALUE_ACTION_NAME = "delete_on_value"; //$NON-NLS-1$

    /** Name of the parameter needed. */
    public static final String VALUE_PARAMETER = "value"; //$NON-NLS-1$

    /**
     * @see ActionMetadata#getName()
     */
    @Override
    public String getName() {
        return DELETE_ON_VALUE_ACTION_NAME;
    }

    /**
     * @see ActionMetadata#getCategory()
     */
    @Override
    @Nonnull
    public List<Parameter> getParameters() {
        final List<Parameter> parameters = super.getParameters();
        parameters.add(new Parameter(VALUE_PARAMETER, STRING.getName(), StringUtils.EMPTY));
        return parameters;
    }

    /**
     * @see AbstractDelete#toDelete(Map, String)
     */
    @Override
    public boolean toDelete(Map<String, String> parsedParameters, String value) {
        return value != null && value.trim().equals(parsedParameters.get(VALUE_PARAMETER));
    }

    /**
     * @see ActionMetadata#acceptColumn(ColumnMetadata)
     */
    @Override
    public boolean acceptColumn(ColumnMetadata column) {
        return STRING.equals(Type.get(column.getType())) || NUMERIC.isAssignableFrom(Type.get(column.getType()));
    }

}