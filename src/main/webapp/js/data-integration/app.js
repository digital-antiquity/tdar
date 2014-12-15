(function($, angular, console){
    "use strict";

    var app = angular.module('integrationApp', ['angularModalService']);

    //top-level controller for the integration viewmodel
    app.controller('IntegrationController', ['$scope', 'ModalService',  '$http', 'IntegrationService', '$q',  function($scope, ModalService, $http, integration, $q){
        var self = this,
            openModal;



        //controller public fields
        self.integration = integration;
        self.tab = 0;
        self.sharedOntologies = [];

        //fixme: remove later, expose viewmodel for debugging
        window.__viewModel = self.integration;


        //'private' methods
        openModal = function(options) {
            ModalService.showModal({
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
            integration.columns.splice(idx, 1);
        }

        self.saveClicked = function() {
            console.log("Saving.")
            console.log(JSON.stringify(integration, null, 4));
        };

        /**
         * Called after user selects list of dataset id's from 'add datasets' modal.
         * @param datasetIds
         */
        self.addDatasets = function(datasetIds) {
            if(datasetIds.length === 0) return;
            //TODO: create recursive 'unroll' function that emits params in struts-friendly syntax
            $http.get('/workspace/ajax/table-details?' + $.param({dataTableIds: datasetIds}, true)
            ).success(function(data) {

                //FIXME: replace direct array minipulation with model add/remove methods so that we know when to update functional dependencies
                self.integration.addDatatables(data[0].dataTables);
                self.integration.updateSharedOntologies(data[0].sharedOntologies);

                //gather the updated node participation data
                $scope.loadIntegrationColumnDetails(self.integration);
            });

        };

        //add and initialize an integration column associated with with the specified ontology ID
        var processAddedIntegrationColumns = function(ontologies) {
            console.debug("processAddedIntegrationColumns ::");
            ontologies.forEach(function(ontology){
                self.integration.addIntegrationColumn('intcol' + ontology.id, ontology);
            });
        };


        self.findDataTableColumns = function(integrationColumn) {
            var cols = [];
            var ontologyId = integrationColumn.ontologyId;
            //troll through each column of each datatable and determine which ones match the specified ontology in the integrationColumn
            self.integration.datatables.forEach(function(table) {
                console.debug("table:%s", table.display_name);
                table.columns.forEach(function(col){
                    if(ontologyId === col.id) {
                        console.log("\t matching ontology:%s table:%s  column:%s", ontologyId, table.display_name, col.name);
                        cols.push({
                            id: col.id,
                            name: col.name,
                            dataTableColumn: col,
                            dataTable: table
                        });
                    }
                });
            });
            return cols;
        };

        self.integrateClicked = function() {
            console.debug('integrate clicked');
            //FIXME: HACK: NEVERDOTHIS: This is absolutely not the correct way to invoke a form submission, for a number of reasons.
            $("#btnSubmitLegacyForm").click();

        };

        self.addDatasetsClicked = function(arg) {
            console.debug('Add Datasets clicked');
            openModal({
                title: "Add Datasets",
                searchType: "dataset",
                url: "/workspace/ajax/find-datasets",
                transformData: function(data) {
                    return data.map(function(item){
                        var result = item;
                        result.title = item.dataset_name;
                        result.title += ' - ' + item.data_table_name;
                        result.id = item.data_table_id;
                        result.submitter_display_name  = item.dataset.submitter.properName;
                        result.date_registered = item.dataset_date_created;
                        return result;
                    })
                },
                close: function(data) {
                    console.debug("datasetsClicked.close::");
                    self.addDatasets(data);
                }

            });
        };

        self.addIntegrationColumnsClicked = function(arg) {
            console.debug('Add Integration Columns Clicked');
            openModal({
                title: "Add Ontologies",
                searchType: "ontology",
                url: "/workspace/ajax/find-ontologies",
                categoryFilter: true,
                close: function(data) {
                    self.addIntegrationColumns(data);
                }
            });
        };

        self.addDisplayColumnClicked = function(arg) {
            integration.addDisplayColumn("display column");
        };

        self.removeSelectedDatasetClicked = function() {
            integration.removeDatatables($scope.selectedDatatables);
            _viewModelDirty();
        };

        self.addIntegrationColumnsMenuItemClicked = function(ontology) {
            self.integration.addIntegrationColumn(ontology.name, ontology);
        }

        $scope.lookupCompatibleColumns = function(id) {
            return self.integration.mappedDatatables[id];
        };

        $scope.loadIntegrationColumnDetails = function(integration) {
            //get column participation for all dataTableColumns across all shared ontologies
            var promises = [];
            var configs = [];

            integration.ontologies.forEach(function(ontology){
                var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                var config = {};
                var params = {
                    "integrationColumn.columnType": "INTEGRATION",
                    "integrationColumn.sharedOntology.id": ontology.id
                };
                mappedCols.forEach(function(mappedCol, i){
                    params["integrationColumn.columns[" + i + "].id"] = mappedCol.id
                });
                config.params = params;
                configs.push(config);
                promises.push($http.get("/workspace/ajax/integration-column-details", config));
            });
            $q.all(promises).then(function(arResults){
                arResults.forEach(function(result, ontologyIdx){
                    var ontology = integration.ontologies[ontologyIdx];
                    var mappedCols = integration.getMappedDatatableColumns(ontology.id);
                    integration.updateNodeParticipationInfo(ontology, mappedCols, result.data);
                });
            }) ;
        }

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

        /**
         * If the user modifies the list of datatables it will put the viewModel in an inconsistent state.   This method returns the viewModel to a consistent
         * state with as little destruction as possible.
         *
         * @private
         */
        var _viewModelDirty = function() {
            console.log('viewModelDirty::');
            self.integration.ontologies.length = 0;

            //hack: rebuild shared ontologies.
            var datasetIds = [];
            datasetIds = self.integration.datatables.map(function(dt){return dt.data_table_id});
            $http.get('/workspace/ajax/table-details?' + $.param({dataTableIds: datasetIds}, true))
                    .success(function(data){
                        console.debug("resetting list of shared ontologies");
                        console.table(data[0].sharedOntologies);
                        self.integration.ontologies = data[0].sharedOntologies;
                    });

            //TODO: handle integration columns for ontologies that are no longer shared (e.g. if a user adds a datatable)
            self.integration.columns = []
            self.setTab(0);

            //TODO: handle display column when source datatableColumn no longer exists
        };


    }]);

/* global jQuery, angular  */
})(jQuery, angular, console);
