/* global describe, it, expect, beforeEach, jasmine */

describe('IntegrationController', function() {
    "use strict";

    var $controller, $httpBackend;


    // this request just spits out the details of the request
    function _logRequestHandler(method, url, data, headers, params){
        console.log('method:', method);
        console.log('url:', url);
        console.log('data:', data);
        console.log('headers:', headers);
        console.log('params:', params);
        return [500, {foo: 'bar'}]
    }

    beforeEach(module('integrationApp'));
    beforeEach(inject(function(_$controller_, _$httpBackend_){
        $controller = _$controller_;
        $httpBackend = _$httpBackend_;

        //let's mock some of the requests that we know dataservice will make  (some of these tests should probably move to dataservice spec)
        $httpBackend.whenGET(/api\/integration\/table-details/).respond(_logRequestHandler);
        $httpBackend.whenGET(/api\/integration\/node-participation/).respond(_logRequestHandler);
        $httpBackend.whenGET(/api\/integration\/ontology-details/).respond(_logRequestHandler);


    }));

    describe('basic fields', function() {
        it('has basic fields defiend', function() {
            var $scope = {};
            var ctrl = $controller('IntegrationController', {$scope: $scope});
            expect($httpBackend).toBeDefined();
            expect(ctrl.tab).toBeDefined();
            expect($scope.alert).toBeDefined();
            expect($scope.alert.kind).toBe('default');
        });
    });

    describe("lookupCompatibleColumns", function() {

        it(" should be empty when nothing loaded", function() {
            var $scope = {};
            var ctrl = $controller('IntegrationController', {$scope: $scope});


            //todo: here's what we want to simulate

            // add dataset 1 (has ontologies o1, o2)
            ctrl.addDatasets([1]);
            $httpBackend.flush();

            // add dataset 2 (has ontologies o1)

            // add an integration column for o1

            // check to make sure that lookupCompatibleColumns(o1.id) has two columns in it (dataset 1, dataset 2)


            // add integration column for o2

            // check to make sure that lookupCompatibleColumns(02.id) has *one* column in it (dataset1);

        });



    })

});