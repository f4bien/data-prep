describe('Preparation list controller', function() {
    'use strict';

    var createController, scope;
    var allPreparations = [
        {
            'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            'dataSetId': 'ddb74c89-6d23-4528-9f37-7a9860bb468e',
            'author': 'anonymousUser',
            'creationDate': 1427447300300,
            'steps': [
                '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                '4ff5d9a6ca2e75ebe3579740a4297fbdb9b7894f',
                '8a1c49d1b64270482e8db8232357c6815615b7cf',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d'
            ],
            'actions': [
                {
                    'action': 'lowercase',
                    'parameters': {
                        'column_name': 'birth'
                    }
                },
                {
                    'action': 'uppercase',
                    'parameters': {
                        'column_name': 'country'
                    }
                },
                {
                    'action': 'cut',
                    'parameters': {
                        'pattern': '.',
                        'column_name': 'first_item'
                    }
                }
            ]
        },
        {
            'id': 'fbaa18e82e913e97e5f0e9d40f04413412be1126',
            'dataSetId': '4d0a2718-bec6-4614-ad6c-8b3b326ff6c7',
            'author': 'anonymousUser',
            'creationDate': 1427447330693,
            'steps': [
                '47e2444dd1301120b539804507fd307072294048',
                'ae1aebf4b3fa9b983c895486612c02c766305410',
                '24dcd68f2117b9f93662cb58cc31bf36d6e2867a',
                '599725f0e1331d5f8aae24f22cd1ec768b10348d'
            ],
            'actions': [
                {
                    'action': 'cut',
                    'parameters': {
                        'pattern': '-',
                        'column_name': 'birth'
                    }
                },
                {
                    'action': 'fillemptywithdefault',
                    'parameters': {
                        'default_value': 'N/A',
                        'column_name': 'state'
                    }
                },
                {
                    'action': 'uppercase',
                    'parameters': {
                        'column_name': 'lastname'
                    }
                }
            ]
        }
    ];

    beforeEach(module('data-prep.preparation-list'));

    beforeEach(inject(function($q, $rootScope, $controller, PreparationService, PlaygroundService, DatasetListService) {
        scope = $rootScope.$new();

        createController = function() {
            var ctrl =  $controller('PreparationListCtrl', {
                $scope: scope
            });
            return ctrl;
        };

        spyOn(DatasetListService, 'getDatasetsPromise').and.returnValue($q.when([]));
        spyOn(PreparationService, 'getPreparations').and.returnValue($q.when({data: allPreparations}));
        spyOn(PlaygroundService, 'load').and.returnValue($q.when(true));
        spyOn(PlaygroundService, 'show').and.callThrough();
    }));

    it('should init preparations', inject(function() {
        //given

        //when
        var ctrl = createController();
        scope.$digest();

        //then
        expect(ctrl.preparations).toBe(allPreparations);
    }));

    it('should load preparation and show playground', inject(function(PlaygroundService) {
        //given
        var ctrl = createController();
        var preparation = {
            id: 'de618c62ef97b3a95b5c171bc077ffe22e1d6f79',
            dataSetId: 'dacd45cf-5bd0-4768-a9b7-f6c199581efc',
            author: 'anonymousUser',
            creationDate: 1427460984585,
            steps: [
                '228c16230de53de5992eb44c7aba362ac714ab1c'
            ],
            actions: []
        };
           
        //when
        ctrl.load(preparation);
        scope.$digest();

        //then
        expect(PlaygroundService.load).toHaveBeenCalledWith(preparation);
        expect(PlaygroundService.show).toHaveBeenCalled();
    }));
});