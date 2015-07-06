describe('Datagrid size service', function () {
    'use strict';

    var gridMock;

    var storageKey = 'dataprep.col_size_00000000';

    var gridColumns;

    beforeEach(module('data-prep.datagrid'));

    beforeEach(inject(function ($window, DatagridService) {
        /*global SlickGridMock:false */
        gridMock = new SlickGridMock();
        gridColumns = [
            {id: '0000', name: 'col0', width: 10, minWidth: 80},
            {id: '0001', name: 'col1', width: 20, minWidth: 80},
            {id: '0002', name: 'col2', width: 30, minWidth: 80},
            {id: '0003', name: 'col3', width: 40, minWidth: 80},
            {id: '0004', name: 'col4', width: 50, minWidth: 80}
        ];
        DatagridService.metadata = {id: '00000000'};

        spyOn(gridMock.onColumnsResized, 'subscribe').and.returnValue();
        spyOn(gridMock, 'resizeCanvas').and.returnValue();
        spyOn(gridMock, 'autosizeColumns').and.returnValue();
        spyOn($window, 'addEventListener').and.returnValue();
    }));

    afterEach(inject(function($window) {
        $window.localStorage.removeItem(storageKey);
    }));

    describe('on creation', function() {
        it('should add grid column resize listener', inject(function (DatagridSizeService) {
            //when
            DatagridSizeService.init(gridMock);

            //then
            expect(gridMock.onColumnsResized.subscribe).toHaveBeenCalled();
        }));

        it('should add window resize listener', inject(function ($window, DatagridSizeService) {
            //when
            DatagridSizeService.init(gridMock);

            //then
            expect($window.addEventListener).toHaveBeenCalled();
            var args = $window.addEventListener.calls.argsFor(0);
            expect(args[0]).toBe('resize');
            expect(args[2]).toBe(true);
        }));
    });

    describe('on event', function() {
        it('should resize grid canvas when window is resized', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);
            var resizeHandler = $window.addEventListener.calls.argsFor(0)[1];

            //when
            resizeHandler();

            //then
            expect(gridMock.resizeCanvas).toHaveBeenCalled();
        }));

        it('should save sizes in localStorage when column is resized', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);
            var resizeHandler = gridMock.onColumnsResized.subscribe.calls.argsFor(0)[0];

            expect($window.localStorage.getItem(storageKey)).toBeFalsy();

            //when
            resizeHandler();

            //then
            expect($window.localStorage.getItem(storageKey)).toBe(JSON.stringify({
                '0000': 10,
                '0001': 20,
                '0002': 30,
                '0003': 40,
                '0004': 50
            }));
        }));
    });

    describe('auto size columns', function() {
        it('should auto resize columns and save width in localStorage when no size have been saved yet', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);

            expect($window.localStorage.getItem(storageKey)).toBeFalsy();

            //when
            DatagridSizeService.autosizeColumns();

            //then
            expect(gridMock.autosizeColumns).toHaveBeenCalled();
            expect($window.localStorage.getItem(storageKey)).toBe(JSON.stringify({
                '0000': 10,
                '0001': 20,
                '0002': 30,
                '0003': 40,
                '0004': 50
            }));
        }));

        it('should set column sizes from localStorage', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);

            $window.localStorage.setItem(storageKey, JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90,
                '0004': 100
            }));

            expect(gridMock.getColumns()[0].width).toBe(10);
            expect(gridMock.getColumns()[1].width).toBe(20);
            expect(gridMock.getColumns()[2].width).toBe(30);
            expect(gridMock.getColumns()[3].width).toBe(40);
            expect(gridMock.getColumns()[4].width).toBe(50);

            //when
            DatagridSizeService.autosizeColumns();

            //then
            expect(gridMock.autosizeColumns).not.toHaveBeenCalled();
            expect($window.localStorage.getItem(storageKey)).toBe(JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90,
                '0004': 100
            }));
            expect(gridMock.getColumns()[0].width).toBe(60);
            expect(gridMock.getColumns()[1].width).toBe(70);
            expect(gridMock.getColumns()[2].width).toBe(80);
            expect(gridMock.getColumns()[3].width).toBe(90);
            expect(gridMock.getColumns()[4].width).toBe(100);
        }));

        it('should set column min width when this column is not in the localStorage entries', inject(function ($window, DatagridSizeService) {
            //given
            gridMock.initColumnsMock(gridColumns);
            DatagridSizeService.init(gridMock);

            $window.localStorage.setItem(storageKey, JSON.stringify({
                '0000': 60,
                '0001': 70,
                '0002': 80,
                '0003': 90
                //missing 0004 entry
            }));

            expect(gridMock.getColumns()[4].width).toBe(50);

            //when
            DatagridSizeService.autosizeColumns();

            //then
            expect(gridMock.getColumns()[4].width).toBe(80);
        }));
    });
});