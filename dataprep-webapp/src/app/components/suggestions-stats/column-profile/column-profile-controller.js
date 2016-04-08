/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

/**
 * @ngdoc controller
 * @name data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
 * @description Column profile controller.
 * @requires data-prep.services.state.constant:state
 * @requires data-prep.statistics.service:StatisticsService
 * @requires data-prep.statistics.service:StatisticsTooltipService
 * @requires data-prep.services.filter.service:FilterService
 */
export default function ColumnProfileCtrl($translate, $timeout, state, StatisticsService, StatisticsTooltipService, FilterService) {
    'ngInject';

    var vm = this;
    vm.chartConfig = {};
    vm.state = state;
    vm.statisticsService = StatisticsService;
    vm.statisticsTooltipService = StatisticsTooltipService;

    //------------------------------------------------------------------------------------------------------
    //------------------------------------------------FILTER------------------------------------------------
    //------------------------------------------------------------------------------------------------------
    function addExactFilter(value) {
        var column = state.playground.grid.selectedColumn;
        return value.length ?
            FilterService.addFilterAndDigest('exact', column.id, column.name, {
                phrase: value,
                caseSensitive: true
            }) :
            FilterService.addFilterAndDigest('empty_records', column.id, column.name);
    }

    /**
     * @ngdoc property
     * @name addBarchartFilter
     * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description Add an "exact" case sensitive filter if the value is not empty, an "empty_records" filter otherwise
     * @type {array}
     */
    vm.addBarchartFilter = function addBarchartFilter(item) {
        return addExactFilter(item.data);
    };

    /**
     * @ngdoc method
     * @name addRangeFilter
     * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description Add an "range" filter
     * @param {object} interval The interval [min, max] to filter
     */
    vm.addRangeFilter = function addRangeFilter(interval) {
        var selectedColumn = state.playground.grid.selectedColumn;

        if (!interval.label) {
            var min = d3.format(',')(interval.min);
            var max = d3.format(',')(interval.max);
            if(min === max){
                interval.label = '[' + min + ']';
            }
            else {
                interval.label = interval.isMaxReached ? '[' + min + ' .. ' + max + ']' : '[' + min + ' .. ' + max + '[';
            }
        }
        var removeFilterFn = StatisticsService.getRangeFilterRemoveFn();
        FilterService.addFilterAndDigest('inside_range',
            selectedColumn.id,
            selectedColumn.name,
            {
                interval: [interval.min, interval.max],
                label: interval.label,
                type: selectedColumn.type,
                isMaxReached: interval.isMaxReached
            },
            removeFilterFn);
    };

    //------------------------------------------------------------------------------------------------------
    //----------------------------------------------AGGREGATION---------------------------------------------
    //------------------------------------------------------------------------------------------------------
    /**
     * @ngdoc property
     * @name aggregations
     * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description The list of possible aggregations
     * @type {array}
     */
    vm.aggregations = ['SUM', 'MAX', 'MIN', 'AVERAGE'];

    vm.selectAggregationColumn = function() {
        if(!vm.selectedAggregationColumn) {
            vm.selectedAggregation = null;
        }
        else {
            vm.selectedAggregation = state.playground.statistics.histogram.aggregation || vm.aggregations[0];
        }
        vm.updateSelectedAggregationDetails();
    };

    vm.resetAggregation = function() {
        vm.selectedAggregationColumn = state.playground.statistics.histogram && state.playground.statistics.histogram.aggregationColumn;
        vm.selectedAggregation = state.playground.statistics.histogram && state.playground.statistics.histogram.aggregation;
        vm.updateSelectedAggregationDetails();
    };

    vm.updateSelectedAggregationDetails = function() {
        if(! vm.selectedAggregationColumn) {
            vm.selectedAggregationDetails = null;
        }
        else {
            const aggregCol = vm.selectedAggregationColumn.name;
            const aggreg = $translate.instant(vm.selectedAggregation);
            const group = state.playground.statistics.histogram.column.name;
            const selectedOptions = {aggreg: aggreg, col: aggregCol, group: group};
            vm.selectedAggregationDetails = $translate.instant('AGGREGATION_DETAILS', selectedOptions);
        }
    };

    vm.getCurrentAggregationDetails = function() {
        if(!state.playground.statistics.histogram || !state.playground.statistics.histogram.aggregation) {
            return null;
        }
        else {
            const aggregCol = state.playground.statistics.histogram.aggregationColumn.name;
            const aggreg = $translate.instant(state.playground.statistics.histogram.aggregation);
            const group = state.playground.statistics.histogram.column.name;
            const selectedOptions = {aggreg: aggreg, col: aggregCol, group: group};
            return $translate.instant('AGGREGATION_DETAILS', selectedOptions);
        }
    };

    /**
     * @ngdoc method
     * @name getCurrentAggregation
     * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @description The current aggregations
     * @return {string} The current aggregation name
     */
    vm.getCurrentAggregation = function getCurrentAggregation() {
        return state.playground.statistics.histogram && state.playground.statistics.histogram.aggregation ?
            state.playground.statistics.histogram.aggregation :
            'LINE_COUNT';
    };

    /**
     * @ngdoc method
     * @name changeAggregation
     * @methodOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
     * @param {object} column The column to aggregate
     * @param {object} aggregation The aggregation to perform
     * @description Trigger a new aggregation graph
     */
    vm.changeAggregation = function changeAggregation() {
        const column = vm.selectedAggregationColumn;
        const aggregation = vm.selectedAggregation;

        if (state.playground.statistics.histogram &&
            state.playground.statistics.histogram.aggregationColumn === column &&
            state.playground.statistics.histogram.aggregation === aggregation) {
            return;
        }

        if (aggregation) {
            StatisticsService.processAggregation(column, aggregation);
        }
        else {
            StatisticsService.processClassicChart();
        }
    };
}

/**
 * @ngdoc property
 * @name aggregationColumns
 * @propertyOf data-prep.actions-suggestions-stats.controller:ColumnProfileCtrl
 * @description The numeric columns list of the dataset.
 * This is bound to {@link data-prep.statistics:StatisticsService StatisticsService}.getAggregationColumns()
 */
Object.defineProperty(ColumnProfileCtrl.prototype,
    'aggregationColumns', {
        enumerable: true,
        configurable: true,
        get: function () {
            return this.statisticsService.getAggregationColumns();
        }
    });
