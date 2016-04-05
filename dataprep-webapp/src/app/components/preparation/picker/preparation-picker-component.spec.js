/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Preparation Picker Component', () => {

    let createElement, scope, element, controller, stateMock;

    const datasets = [
        {id: 'de3cc32a-b624-484e-b8e7-dab9061a009c', name: 'my dataset'},
        {id: '4d0a2718-bec6-4614-ad6c-8b3b326ff6c9', name: 'my second dataset'},
        {id: '555a2718-bec6-4614-ad6c-8b3b326ff6c7', name: 'my second dataset (1)'}
    ];

    const candidatePreparations = [
        {
            prepration: {
                'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
                'dataSetId': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
                'author': 'anonymousUser',
                'creationDate': 1427447300300,
                'steps': [
                    '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                    '4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
                    '8a1c49d1b64270482e8db8232357c6815615b7cf',
                    '599725f0e1331d5f8aae24f22cd1ec768b10348d'
                ]
            },
            dataset: datasets[0]
        },
        {
            prepration: {
                'id': 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
                'dataSetId': '4d0a2718-bec6-4614-ad6c-8b3b326ff6c9',
                'author': 'anonymousUser',
                'creationDate': 1427447330693,
                'steps': [
                    '47e2444dd1301120b539804507fd307072294048',
                    'ae1aebf4b3fa9b983c895486612c02c766305410',
                    '24dcd68f2117b9f93662cb58cc31bf36d6e2867a',
                    '599725f0e1331d5f8aae24f22cd1ec768b10348d'
                ]
            },
            dataset: datasets[1]
        }
    ];

    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(angular.mock.module('data-prep.preparation-picker', ($provide) => {
        stateMock = {
            playground: {
                dataset: datasets[0]
            }
        };
        $provide.constant('state', stateMock);
    }));

    beforeEach(angular.mock.module('pascalprecht.translate', function ($translateProvider) {
        $translateProvider.translations('en', {
            'NO_COMPATIBLE_PREPARATIONS': 'There are no compatible preparations to dataset: {{name}}'
        });
        $translateProvider.preferredLanguage('en');
    }));

    beforeEach(inject(($rootScope, $compile) => {
        scope = $rootScope.$new();
        scope.closePicker = () => {};

        createElement = () => {
            element = angular.element(
                `<preparation-picker
                    close-picker="closePicker"
                </preparation-picker>`);
            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('preparationPicker');
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('display preparations list', () => {

        it('should render preparations list', inject(($q, DatasetService) => {
            //given
            spyOn(DatasetService, 'getCompatiblePreparations').and.returnValue($q.when(candidatePreparations));

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.find('inventory-tile').length).toBe(candidatePreparations.length);
        }));
    });

    describe('No preparations to display', () => {

        it('should show message', inject(($q, DatasetService) => {
            //given
            spyOn(DatasetService, 'getCompatiblePreparations').and.returnValue($q.when([]));

            //when
            createElement();
            scope.$digest();

            //then
            expect(element.find('span').eq(0).text()).toBe('There are no compatible preparations to dataset: ' + datasets[0].name);
        }));
    });
});