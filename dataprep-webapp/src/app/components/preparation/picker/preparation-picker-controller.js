/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

class PreparationPickerCtrl {
    constructor($rootScope, $state, state, DatasetService, PlaygroundService, PreparationService, RecipeService) {
        'ngInject';

        this.$rootScope = $rootScope;
        this.$state = $state;
        this.datasetService = DatasetService;

        this.state = state;
        this.newPreparationName = this.state.playground.preparation ?
            this.state.playground.preparation.name :
            this.state.playground.dataset.name;
        this.datasetId = this.state.playground.dataset.id;

        this.preparationService = PreparationService;
        this.recipeService = RecipeService;
        this.candidatePreparations = [];
        this.playgroundService = PlaygroundService;
    }

    /**
     * @ngdoc method
     * @name $onInit
     * @methodOf data-prep.preparation-picker.controller:PreparationPickerCtrl
     * @description initializes preparation picker form
     **/
    $onInit() {
        this.isFetchingPreparations = true;
        this.datasetService.getCompatiblePreparations(this.datasetId)
            .then((compatiblePreparations) => {
                this.candidatePreparations = compatiblePreparations;
            })
            .finally(() => {
                this.isFetchingPreparations = false;
            });
    }

    /**
     * @ngdoc method
     * @name selectPreparation
     * @methodOf data-prep.preparation-picker.controller:PreparationPickerCtrl
     * @param {Object} selectedPrepa selected preparation
     * @description selects the preparation to apply
     **/
    selectPreparation(selectedPrepa) {
        this.selectedPreparation = selectedPrepa;
        this.playgroundService.applyPreparationToDataset(this.selectedPreparation.id, this.datasetId, this.newPreparationName)
            .then((updatedPreparationId) => {
                this.closePicker();
                this.$state.go('playground.preparation', {prepid: updatedPreparationId}, {reload: true});
            });
    }

}

export default PreparationPickerCtrl