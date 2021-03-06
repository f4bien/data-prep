package org.talend.dataprep.transformation.pipeline;

import org.talend.dataprep.api.dataset.DataSetRow;
import org.talend.dataprep.api.dataset.RowMetadata;

/**
 * Links together {@link RuntimeNode nodes}. Each implementation is responsible for handling correct
 * {@link #emit(DataSetRow, RowMetadata)} and {@link #signal(Signal)} methods.
 */
public interface RuntimeLink {

    /**
     * Emits a new row and its corresponding metadata.
     * 
     * @param row A {@link DataSetRow row} to emit to the next {@link RuntimeNode}.
     * @param metadata The {@link RowMetadata row metadata} to be used by the next {@link RuntimeNode}.
     */
    void emit(DataSetRow row, RowMetadata metadata);

    /**
     * Sends a {@link Signal event} to the {@link RuntimeNode}. Signals are data-independent events to indicate external
     * events (such as end of the stream).
     * 
     * @param signal A {@link Signal signal} to be sent to the pipeline.
     */
    void signal(Signal signal);

}
