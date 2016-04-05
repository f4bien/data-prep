/*  ============================================================================

 Copyright (C) 2006-2016 Talend Inc. - www.talend.com

 This source code is available under agreement available at
 https://github.com/Talend/data-prep/blob/master/LICENSE

 You should have received a copy of the agreement
 along with this program; if not, write to Talend SA
 9 rue Pages 92150 Suresnes, France

 ============================================================================*/

describe('Inventory Tile Component', () => {
    var scope, createElement, element, controller;

    beforeEach(angular.mock.module('data-prep.inventory-tile'));
    beforeEach(angular.mock.module('htmlTemplates'));

    beforeEach(inject(function ($rootScope, $compile) {
        scope = $rootScope.$new();

        scope.dataset = {
            'id': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            'name': 'customers_jso_light',
            'author': 'anonymousUser',
            'records': 15,
            'nbLinesHeader': 1,
            'nbLinesFooter': 0,
            'created': '03-30-2015 08:06'
        };

        scope.preparation = {
            'id': 'ab136cbf0923a7f11bea713adb74ecf919e05cfa',
            'dataSetId': 'de3cc32a-b624-484e-b8e7-dab9061a009c',
            'author': 'anonymousUser',
            'creationDate': 1427447300000,
            'lastModificationDate': 1427447300300,
            'steps': [
                '35890aabcf9115e4309d4ce93367bf5e4e77b82a',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82b',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82c',
                '35890aabcf9115e4309d4ce93367bf5e4e77b82d',
            ],
            favorite: false,
            'actions': [
                {
                    'action': 'lowercase',
                    'parameters': {
                        'column_name': 'birth'
                    }
                }
            ]
        };

        createElement = () => {
            element = angular.element(
                `<inventory-tile
                        dataset="dataset"
                        editable-title="false"
                        on-clone="clone(preparation)"
                        on-favortite="onFavortite(preparation)"
                        on-remove="remove(preparation)"
                        on-rename="rename(preparation, text)"
                        on-tile-click="onTileClick"
                        on-title-click="onTitleClick"
                        preparation="preparation"
                        show-clone-icon="true"
                        show-favorite-icon="true"
                        show-remove-icon="true"
                ></inventory-tile>`
            );
            $compile(element)(scope);
            scope.$digest();

            controller = element.controller('inventoryTile');
        };
    }));

    afterEach(() => {
        scope.$destroy();
        element.remove();
    });

    describe('Render', () => {
        it('should render the tile', inject(($filter) => {
            //given
            var momentize = $filter('TDPMoment');

            //when
            createElement();
            let tile = element.find('.preparation');

            //then
            let descriptionUser = tile.eq(0).find('.description').eq(0).find('span').text();
            let descriptionSince = tile.eq(0).find('.description').text();
            let datasetName = tile.eq(0).find('.details').eq(0).find('.name').text();
            let datasetRowsDetails = tile.eq(0).find('.details').eq(0).text();
            let stepsDetails = tile.eq(0).find('.details').eq(1).text();

            expect(descriptionUser).toContain('anonymousUser'); //owner
            expect(descriptionSince).toContain(momentize('1427447300300')); //lastModification date ago
            expect(datasetName).toContain('customers_jso_light'); //dataset name
            expect(datasetRowsDetails).toContain('15'); //dataset nb records
            expect(stepsDetails).toContain('3'); //steps

            let favIcon = tile.find('.favorite');
            let cloneIcon = tile.find('.clone-btn').eq(0);
            let removeIcon = tile.find('.trash');
            expect(favIcon.length).toBe(1);
            expect(cloneIcon.length).toBe(1);
            expect(removeIcon.length).toBe(1);
        }));
    });

    describe('Mouse Events', () => {
        let tile;
        beforeEach(() => {
            createElement();
            tile = element.find('.preparation').eq(0);

            controller.onClone = jasmine.createSpy('onClone');
            controller.onFavorite = jasmine.createSpy('onFavorite');
            controller.onRemove = jasmine.createSpy('onRemove');
            controller.onRename = jasmine.createSpy('onRename');
            controller.onTileClick = jasmine.createSpy('onTileClick');
            controller.onTitleClick = jasmine.createSpy('onTitleClick');
        });

        it('should call onClone callback', () => {
            //given
            let cloneIcon = tile.find('.clone-btn').eq(0);

            //when
            scope.$digest();
            cloneIcon.click();

            //then
            expect(controller.onClone).toHaveBeenCalledWith({preparation: controller.preparation});
        });

        it('should call onFavorite callback', () => {
            //given
            let favIcon = tile.find('.favorite').eq(0);

            //when
            scope.$digest();
            favIcon.click();

            //then
            expect(controller.preparation.favorite).toBe(true);
        });

        it('should call onRemove callback', () => {
            //given
            let removeIcon = tile.find('.trash').eq(0);

            //when
            scope.$digest();
            removeIcon.click();

            //then
            expect(controller.onRemove).toHaveBeenCalledWith({preparation: controller.preparation});
        });

        it('should call onTileClick callback', () => {
            //when
            scope.$digest();
            tile.click();

            //then
            expect(controller.onTileClick).toHaveBeenCalledWith({preparation: controller.preparation});
        });

        it('should call onTitleClick callback', () => {
            //given
            let titleIcon = tile.find('.one-line').eq(0);

            //when
            scope.$digest();
            titleIcon.click();

            //then
            expect(controller.onTitleClick).toHaveBeenCalledWith({preparation: controller.preparation});
        });

        //it('should show hidden icons on mouseover', () => {
        //    //given
        //    console.log(tile.find('.clone-btn').css('display'));
        //    expect(tile.find('.trash').eq(0).css('display')).toEqual('none');
        //
        //    //when
        //    scope.$digest();
        //    tile.mouseover();
        //
        //    //then
        //    expect(tile.find('.trash').eq(0).css('display')).toEqual('block');
        //});

        //it('should show hidden icons on mouseover', () => {
        //    //when
        //    scope.$digest();
        //    tile.mouseover();
        //
        //    //then
        //    expect(tile.css('background-color')).toEqual('block');
        //});
    });
});