describe('Filter search directive', function() {
    'use strict';
    
    var scope, createElement, element;

    beforeEach(module('data-prep.filter-search'));
    beforeEach(module('htmlTemplates'));

    beforeEach(inject(function($rootScope, $compile) {
        scope = $rootScope.$new();
        createElement = function() {
            element = angular.element('<filter-search></filter-search>');
            $compile(element)(scope);
            scope.$digest();
        };
    }));

    afterEach(function() {
        scope.$destroy();
        element.remove();
    });

    it('should render input with auto-complete', function() {
        //when
        createElement();

        //then
        expect(element.find('div[mass-autocomplete]').length).toBe(1);
        expect(element.find('input[type="search"]').length).toBe(1);
    });

    it('should stop propagation on ESC key down', function () {
        //given
        createElement();

        var bodyEscEvent = false;
        var escEventListener = function(event) {
            if(event.keyCode === 27) {
                bodyEscEvent = true;
            }
        };
        var body = angular.element('body');
        body.append(element);
        body.keydown(escEventListener);

        var event = angular.element.Event('keydown');
        event.keyCode = 27;

        //when
        element.find('input[type="search"]').eq(0).trigger(event);
        scope.$digest();

        //then
        expect(bodyEscEvent).toBe(false);

        //finally
        body.off('keydown', escEventListener);
    });

    it('should propagate on key down other than ESC', function () {
        //given
        createElement();

        var bodyEnterEvent = false;
        var escEventListener = function(event) {
            if(event.keyCode === 13) {
                bodyEnterEvent = true;
            }
        };
        var body = angular.element('body');
        body.append(element);
        body.keydown(escEventListener);

        var event = angular.element.Event('keydown');
        event.keyCode = 13;

        //when
        element.find('input[type="search"]').eq(0).trigger(event);
        scope.$digest();

        //then
        expect(bodyEnterEvent).toBe(true);

        //finally
        body.off('keydown', escEventListener);
    });
});