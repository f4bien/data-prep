// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// https://github.com/Talend/data-prep/blob/master/LICENSE
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package org.talend.dataprep.api.service;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.*;
import static org.talend.daikon.exception.ExceptionContext.withBuilder;
import static org.talend.dataprep.exception.error.PreparationErrorCodes.UNABLE_TO_READ_PREPARATION;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.talend.dataprep.api.preparation.Action;
import org.talend.dataprep.api.preparation.AppendStep;
import org.talend.dataprep.api.preparation.Preparation;
import org.talend.dataprep.api.service.api.PreviewAddParameters;
import org.talend.dataprep.api.service.api.PreviewDiffParameters;
import org.talend.dataprep.api.service.api.PreviewUpdateParameters;
import org.talend.dataprep.api.service.command.dataset.CompatibleDataSetList;
import org.talend.dataprep.api.service.command.preparation.*;
import org.talend.dataprep.command.preparation.PreparationDetailsGet;
import org.talend.dataprep.command.preparation.PreparationGetActions;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.error.APIErrorCodes;
import org.talend.dataprep.exception.error.CommonErrorCodes;
import org.talend.dataprep.http.HttpResponseContext;
import org.talend.dataprep.metrics.Timed;

import com.fasterxml.jackson.core.type.TypeReference;
import com.netflix.hystrix.HystrixCommand;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
public class PreparationAPI extends APIService {

    @RequestMapping(value = "/api/preparations", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all preparations.", notes = "Returns the list of preparations the current user is allowed to see.")
    @Timed
    public void listPreparations(
            @RequestParam(value = "format", defaultValue = "long") @ApiParam(name = "format", value = "Format of the returned document (can be 'long' or 'short'). Defaults to 'long'.") String format,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Listing preparations (pool: {} )...", getConnectionStats());
        }
        PreparationList.Format listFormat = PreparationList.Format.valueOf(format.toUpperCase());
        HystrixCommand<InputStream> command = getCommand(PreparationList.class, listFormat);
        try (InputStream commandResult = command.execute()) {
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(commandResult, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listed preparations (pool: {} )...", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    /**
     * Returns a list containing all data sets metadata that are compatible with a preparation identified by
     * <tt>preparationId</tt>: its id. If no compatible data set is found an empty list is returned. The base data set
     * of the preparation with id <tt>preparationId</tt> is never returned in the list.
     *
     * @param preparationId the specified preparation id
     * @param sort the sort criterion: either name or date.
     * @param order the sorting order: either asc or desc
     */
    @RequestMapping(value = "/api/preparations/{id}/basedatasets", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all data sets that are compatible with a preparation.", notes = "Returns the list of data sets the current user is allowed to see and that are compatible with the preparation.")
    @Timed
    public void listCompatibleDatasets(
            @PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId,
            @ApiParam(value = "Sort key (by name or date), defaults to 'date'.") @RequestParam(defaultValue = "DATE", required = false) String sort,
            @ApiParam(value = "Order for sort key (desc or asc), defaults to 'desc'.") @RequestParam(defaultValue = "DESC", required = false) String order,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Looking for base data set Id (pool: {} )...", getConnectionStats());
        }

        try {
            // get the preparation
            ByteArrayOutputStream temp = new ByteArrayOutputStream();
            getPreparation(preparationId, temp);
            final String preparationJson = new String(temp.toByteArray());
            if (StringUtils.isEmpty(preparationJson)) {
                throw new TDPException(APIErrorCodes.UNABLE_TO_RETRIEVE_PREPARATION_CONTENT);
            }
            final Preparation preparation = mapper.readerFor(Preparation.class).readValue(preparationJson);

            // to list compatible datasets
            String dataSetId = preparation.getDataSetId();
            HystrixCommand<InputStream> listCommand = getCommand(CompatibleDataSetList.class, dataSetId, sort, order);
            InputStream content = listCommand.execute();
            IOUtils.copyLarge(content, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Listing compatible datasets (pool: {}) done.", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_LIST_COMPATIBLE_DATASETS, e);
        }
    }

    @RequestMapping(value = "/api/preparations", method = POST, consumes = APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Create a new preparation for preparation content in body.", notes = "Returns the created preparation id.")
    @Timed
    public String createPreparation(
            @ApiParam(name = "body", value = "The original preparation. You may set all values, service will override values you can't write to.") @RequestBody Preparation preparation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating preparation (pool: {} )...", getConnectionStats());
        }
        PreparationCreate preparationCreate = getCommand(PreparationCreate.class, preparation);
        final String preparationId = preparationCreate.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Created preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = PUT, consumes = APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Update a preparation with content in body.", notes = "Returns the updated preparation id.")
    @Timed
    public String updatePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to update.") @PathVariable("id") String id,
            @ApiParam(name = "body", value = "The updated preparation. Null values are ignored during update. You may set all values, service will override values you can't write to.") @RequestBody Preparation preparation) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation (pool: {} )...", getConnectionStats());
        }
        PreparationUpdate preparationUpdate = getCommand(PreparationUpdate.class, id, preparation);
        final String preparationId = preparationUpdate.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}", method = DELETE, consumes = MediaType.ALL_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Delete a preparation by id", notes = "Delete a preparation content based on provided id. Id should be a UUID returned by the list operation. Not valid or non existing preparation id returns empty content.")
    @Timed
    public String deletePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to delete.") @PathVariable("id") String id) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation (pool: {} )...", getConnectionStats());
        }
        PreparationDelete preparationDelete = getCommand(PreparationDelete.class, id);
        final String preparationId = preparationDelete.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/clone/{id}", method = PUT, produces = MediaType.TEXT_PLAIN_VALUE)
    @ApiOperation(value = "Clone a preparation by id", notes = "Clone a preparation content based on provided id.")
    @Timed
    public String clonePreparation(
            @ApiParam(name = "id", value = "The id of the preparation to clone.") @PathVariable("id") String id) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Cloning preparation (pool: {} )...", getConnectionStats());
        }
        PreparationClone preparationClone = getCommand(PreparationClone.class, id);
        String preparationId = preparationClone.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cloned preparation (pool: {} )...", getConnectionStats());
        }
        return preparationId;
    }

    @RequestMapping(value = "/api/preparations/{id}/details", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preparation by id and details.", notes = "Returns the preparation details.")
    @Timed
    public void getPreparation(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId,
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation details (pool: {} )...", getConnectionStats());
        }

        HystrixCommand<InputStream> command = getCommand(PreparationDetailsGet.class, preparationId);
        try (InputStream commandResult = command.execute()) {
            // You cannot use Preparation object mapper here: to serialize steps & actions, you'd need a version
            // repository not available at API level. Code below copies command result direct to response.
            HttpResponseContext.header("Content-Type", APPLICATION_JSON_VALUE); //$NON-NLS-1$
            IOUtils.copyLarge(commandResult, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation details (pool: {} )...", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/content", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get preparation content by id and at a given version.", notes = "Returns the preparation content at version.")
    @Timed
    public void getPreparation(@PathVariable(value = "id") @ApiParam(name = "id", value = "Preparation id.") String preparationId,
            @RequestParam(value = "version", defaultValue = "head") @ApiParam(name = "version", value = "Version of the preparation (can be 'origin', 'head' or the version id). Defaults to 'head'.") String version,
            @RequestParam(required = false, defaultValue = "full") @ApiParam(name = "sample", value = "Size of the wanted sample, if missing or 'full', the full preparation content is returned") String sample, //
            final OutputStream output) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Retrieving preparation content (pool: {} )...", getConnectionStats());
        }
        Long sampleValue;
        try {
            sampleValue = Long.parseLong(sample);
        } catch (NumberFormatException e) {
            sampleValue = null;
        }

        final PreparationDetailsGet preparationDetailsGet = getCommand(PreparationDetailsGet.class, preparationId);
        HystrixCommand<InputStream> command = getCommand(PreparationGetContent.class, preparationId, version, sampleValue, preparationDetailsGet);
        try (InputStream preparationContent = command.execute()) {
            IOUtils.copyLarge(preparationContent, output);
            output.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieved preparation content (pool: {} )...", getConnectionStats());
            }
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{id}/actions", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Adds an action at the end of preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void addPreparationAction(@ApiParam(name = "id", value = "Preparation id.") @PathVariable(value = "id")  final String preparationId,
                                     @ApiParam("Action to add at end of the preparation.") @RequestBody final AppendStep step) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Adding action to preparation (pool: {} )...", getConnectionStats());
        }

        // get the preparation
        Preparation preparation = internalGetPreparation(preparationId);

        // get the preparation actions
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId);

        // get the diff
        final DiffMetadata diffCommand = getCommand(DiffMetadata.class, preparation.getDataSetId(), preparationId,
                step.getActions(), getActionsCommand);

        // add the action
        final HystrixCommand<Void> command = getCommand(PreparationAddAction.class, preparationId, step, diffCommand);
        command.execute();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added action to preparation (pool: {} )...", getConnectionStats());
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/{preparationId}/actions/{stepId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void updatePreparationAction(@ApiParam(name = "preparationId", value = "Preparation id.") @PathVariable(value = "preparationId") final String preparationId,
                                        @ApiParam(name = "stepId", value = "Step id in the preparation.") @PathVariable(value = "stepId") final String stepId,
                                        @ApiParam("New content for the action.") @RequestBody final AppendStep step) {
    //@formatter:on

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updating preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }

        // get the preparation
        Preparation preparation = internalGetPreparation(preparationId);

        // get the preparation actions for up to the updated action
        final int stepIndex = preparation.getSteps().indexOf(stepId);
        final String parentStepId = preparation.getSteps().get(stepIndex - 1);
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId, parentStepId);

        // get the diff
        final DiffMetadata diffCommand = getCommand(DiffMetadata.class, preparation.getDataSetId(), preparationId,
                step.getActions(), getActionsCommand);

        // get the update action command and execute it
        final HystrixCommand<Void> command = getCommand(PreparationUpdateAction.class, preparationId, stepId, step, diffCommand);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Updated preparation action at step #{} (pool: {} )...", stepId, getConnectionStats());
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/actions/{stepId}", method = DELETE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void deletePreparationAction(@PathVariable(value = "id")
    @ApiParam(name = "id", value = "Preparation id.")
    final String preparationId, @PathVariable(value = "stepId")
    @ApiParam(name = "stepId", value = "Step id to delete.")
    final String stepId) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleting preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }

        final HystrixCommand<Void> command = getCommand(PreparationDeleteAction.class, preparationId, stepId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Deleted preparation action at step #{} (pool: {} ) ...", stepId, //
                    getConnectionStats());
        }
    }

    @RequestMapping(value = "/api/preparations/{id}/head/{headId}", method = PUT, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Delete an action in the preparation.", notes = "Does not return any value, client may expect successful operation based on HTTP status code.")
    @Timed
    public void setPreparationHead(@PathVariable(value = "id")
    @ApiParam(name = "id", value = "Preparation id.")
    final String preparationId, @PathVariable(value = "headId")
    @ApiParam(name = "headId", value = "New head step id")
    final String headId) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moving preparation #{} head to step '{}'...", preparationId, headId);
        }

        final HystrixCommand<Void> command = getCommand(PreparationMoveHead.class, preparationId, headId);
        command.execute();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Moved preparation #{} head to step '{}'...", preparationId, headId);
        }
    }

    // ---------------------------------------------------------------------------------
    // ----------------------------------------PREVIEW----------------------------------
    // ---------------------------------------------------------------------------------

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/diff", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between 2 steps of the same preparation.")
    @Timed
    public void previewDiff(@RequestBody final PreviewDiffParameters input, final OutputStream output) {
    //@formatter:on

        // get preparation details
        final Preparation preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> lastActiveStepActions = internalGetActions(preparation.getId(), input.getCurrentStepId());
        final List<Action> previewStepActions = internalGetActions(preparation.getId(), input.getPreviewStepId());

        final HystrixCommand<InputStream> transformation = getCommand(PreviewDiff.class, input, preparation, lastActiveStepActions, previewStepActions);
        try (InputStream commandResult = transformation.execute()) {
            IOUtils.copyLarge(commandResult, output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/update", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview diff between the same step of the same preparation but with one step update.")
    public void previewUpdate(@RequestBody final PreviewUpdateParameters input, final OutputStream output) {
    //@formatter:on

        // get preparation details
        final Preparation preparation = internalGetPreparation(input.getPreparationId());
        final List<Action> actions = internalGetActions(preparation.getId());

        final HystrixCommand<InputStream> transformation = getCommand(PreviewUpdate.class, input, preparation, actions);
        try (InputStream commandResult = transformation.execute()) {
            IOUtils.copyLarge(commandResult, output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }
    }

    //@formatter:off
    @RequestMapping(value = "/api/preparations/preview/add", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get a preview between the head step and a new appended transformation")
    public void previewAdd(@RequestBody @Valid final PreviewAddParameters input, final OutputStream output) {
    //@formatter:on

        Preparation preparation = null;
        List<Action> actions = new ArrayList<>(0);

        // get preparation details with dealing with preparations
        if (StringUtils.isNotBlank(input.getPreparationId())) {
            preparation = internalGetPreparation(input.getPreparationId());
            actions = internalGetActions(preparation.getId());
        }

        final HystrixCommand<InputStream> transformation = getCommand(PreviewAdd.class, input, preparation, actions);

        try (InputStream commandResult = transformation.execute()) {
            IOUtils.copyLarge(commandResult, output);
            output.flush();
        } catch (Exception e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_TRANSFORM_DATASET, e);
        }

    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId) {
        return internalGetActions(preparationId, "head");
    }

    /**
     * Helper method used to retrieve preparation actions via a hystrix command.
     *
     * @param preparationId the preparation id to get the actions from.
     * @param stepId the preparation version.
     * @return the preparation actions.
     */
    private List<Action> internalGetActions(String preparationId, String stepId) {
        final PreparationGetActions getActionsCommand = getCommand(PreparationGetActions.class, preparationId, stepId);
        try {
            return mapper.readerFor(new TypeReference<List<Action>>() {
            }).readValue(getActionsCommand.execute());
        } catch (IOException e) {
            throw new TDPException(APIErrorCodes.UNABLE_TO_GET_PREPARATION_DETAILS, e);
        }
    }


    /**
     * Helper method used to get a preparation for internal class use.
     *
     * @param preparationId the preparation id.
     * @return the preparation.
     */
    private Preparation internalGetPreparation(String preparationId) {
        ByteArrayOutputStream temp = new ByteArrayOutputStream();
        getPreparation(preparationId, temp);
        try {
            return mapper.readerFor(Preparation.class).readValue(new ByteArrayInputStream(temp.toByteArray()));
        } catch (IOException e) {
            throw new TDPException(UNABLE_TO_READ_PREPARATION, e, withBuilder().put("id", preparationId).build());
        }
    }

}
