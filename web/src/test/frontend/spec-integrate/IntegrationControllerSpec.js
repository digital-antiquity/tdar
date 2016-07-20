/* global describe, it, expect, beforeEach, jasmine */

describe('IntegrationController', function() {
    "use strict";

    var $controller;

    beforeEach(module('integrationApp'));
    beforeEach(inject(function(_$controller_){
        $controller = _$controller_;
    }));

    describe('basic fields', function() {
        it('has basic fields defiend', function() {
            var $scope = {};
            var ctrl = $controller('IntegrationController', {$scope: $scope});
            expect(ctrl.tab).toBeDefined();
            expect($scope.alert).toBeDefined();
            expect($scope.alert.kind).toBe('default');


        });
    });

});