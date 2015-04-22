describe('Dataset list directive', function() {
    'use strict';
    
    var scope, createElement;
    var datasets = [
        {
            'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
            'name': 'US States',
            'author': 'anonymousUser',
            'created': '02-03-2015 14:52'
        },
        {
            'id': 'e93b9c92-e054-4f6a-a38f-ca52f22ead2b',
            'name': 'Customers',
            'author': 'anonymousUser',
            'created': '02-03-2015 14:53'
        }
    ];

    beforeEach(module('data-prep.dataset-list'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'INVENTORY_DETAILS': 'owned by {{author}}, created on {{created}}, contains {{records}} lines'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile, $q, DatasetListService) {
        scope = $rootScope.$new();
        createElement = function() {
            var element = angular.element('<dataset-list></dataset-list>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };

        spyOn(DatasetListService, 'refreshDatasets').and.callFake(function() {
            DatasetListService.datasets = datasets;
            return $q.when(datasets);
        });
    }));

    it('should render dataset list', function() {
        //when
        var element = createElement();

        //then
        expect(element.find('td.inventory-title').eq(0).text()).toBe('US States');
        expect(element.find('.inventory-description').eq(0).text()).toBe('owned by anonymousUser, created on 02-03-2015 14:52, contains  lines');
        expect(element.find('td.inventory-title').eq(1).text()).toBe('Customers');
        expect(element.find('.inventory-description').eq(1).text()).toBe('owned by anonymousUser, created on 02-03-2015 14:53, contains  lines');
    });
});