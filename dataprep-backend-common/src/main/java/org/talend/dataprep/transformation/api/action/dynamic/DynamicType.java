package org.talend.dataprep.transformation.api.action.dynamic;

import java.util.Arrays;

import org.springframework.web.context.WebApplicationContext;
import org.talend.dataprep.transformation.api.action.dynamic.cluster.ClusterParameters;

public enum DynamicType {
    TEXT_CLUSTER("textclustering", ClusterParameters.class);

    private String action;
    private Class<? extends DynamicParameters> generatorType;

    private DynamicType(final String action, Class<? extends DynamicParameters> generatorType) {
        this.action = action;
        this.generatorType = generatorType;
    }

    public static DynamicType fromAction(final String action) {
        return Arrays.stream(values())
                .filter(type -> type.getAction().equals(action))
                .findFirst()
                .orElse(null);
    }

    public DynamicParameters getGenerator(final WebApplicationContext context) {
        return context.getBean(generatorType);
    }

    public String getAction() {
        return action;
    }
}