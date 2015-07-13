package org.talend.dataprep.transformation.api.transformer.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

import org.talend.dataprep.api.dataset.DataSet;
import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;
import org.talend.dataprep.transformation.api.action.context.TransformationContext;
import org.talend.dataprep.transformation.api.transformer.TransformerWriter;

/**
 * Full configuration for a transformation.
 */
public class TransformerConfiguration {

    /** The dataset input to transform. */
    private final DataSet input;

    /** Where to write the transformed content. */
    private final TransformerWriter output;

    /** Indexes of rows (used in diff). */
    private final List<Integer> indexes;

    /** True if in preview mode. */
    private final boolean preview;

    /** The list of actions to perform on columns. */
    private final List<BiConsumer<RowMetadata, TransformationContext>> columnActions;

    /** The list of actions to perform on records. */
    private final List<BiConsumer<DataSetRow, TransformationContext>> recordActions;

    /** List of transformation context, one per action. */
    private List<TransformationContext> transformationContexts;

    /**
     * Constructor for the transformer configuration.
     * 
     * @param input the json parser.
     * @param output the writer plugged to the output stream to write into.
     * @param indexes The records indexes to transform.
     * @param preview preview mode.
     * @param columnActions the actions to perform on columns.
     * @param recordActions the actions to perform on records.
     */
    private TransformerConfiguration(final DataSet input, //
            final TransformerWriter output, //
            final List<Integer> indexes, //
            final boolean preview, //
            final List<BiConsumer<RowMetadata, TransformationContext>> columnActions, //
            final List<BiConsumer<DataSetRow, TransformationContext>> recordActions) {
        this.input = input;
        this.output = output;
        this.indexes = indexes;
        this.preview = preview;
        this.columnActions = columnActions == null ? Collections.emptyList() : columnActions;
        this.recordActions = recordActions == null ? Collections.emptyList() : recordActions;
        this.transformationContexts = new ArrayList<>();

        int actionSize = this.recordActions.size();
        if (this.columnActions.size() > recordActions.size()) {
            actionSize = this.columnActions.size();
        }

        for (int i = 0; i < actionSize; i++) {
            this.transformationContexts.add(new TransformationContext());
        }

    }

    /**
     * @return the dataset to transform as json parser.
     */
    public DataSet getInput() {
        return input;
    }

    /**
     * @return the writer where to write the transform dataset.
     */
    public TransformerWriter getOutput() {
        return output;
    }

    /**
     * @return the row indexes.
     */
    public List<Integer> getIndexes() {
        return indexes;
    }

    /**
     * @return true if in preview mode.
     */
    public boolean isPreview() {
        return preview;
    }

    /**
     * @return the actions to perform on columns.
     */
    public List<BiConsumer<RowMetadata, TransformationContext>> getColumnActions() {
        return columnActions;
    }

    /**
     * @return the actions to perform on records.
     */
    public List<BiConsumer<DataSetRow, TransformationContext>> getRecordActions() {
        return recordActions;
    }

    /**
     * @return a TransformerConfiguration builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param index the param index.
     * @return the transformation context that match the given index.
     */
    public TransformationContext getTransformationContext(int index) {
        return transformationContexts.get(index);
    }

    /**
     * Builder pattern used to simplify code writing.
     */
    public static class Builder {

        /** The dataset input to transform. */
        private DataSet input;

        /** Where to write the transformed content. */
        private TransformerWriter output;

        /** Indexes of rows (used in diff). */
        private List<Integer> indexes;

        /** True if in preview mode. */
        private boolean preview;

        /** The list of actions to perform on columns. */
        private List<BiConsumer<RowMetadata, TransformationContext>> columnActions = new ArrayList<>(2);

        /** The list of actions to perform on records. */
        private List<BiConsumer<DataSetRow, TransformationContext>> recordActions = new ArrayList<>(2);

        /**
         * @param input the dataset input to set.
         * @return the builder to chain calls.
         */
        public Builder input(final DataSet input) {
            this.input = input;
            return this;
        }

        /**
         * @param output where to write the transformed dataset.
         * @return the builder to chain calls.
         */
        public Builder output(final TransformerWriter output) {
            this.output = output;
            return this;
        }

        /**
         * @param indexes the indexes to set.
         * @return the builder to chain calls.
         */
        public Builder indexes(final List<Integer> indexes) {
            if (this.indexes == null) {
                this.indexes = new ArrayList<>(indexes.size());
            }
            this.indexes.addAll(indexes);
            return this;
        }

        /**
         * @param preview the preview flag to set.
         * @return the builder to chain calls.
         */
        public Builder preview(final boolean preview) {
            this.preview = preview;
            return this;
        }

        /**
         * @param columnActions the actions to perform on columns.
         * @return the builder to chain calls.
         */
        public Builder columnActions(final BiConsumer<RowMetadata, TransformationContext> columnActions) {
            this.columnActions.add(columnActions);
            return this;
        }

        /**
         * @param recordActions the actions to perform on records.
         * @return the builder to chain calls.
         */
        public Builder recordActions(final BiConsumer<DataSetRow, TransformationContext> recordActions) {
            this.recordActions.add(recordActions);
            return this;
        }

        /**
         * @return a new TransformerConfiguration from the builder setup.
         */
        public TransformerConfiguration build() {
            return new TransformerConfiguration(input, output, indexes, preview, columnActions, recordActions);
        }

    }
}