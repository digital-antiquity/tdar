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

        
        self.loadJSON = function() {
            var json = dataService.loadExistingIntegration(dataService.getDocumentData().jsondata , self.integration);
         };
        
        /**
         * Called after user selects list of dataset id's from 'add datasets' modal.
         * @param dataTableIds
         */
        self.addDatasets = function(dataTableIds) {
            if(dataTableIds.length === 0) return;
            dataService.loadTableDetails(dataTableIds).then(function(dataTables) {
                self.integration.addDataTables(dataTables);
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
            self.setTab(integration.columns.length -1);
        };

        self.addCountColumnClicked = function(arg) {
            integration.addCountColumn("count column");
            self.setTab(integration.columns.length -1);
        };

        self.removeSelectedDatasetClicked = function() {
            integration.removeDataTables($scope.selectedDataTables);
        };

        self.addIntegrationColumnsMenuItemClicked = function(ontology) {
            self.integration.addIntegrationColumn(ontology.title, ontology);
            self.setTab(integration.columns.length -1);
        }

        self.isCountColumnDisabled = function() {
            return !self.integration.isCountColumnEnabled();
        };

        /**
         * Output column filter: show only 'count' columns
         * @param col
         * @returns {boolean}
         */
        $scope.filterCount = function(col) {
            return col.columnEncodingType === 'COUNT';
        };

        $scope.lookupCompatibleColumns = function(id) {
            return self.integration.mappedDataTables[id];
            //FIXME: angular doesn't like my getter functions - apparently they cause endless loop of detected changes (or something?)
            //https://docs.angularjs.org/error/$rootScope/infdig
            //return (self.integration.getMappedDataTables()[id]);
        };

        //FIXME: proper validation required
        $scope.isValid = function() {
            console.log("isValid:: %s", self.integration.columns.length);
            return self.integration.columns.length > 0;
        }

        /**
         * convenience method:  does the current ontologyNodeValue occur in the current dataTableColumn.
         *
         * @param dataTableColumnId
         */
        $scope.ontologyValuePresent = function(dataTableColumn, ontology) {
            return self.integration.isNodePresent(dataTableColumn, ontology);
        }

        /**
         *
         *
         * @param criteria "some" or "every"
         */
        $scope.selectMatchingNodes = function(criteria) {
            var integrationColumn = self.integration.columns[self.tab];
            var nodeSelections = integrationColumn.nodeSelections;
            nodeSelections.forEach(function(selectionInfo){
                var ontologyNode = selectionInfo.node;
                var mappedColumns = self.integration.getMappedDataTableColumns(integrationColumn.ontologyId)
                selectionInfo.selected = selectionInfo.selected ||  mappedColumns[criteria](function(dtc){
                    return self.integration.isNodePresent(dtc, ontologyNode)
                });
            });

        };

    }]);

    /* global jQuery, angular  */
})(jQuery, angular, console);
