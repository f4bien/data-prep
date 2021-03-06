                                                                                                                                                                                           /*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

export const playgroundState = {
    preparationName: '',
    previousState: 'nav.index.datasets',
    previousStateOptions: {folderPath: ''},
    preparation: null,
    candidatePreparations: [],
    displayPreparationPicker: false
};

export function PlaygroundStateService(RecipeStateService, recipeState,
                                       GridStateService, gridState,
                                       FilterStateService, filterState,
                                       SuggestionsStateService, suggestionsState,
                                       LookupStateService, lookupState,
                                       StatisticsStateService, statisticsState,
                                       ParametersStateService, parametersState) {
    'ngInject';

    playgroundState.recipe = recipeState;
    playgroundState.grid = gridState;
    playgroundState.lookup = lookupState;
    playgroundState.filter = filterState;
    playgroundState.suggestions = suggestionsState;
    playgroundState.statistics = statisticsState;
    playgroundState.parameters = parametersState;

    return {
        //playground
        reset: reset,
        setDataset: setDataset,
        setIsFetchingStats: setIsFetchingStats,
        setPreparation: setPreparation,
        setPreparationName: setPreparationName,
        setNameEditionMode: setNameEditionMode,
        setData: setData,
        setPreviousState: setPreviousState,
        setPreviousStateOptions: setPreviousStateOptions,
        updateDatasetRecord: updateDatasetRecord,
        updateDatasetStatistics: updateDatasetStatistics,
        setCandidatePreparations: setCandidatePreparations,
        updatePreparationPickerDisplay: updatePreparationPickerDisplay,

        //parameters
        toggleDatasetParameters: toggleDatasetParameters,
        hideDatasetParameters: ParametersStateService.hide,
        setIsSendingDatasetParameters: ParametersStateService.setIsSending,
        setDatasetEncodings: ParametersStateService.setEncodings,

        //recipe
        showRecipe: RecipeStateService.show,
        hideRecipe: RecipeStateService.hide,

        //datagrid
        setColumnFocus: GridStateService.setColumnFocus,
        setGridSelection: GridStateService.setGridSelection,

        //lookup
        setLookupActions: LookupStateService.setActions,
        setLookupAddedActions: LookupStateService.setAddedActions,
        setLookupDatasets: LookupStateService.setDatasets,
        setLookupAddMode: LookupStateService.setAddMode,
        setLookupSelectedColumn: LookupStateService.setSelectedColumn,
        setLookupUpdateMode: LookupStateService.setUpdateMode,
        setLookupVisibility: LookupStateService.setVisibility,
        updateLookupColumnsToAdd: LookupStateService.updateColumnsToAdd,
        setLookupDatasetsSort: LookupStateService.setSort,
        setLookupDatasetsOrder: LookupStateService.setOrder,

        //filters
        addGridFilter: addGridFilter,
        updateGridFilter: updateGridFilter,
        removeGridFilter: removeGridFilter,
        removeAllGridFilters: removeAllGridFilters,
        enableFilters: enableFilters,
        disableFilters: disableFilters,

        //suggestion
        setColumnTransformations: SuggestionsStateService.setColumnTransformations,
        setLineTransformations: SuggestionsStateService.setLineTransformations,
        setSuggestionsLoading: SuggestionsStateService.setLoading,
        setTransformationsForEmptyCells: SuggestionsStateService.setTransformationsForEmptyCells,
        setTransformationsForInvalidCells: SuggestionsStateService.setTransformationsForInvalidCells,
        updateFilteredTransformations: SuggestionsStateService.updateFilteredTransformations,

        //statistics
        setStatisticsBoxPlot: StatisticsStateService.setBoxPlot,
        setStatisticsDetails: StatisticsStateService.setDetails,
        setStatisticsRangeLimits: StatisticsStateService.setRangeLimits,
        setStatisticsHistogram: StatisticsStateService.setHistogram,
        setStatisticsFilteredHistogram: StatisticsStateService.setFilteredHistogram,
        setStatisticsHistogramActiveLimits: StatisticsStateService.setHistogramActiveLimits,
        setStatisticsPatterns: StatisticsStateService.setPatterns,
        setStatisticsFilteredPatterns: StatisticsStateService.setFilteredPatterns
    };

    //--------------------------------------------------------------------------------------------------------------
    //--------------------------------------------------PLAYGROUND--------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    function setDataset(dataset) {
        playgroundState.dataset = dataset;
    }

    function setData(data) {
        playgroundState.data = data;
        GridStateService.setData(data);

        if(filterState.enabled) {
            GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        } else {
            GridStateService.setFilter([], playgroundState.data);
        }
    }

    function setPreparation(preparation) {
        playgroundState.preparation = preparation;
    }

    function setPreparationName(preparationName) {
        playgroundState.preparationName = preparationName;
    }

    function setNameEditionMode(editionMode) {
        playgroundState.nameEditionMode = editionMode;
    }

    function updateDatasetStatistics(metadata) {
        _.forEach(playgroundState.data.metadata.columns, function (col) {
            var correspondingColumn = _.find(metadata.columns, {id: col.id});
            col.statistics = correspondingColumn.statistics;
        });
    }

    function updateDatasetRecord(records) {
        playgroundState.dataset.records = records;
    }

    function setPreviousState(previousState) {
        playgroundState.previousState = previousState;
    }

    function setPreviousStateOptions(previousStateOptions) {
        playgroundState.previousStateOptions = previousStateOptions;
    }

    function setIsFetchingStats(value) {
        playgroundState.isFetchingStats = value;
    }

    function setCandidatePreparations (preparationsList) {
        playgroundState.candidatePreparations = preparationsList;
    }

    function updatePreparationPickerDisplay (display) {
        playgroundState.displayPreparationPicker = display;
    }
    //--------------------------------------------------------------------------------------------------------------
    //-------------------------------------------------PARAMETERS---------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    function toggleDatasetParameters() {
        if (parametersState.visible) {
            ParametersStateService.hide();
        }
        else {
            showDatasetParameters();
        }
    }

    function showDatasetParameters() {
        ParametersStateService.update(playgroundState.dataset);
        ParametersStateService.show();
    }

    //--------------------------------------------------------------------------------------------------------------
    //---------------------------------------------------FILTERS----------------------------------------------------
    //--------------------------------------------------------------------------------------------------------------
    function addGridFilter(filter) {
        FilterStateService.addGridFilter(filter);
        GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        FilterStateService.enableFilters();
    }

    function updateGridFilter(oldFilter, newFilter) {
        FilterStateService.updateGridFilter(oldFilter, newFilter);
        GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
        FilterStateService.enableFilters();
    }

    function removeGridFilter(filter) {
        FilterStateService.removeGridFilter(filter);
        GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
    }

    function removeAllGridFilters() {
        FilterStateService.removeAllGridFilters();
        GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
    }

    function enableFilters() {
        FilterStateService.enableFilters();
        GridStateService.setFilter(filterState.gridFilters, playgroundState.data);
    }

    function disableFilters() {
        FilterStateService.disableFilters();
        GridStateService.setFilter([], playgroundState.data);
    }


    function reset() {
        playgroundState.data = null;
        playgroundState.dataset = null;
        playgroundState.preparation = null;
        playgroundState.nameEditionMode = false;
        playgroundState.lookupData = null;
        playgroundState.exportParameters = null;
        playgroundState.isFetchingStats = false;

        RecipeStateService.reset();
        FilterStateService.reset();
        GridStateService.reset();
        LookupStateService.reset();
        SuggestionsStateService.reset();
        StatisticsStateService.reset();
        ParametersStateService.reset();
    }
}