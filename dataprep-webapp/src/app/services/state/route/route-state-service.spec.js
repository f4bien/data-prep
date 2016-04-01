/*  ============================================================================

  Copyright (C) 2006-2016 Talend Inc. - www.talend.com

  This source code is available under agreement available at
  https://github.com/Talend/data-prep/blob/master/LICENSE

  You should have received a copy of the agreement
  along with this program; if not, write to Talend SA
  9 rue Pages 92150 Suresnes, France

  ============================================================================*/

describe('Route state service', () => {
    'use strict';

    beforeEach(angular.mock.module('data-prep.services.state'));

    describe('previous', () => {
        it('should init previous route', inject((routeState) => {
            //then
            expect(routeState.previous).toBe('nav.index.datasets');
            expect(routeState.previousOptions).toEqual({folderPath: ''});
        }));

        it('should set previous route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = {opt: 'my options'};

            expect(routeState.previous).not.toBe(previous);
            expect(routeState.previousOptions).not.toBe(previousOptions);

            //when
            RouteStateService.setPrevious(previous, previousOptions);

            //then
            expect(routeState.previous).toBe(previous);
            expect(routeState.previousOptions).toBe(previousOptions);
        }));

        it('should NOT change previous route if it is falsy', inject((routeState, RouteStateService) => {
            //given
            const previous = '';
            const previousOptions = {opt: 'my options'};

            const originalPrevious = 'toto';
            const originalPreviousOptions = {};

            routeState.previous = originalPrevious;
            routeState.previousOptions = originalPreviousOptions;

            //when
            RouteStateService.setPrevious(previous, previousOptions);

            //then
            expect(routeState.previous).toBe(originalPrevious);
            expect(routeState.previousOptions).toBe(originalPreviousOptions);
        }));

        it('should reset previous route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = {opt: 'my options'};

            routeState.previous = previous;
            routeState.previousOptions = previousOptions;

            //when
            RouteStateService.resetPrevious();

            //then
            expect(routeState.previous).toBe('nav.index.datasets');
            expect(routeState.previousOptions).toEqual({folderPath: ''});
        }));
    });

    describe('next', () => {
        it('should init next route', inject((routeState) => {
            //then
            expect(routeState.next).toBe('nav.index.datasets');
            expect(routeState.nextOptions).toEqual({folderPath: ''});
        }));

        it('should set next route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = {opt: 'my options'};

            expect(routeState.next).not.toBe(previous);
            expect(routeState.nextOptions).not.toBe(previousOptions);

            //when
            RouteStateService.setNext(previous, previousOptions);

            //then
            expect(routeState.next).toBe(previous);
            expect(routeState.nextOptions).toBe(previousOptions);
        }));

        it('should NOT change next route when it is falsy', inject((routeState, RouteStateService) => {
            //given
            const previous = '';
            const previousOptions = {};

            const originalPrevious = 'previous.route';
            const originalPreviousOptions = {opt: 'my options'};

            routeState.next = originalPrevious;
            routeState.nextOptions = originalPreviousOptions;

            //when
            RouteStateService.setNext(previous, previousOptions);

            //then
            expect(routeState.next).toBe(originalPrevious);
            expect(routeState.nextOptions).toBe(originalPreviousOptions);
        }));

        it('should reset next route', inject((routeState, RouteStateService) => {
            //given
            const previous = 'previous.route';
            const previousOptions = {opt: 'my options'};

            routeState.next = previous;
            routeState.nextOptions = previousOptions;

            //when
            RouteStateService.resetNext();

            //then
            expect(routeState.next).toBe('nav.index.datasets');
            expect(routeState.nextOptions).toEqual({folderPath: ''});
        }));
    });
});
