(function() {
    'use strict';

    /**
     * @ngdoc object
     * @name data-prep.actions-suggestions
     * @description This module contains the controller and directives to manage suggested transformation list
     * @requires talend.widget
     * @requires data-prep.services.transformation
     * @requires data-prep.services.playground
     * @requires data-prep.services.preparation
     */
    angular.module('data-prep.actions-suggestions', [
        'talend.widget',
        'data-prep.services.transformation',
        'data-prep.services.playground',
        'data-prep.services.preparation'
    ]);
})();