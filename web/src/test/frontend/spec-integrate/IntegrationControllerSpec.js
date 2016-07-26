/* global describe, it, expect, beforeEach, jasmine */

describe('IntegrationController', function() {
    "use strict";

    var $controller, $httpBackend;


    // this request just spits out the details of the request
    function logAndRespond(respondArgs) {
        return function _logRequestHandler(method, url, data, headers, params){
            console.log('method:', method);
            console.log('url:', url);
            console.log('data:', data);
            console.log('headers:', headers);
            console.log('params:', params);
            return respondArgs;
        }
    }

    beforeEach(module('integrationApp'));
    beforeEach(inject(function(_$controller_, _$httpBackend_){
        $controller = _$controller_;
        $httpBackend = _$httpBackend_;
        var data = {
            tableDetails: {
                '1': {
                    dataTables:[{
                        displayName: 'table one',
                        datasetTitle: 'dataset one',
                        id: '1',
                        dataTableColumns:[
                            {
                                "name": "t1c1",
                                "displayName": "column one",
                                "columnEncodingType": "CODED_VALUE",
                                "mappedOntologyId": 1,
                                "id": 1
                            },
                            {
                                "name": "t1c2",
                                "displayName": "column two",
                                "columnEncodingType": "CODED_VALUE",
                                "mappedOntologyId": 2,
                                "id": 2
                            }
                            
                        ]
                    }],
                    mappedOntologies:[]

                }
            }
        };

        //let's mock some of the requests that we know dataservice will make  (some of these tests should probably move to dataservice spec)
        $httpBackend.whenPOST(/api\/integration\/node-participation/).respond(logAndRespond([200, []]));
        $httpBackend.whenGET(/api\/integration\/ontology-details/).respond(logAndRespond([200, {}]));
        $httpBackend.whenGET(/api\/integration\/table-details/).respond(logAndRespond([200, data.tableDetails['1']]));
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