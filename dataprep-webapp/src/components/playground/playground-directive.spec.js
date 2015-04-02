describe('Playground directive', function() {
    'use strict';

    var scope, createElement, element;

    var metadata = {
        'id': '12ce6c32-bf80-41c8-92e5-66d70f22ec1f',
        'name': 'US States',
        'author': 'anonymousUser',
        'created': '02-03-2015 14:52',
        records: '3'
    };

    beforeEach(module('data-prep.playground'));
    beforeEach(module('htmlTemplates'));
    beforeEach(module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'FILE_DETAILS': 'File: {{name}} ({{records}} lines)'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(function($rootScope, $compile, PreparationListService) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<playground></playground>');
            angular.element('body').append(element);

            $compile(element)(scope);
            scope.$digest();
        };

        spyOn(PreparationListService, 'refreshPreparations').and.callFake(function() {});
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render playground elements', inject(function(PlaygroundService) {
        //given
        PlaygroundService.currentMetadata = metadata;

        //when
        createElement();

        //then
        var playground = angular.element('body').find('.playground').eq(0);

        //check header is present and contains description and search filter
        expect(playground.find('.modal-header').length).toBe(1);
        expect(playground.find('.modal-header').eq(0).find('li').eq(1).text().trim()).toBe('File: US States (3 lines)');

        //check body is present
        expect(playground.find('.modal-body').length).toBe(1);

        //check left slidable is hidden recipe with left slide action
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(0).hasClass('recipe')).toBe(true);
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(0).hasClass('slide-hide')).toBe(true);
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(0).find('.action').eq(0).hasClass('right')).toBe(false);

        //check right slidable is displayed transformations with right slide action
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(1).hasClass('suggestions')).toBe(true);
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(1).hasClass('slide-hide')).toBe(false);
        expect(playground.find('.modal-body').eq(0).find('.slidable').eq(1).find('.action').eq(0).hasClass('right')).toBe(true);

        //check datagrid and filters are present
        expect(playground.find('.modal-body').eq(0).find('.filter-list').length).toBe(1);
        expect(playground.find('.modal-body').eq(0).find('.filter-list').find('.filter-search').length).toBe(1);
        expect(playground.find('.modal-body').eq(0).find('datagrid').length).toBe(1);
    }));

    it('should refresh preparation list on playground hide', inject(function(PlaygroundService, PreparationListService) {
        //given
        PlaygroundService.currentMetadata = metadata;
        createElement();

        PlaygroundService.show();
        scope.$apply();
        expect(PreparationListService.refreshPreparations).not.toHaveBeenCalled();

        //when
        PlaygroundService.hide();
        scope.$apply();

        //then
        expect(PreparationListService.refreshPreparations).toHaveBeenCalled();
    }));
});