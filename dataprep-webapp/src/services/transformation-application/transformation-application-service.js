(function() {
	'use strict';

	/**
	 * @ngdoc ?????????????
	 * @name ?????????????-pr
	 * @description ?????????????
	 * @requires ????????????
	 * @requires data-prep.services.tran???????????????????
	 * @requires data-prep.services.reci???????????????????
	 * @requires data-prep.services.play???????????????????
	 * @requires data-prep.services.play???????????????????
	 */
	function TransformationApplicationService(PlaygroundService, ColumnSuggestionService, EarlyPreviewService) {
		/**
		 * @ngdoc method??????????????????????????????
		 * @name transformClosure??????????????????????????????
		 * @methodOf data-prep.actions-suggestions-stats.controller:ActionsSuggestionsCtrl??????????????????????????????
		 * @description Transformation application closure. It take the transformation to build the closure.??????????????????????????????
		 * The closure then take the parameters and append the new step in the current preparation??????????????????????????????
		 */
		this.transformClosure = function transformClosure(transfo, transfoScope) {
			/*jshint camelcase: false */
			var currentCol = ColumnSuggestionService.currentColumn;
			return function(params) {
				EarlyPreviewService.deactivatePreview();

				params = params || {};
				params.scope = transfoScope;
				params.column_id = currentCol.id;
				params.column_name = currentCol.name;

				PlaygroundService.appendStep(transfo.name, params)
					.then(EarlyPreviewService.deactivateDynamicModal)
					.finally(function() {
						setTimeout(EarlyPreviewService.activatePreview, 500);
					});
			};
		};

	}

	angular.module('data-prep.services.transformationApplication')
		.service('TransformationApplicationService', TransformationApplicationService);
})();