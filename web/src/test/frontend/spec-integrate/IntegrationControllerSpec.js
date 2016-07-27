/* global describe, it, expect, beforeEach, jasmine */

describe('IntegrationController', function() {
    "use strict";

    var $controller, $httpBackend, dataService;


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
    beforeEach(inject(function(_$controller_, _$httpBackend_, _DataService_){
        $controller = _$controller_;
        $httpBackend = _$httpBackend_;
        dataService = _DataService_;
        var _responseData = {
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
                    mappedOntologies:[
                        {
                            title: 'o1',
                            description: 'ontology one',
                            id: 1,
                            nodes: [
                                {
                                    displayName: 'o1 node1',
                                    index: '1',
                                    iri: 'o1_node1',
                                    id: 1

                                },
                                {
                                    displayName: 'o1 node2',
                                    index: '2',
                                    iri: 'o1_node2',
                                    id: 2

                                },

                            ]
                        },
                        {
                            title: 'o2',
                            description: 'ontology two',
                            id: 2,
                            nodes: [
                                {
                                    displayName: 'o2 node1',
                                    index: '1',
                                    iri: 'o2_node1',
                                    id: 3

                                },
                                {
                                    displayName: 'o2 node2',
                                    index: '1.1',
                                    iri: 'o2_node2',
                                    id: 4

                                }
                            ]
                        }

                    ]

                },
                '2': {
                    dataTables:[{
                        displayName: 'table two',
                        datasetTitle: 'dataset two',
                        id: '2',
                        dataTableColumns:[
                            {
                                "name": "t1c1",
                                "displayName": "column one",
                                "columnEncodingType": "CODED_VALUE",
                                "mappedOntologyId": 1,
                                "id": 3
                            }
                        ]
                    }],
                    mappedOntologies:[
                        {
                            title: 'o1',
                            description: 'ontology one',
                            id: 1,
                            nodes: [
                                {
                                    displayName: 'o1 node1',
                                    index: '1',
                                    iri: 'o1_node1',
                                    id: 1

                                },
                                {
                                    displayName: 'o1 node2',
                                    index: '2',
                                    iri: 'o1_node2',
                                    id: 2

                                }
                            ]
                        }
                    ]
                }
            },
            nodeParticipation: {
                //participation for table 1, column 1
                '1': {
                    dataTableColumn: {
                        name: 't1c1',
                        id: 1
                    },
                    sharedOntology: {
                        title: 'o1',
                        id: 1
                    },
                    flattenedNodes: [
                        {iri:'o1_node1', id:'1'},
                        {iri:'o1_node2', id:'2'}
                    ]
                },

                //participation for table 1, column 2
                '2': {
                    dataTableColumn: {
                        name: 't1c2',
                        id: 2
                    },
                    sharedOntology: {
                        title: 'o2',
                        id: 2
                    },
                    flattenedNodes: [
                        {iri:'o2_node1', id:'3'}
                    ]
                },

                //participation for table 1, column 2
                '3': {
                    dataTableColumn: {
                        name: 't2c1',
                        id: 3
                    },
                    sharedOntology: {
                        title: 'o1',
                        id: 1
                    },
                    flattenedNodes: [
                        {iri:'o1_node1', id:'1'}
                    ]
                }
            }

        };

        //let's mock some of the requests that we know dataservice will make  (some of these tests should probably move to dataservice spec)
        $httpBackend.whenPOST(/api\/integration\/node-participation/).respond(logAndRespond([200, [
            _responseData.nodeParticipation['1'], _responseData.nodeParticipation['2']
        ]]));
        $httpBackend.whenGET(/api\/integration\/ontology-details/).respond(logAndRespond([200, {}]));
        console.log('data.tableDetails', _responseData.tableDetails);
        //$httpBackend.whenGET(/api\/integration\/table-details/).respond(logAndRespond([200, _responseData.tableDetails[1]]))
        $httpBackend.whenGET(/api\/integration\/table-details/).respond(
            function(method, url, requestData, headers, params){
                return [200, _responseData.tableDetails[params.dataTableIds]]
            }
        );



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
            ctrl.addDatasets([2]);
            $httpBackend.flush();

            // add an integration column for o1
            //fixme: possible bug:  the objects returned from dataService.getCachedOntologies([) are not same as controller.integration.ontologies (latter has more fields)
            //fixme: possible bug: after adding tables, then adding integration column, shouldn't ontology-details have been called by now?
            //fixme: could be that we need to simulate the "add datasets" modal close callback?
            expect(dataService.getCachedOntologies(['1'])).toBeDefined();
            var o1 = dataService.getCachedOntologies(['1']);
            ctrl.addIntegrationColumnsMenuItemClicked(o1);
            $httpBackend.flush();


            // check to make sure that lookupCompatibleColumns(o1.id) has two columns in it (dataset 1, dataset 2)


            // add integration column for o2

            // check to make sure that lookupCompatibleColumns(02.id) has *one* column in it (dataset1);

        });



    })

});