/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Transformation simple params directive', function () {
    'use strict';
    var scope, createElement;

    beforeEach(angular.mock.module('data-prep.transformation-form'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'COLON': ': '
        });
        $translateProvider.preferredLanguage('en');
    }));


    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();

        createElement = function() {
            var element = angular.element('<transform-simple-param parameter="parameter"></transform-simple-param>');
            $compile(element)(scope);
            scope.$digest();
            return element;
        };
    }));

    it('should render an action with a parameter', function() {
        //given
        scope.parameter =   {
                'name': 'param1',
                'label': 'Param 1',
                'type': 'string',
                'inputType': 'text',
                'default': '.'
            };

        //when
        var element = createElement();

        //then
        expect(element.find('.param-name').text().trim()).toBe('Param 1:');
        expect(element.find('.param-input').find('input[type="text"]').length).toBe(1);
    });
});
