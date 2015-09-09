package org.talend.dataprep.api.service.command.preparation;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.PreviewUpdateInput;

@Component
@Scope("request")
public class PreviewUpdate extends PreviewAbstract {

    /** The all the preview parameters. */
    private final PreviewUpdateInput input;

    public PreviewUpdate(final HttpClient client, final PreviewUpdateInput input) {
        super(client);
        this.input = input;
    }

    @Override
    protected InputStream run() throws Exception {

        // get preparation details
        final Preparation preparation = getPreparation(input.getPreparationId());
        final String dataSetId = preparation.getDataSetId();

        //Get steps from first transformation
        final List<String> steps = preparation.getSteps();
        steps.remove(0);
        
        // extract actions by steps in chronological order, until defined last active step (from input)
        Map<String, Action> originalActions = new LinkedHashMap<>();
        final Iterator<Action> actions = getPreparationActions(preparation, input.getCurrentStepId()).iterator();
        steps.stream().filter(step -> actions.hasNext()).forEach(step -> originalActions.put(step, actions.next()));

        // modify actions to include the update
        final Map<String, Action> modifiedActions = new LinkedHashMap<>(originalActions);
        if (modifiedActions.get(input.getUpdateStepId()) != null) {
            modifiedActions.put(input.getUpdateStepId(), input.getAction());
        }

        // serialize and base 64 encode the 2 actions list
        final String oldEncodedActions = serializeActions(originalActions.values());
        final String newEncodedActions = serializeActions(modifiedActions.values());

        // get dataset content
        final InputStream content = getDatasetContent(dataSetId);
        // get usable tdpIds
        final String encodedTdpIds = serializeIds(input.getTdpIds());

        // call transformation preview with content and the 2 transformations
        return previewTransformation(content, oldEncodedActions, newEncodedActions, encodedTdpIds);
    }

}
