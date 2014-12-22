(function($, angular, console){
    "use strict";
    console.debug("IntegrationController::");


    var app = angular.module('integrationApp');

    //top-level controller for the integration viewmodel
    app.controller('IntegrationController', ['$scope', 'ModalService',  'IntegrationService', 'DataService', function($scope, ModalService, integration, dataService){
        var self = this,
            _openModal,
            _processAddedIntegrationColumns;

        //controller public fields
        self.integration = integration;
        self.tab = 0;
        self.sharedOntologies = [];

        ////fixme: remove later, expose viewmodel for debugging
        window.__viewModel = self.integration;

        _openModal = function(options) {
            ModalService.showModal({
                //Note: there is no file w/ this name. Angular first looks for partials in DOM elements w/ id specified by templateUrl.
                templateUrl: "workspace/modal-dialog.html",
                controller: "ModalDialogController",
                inputs: {
                    options: $.extend({categoryFilter: false}, options)
                }
            }).then(function(modal){
                //model.element is a jqSelection containing the top-level element for this control (e.g. $("#modalContainer")
                modal.element.modal();

                //model.close is a promise that is resolved when the modal closes
                modal.close.then(function(result){
                    //modal.element.modal('hide');
                    console.log("modal destroyed  result:%s", result);
                    $scope.message = result ? "result was yes" : "result was no";

                    if(options.close) {
                        options.close(result);
                    }
                });

                //if the service cannot create the modal or catches an exception, the promise calls an error callback
            }).catch(function(error) {
                console.error("ModalService error: %s", error);
            });
        };

        //controller public methods
        self.setTab  = function(idx) {
            this.tab = idx;
        }

        self.isTabSet = function(idx) {
            return this.tab === idx;
        }

        self.closeTab = function(idx) {
            integration.removeOutputColumn(idx);
        }

        self.saveClicked = function() {
            console.log("Saving.")
            console.log(JSON.stringify(integration, null, 4));
        };

        /**
         * Called after user selects list of dataset id's from 'add datasets' modal.
         * @param datatableIds
         */
        self.addDatasets = function(datatableIds) {
            if(datatableIds.length === 0) return;
            //TODO: create recursive 'unroll' function that emits params in struts-friendly syntax
            dataService.loadTableDetails(datatableIds).success(function(data) {
                self.integration.addDatatables(data.dataTables);
                //self.integration.updateSharedOntologies(data[0].sharedOntologies);
                //gather the updated node participation data
                $scope.loadIntegrationColumnDetails(self.integration);
            });
        };

        //add and initialize an integration column associated with with the specified ontology ID
        _processAddedIntegrationColumns = function(ontologies) {
            console.debug("_processAddedIntegrationColumns ::");
            ontologies.forEach(function(ontology){
                self.integration.addIntegrationColumn('intcol' + ontology.id, ontology);
            });
        };

        self.integrateClicked = function() {
            console.debug('integrate clicked');
            //FIXME: HACK: NEVERDOTHIS: This is absolutely not the correct way to invoke a form submission, for a number of reasons.
            $("#btnSubmitLegacyForm").click();

        };

        self.addDatasetsClicked = function(arg) {
            console.debug('Add Datasets clicked');
            _openModal({
                title: "Add Datasets",
                searchType: "dataset",
                close: function(data) {
                    self.addDatasets(data);
                }

            });
        };

        self.addIntegrationColumnsClicked = function(arg) {
            console.debug('Add Integration Columns Clicked');
            _openModal({
                title: "Add Ontologies",
                searchType: "ontology",
                categoryFilter: true,
                close: function(data) {
                    self.addIntegrationColumns(data);
                }
            });
        };

        self.addDisplayColumnClicked = function(arg) {
            integration.addDisplayColumn("display column");
        };

        self.addCountColumnClicked = function(arg) {
            integration.addCountColumn("count column");
        };

        self.removeSelectedDatasetClicked = function() {
            integration.removeDatatables($scope.selectedDatatables);
        };

        self.addIntegrationColumnsMenuItemClicked = function(ontology) {
            self.integration.addIntegrationColumn(ontology.title, ontology);
        }

        self.isCountColumnDisabled = function() {
            return !self.integration.isCountColumnEnabled();
        };
        
        $scope.filterCount = function(col) {
            if(col.columnEncodingType =='COUNT')
            {
                return true; // this will be listed in the results
            }

            return false; // otherwise it won't be within the results
        };

        $scope.lookupCompatibleColumns = function(id) {
            return self.integration.mappedDatatables[id]
            //FIXME: angular doesn't like my getter functions - apparently they cause endless loop of detected changes (or something?)
            //https://docs.angularjs.org/error/$rootScope/infdig
            //return (self.integration.getMappedDatatables()[id]);
        };

        $scope.loadIntegrationColumnDetails = function(integration) {
            dataService.loadIntegrationColumnDetails(integration);
        };

        //FIXME: proper validation required
        $scope.isValid = function() {
            console.log("isValid:: %s", self.integration.columns.length);
            return self.integration.columns.length > 0;
        }

        /**
         * convenience method:  does the current ontologyNodeValue occur in the current datatableColumn.
         *
         * @param datatableColumnId
         */
        $scope.ontologyValuePresent = function(ontologyId, nodeIdx, dtcId) {
            var ontologyParticipation = self.integration.ontologyParticipation[ontologyId];
            var nodeInfo = ontologyParticipation.nodeInfoList[nodeIdx];
            return nodeInfo.colIds.indexOf(dtcId) > -1;
        }

    }]);

    /* global jQuery, angular  */
})(jQuery, angular, console);
