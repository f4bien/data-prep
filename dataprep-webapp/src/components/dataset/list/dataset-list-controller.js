(function() {
    'use strict';

    function DatasetListCtrl($q, DatasetService, DatasetListService, DatasetGridService) {
        var vm = this;
        vm.datasetListService = DatasetListService;

        /**
         * Last selected dataset metadata
         * @type {dataset}
         */
        vm.lastSelectedMetadata = null;

        /**
         * Last selected records and columns
         * @type {data}
         */
        vm.lastSelectedData = null;

        /**
         * Get the dataset data and display data modal
         * @param dataset - the dataset to open
         */
        vm.open = function(dataset) {
            var getDataPromise;
            if(vm.lastSelectedMetadata && dataset.id === vm.lastSelectedMetadata.id) {
                getDataPromise = $q.when(true);
            }
            else {
                getDataPromise = DatasetService.getDataFromId(dataset.id, false)
                    .then(function(data) {
                        vm.lastSelectedMetadata = dataset;
                        vm.lastSelectedData = data;
                    });
            }
            getDataPromise.then(function() {
                DatasetGridService.setDataset(vm.lastSelectedMetadata, vm.lastSelectedData);
                DatasetGridService.show();
            });
        };

        /**
         * Delete a dataset
         * @param dataset - the dataset to delete
         */
        vm.delete = function(dataset) {
            DatasetService.deleteDataset(dataset)
                .then(function() {
                    DatasetListService.refreshDatasets();
                });
        };

        DatasetListService.refreshDatasets();
    }

    Object.defineProperty(DatasetListCtrl.prototype,
        'datasets', {
            enumerable: true,
            configurable: false,
            get: function () {
                return this.datasetListService.datasets;
            },
            set: function(value) {
                this.datasetListService.datasets = value;
            }
        });

    angular.module('data-prep-dataset')
        .controller('DatasetListCtrl', DatasetListCtrl);
})();