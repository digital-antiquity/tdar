(function($, angular, console){
    "use strict";
    var app,_projects, _collections, _categories, _documentData;
    app = angular.module('integrationApp', ['angularModalService']);


    //Our integration "Model" object.
    function Integration() {
        var self = this;
        self.title = "";
        self.description = "";
        self.columns = [];
    }

    //model for search filter that can be used for datasets or ontologies
    function SearchFilter() {
        var self = this;
        $.extend(self, {
            title: "",
            projectId: null,
            collectionId: null,
            categoryId: null,
            unbookmarked: false,
            incompatible: false
        });
    }

    //read json embedded in script elements
    function _loadDocumentData() {
        var dataElements = $('[type="application/json"][id]').toArray();
        var map = {};
        dataElements.forEach(function(elem){
            var key = elem.id;
            var val = JSON.parse(elem.innerHTML);
            map[key] = val;
        });
        return map;
    }

    _documentData = _loadDocumentData();


    //top-level controller for the integration viewmodel
    app.controller('IntegrationCtrl', ['$scope', 'ModalService', function($scope, ModalService){
        var self = this,
            integration = new Integration(),
            openModal;

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


        this.integration = integration;

        this.tab = 0;

        this.setTab  = function(idx) {
            this.tab = idx;
        }

        this.isTabSet = function(idx) {
            return this.tab === idx;
        }

        this.closeTab = function(idx) {
            integration.columns.splice(idx, 1);
        }

        this.saveClicked = function() {
            console.log("Saving.")
            console.log(JSON.stringify(integration, null, 4));
        };

        this.addDatasets = function(datsetIds) {

        };

        this.addIntegrationColumns = function(ontologyIds) {

        };

        this.integrateClicked = function() {
            console.log('integrate clicked');
        };

        this.addDatasetsClicked = function(arg) {
            console.log('Add Datasets clicked');
            openModal({
                title: "Add Datasets",
                searchType: "dataset",
                url: "/workspace/ajax/find-datasets",
                transformData: function(data) {
                    return data.map(function(item){
                        var result = item;
                        result.title = item.dataset_title;
                        if(item.sibling_count > 1) {
                            result.title += ' - ' + item.display_name;
                        }
                        result.id = item.data_table_id;
                        result.submitter_display_name  = item.submitter_display_name;
                        result.date_registered = item.date_registered;
                        return result;
                    })
                },
                close: function(data) {
                    console.debug("datasetsClicked.close::");
                    self.addDatasets(data);
                }

            });
        };

        this.addIntegrationColumnsClicked = function(arg) {
            console.log('Add Integration Columns Clicked');
            integration.columns.push({name: 'integration column: ' + integration.columns.length });
            openModal({
                title: "Add Ontologies",
                searchType: "ontology",
                url: "/workspace/ajax/find-ontologies",
                categoryFilter: true,
                close: function(data) {
                    self.addIntegrationColumns();
                }
            });
        };

        this.addDisplayColumnClicked = function(arg) {
            console.log('add display column clicked');
            integration.columns.push({name: 'display column: ' + integration.columns.length })
        };

        this.removeSelectedDatasetClicked = function(arg) {
            console.log('remove selected dataset clicked');
        }
    }]);

    //Controller that drives the add-integration-column controller
    app.controller('ModalDialogController', ['$scope', '$http', 'close', 'options',  function($scope, $http, close, options){
        var url = options.url, closeWait = 500;
        console.debug("ModalDialogController:: url:%s", url);
        $scope.title = options.title;
        $scope.filter = new SearchFilter();
        $scope.selectedItems = [];
        $scope.results = [];

        //initialize lookup lists
        $scope.projects = _documentData.allProjects;
        $scope.collections = _documentData.allCollections;
        $scope.categories = _documentData.allCategories;

        $scope.categoryFilter = options.categoryFilter;

        //when non-null
        $scope.errorMessage = null;

        //ajax search fires up at launch and whenever search terms change
        $scope.search = function() {
            console.debug("$scope.search::");
            console.debug(options);
            var config = {};
            config.params = $.extend({}, $scope.filter);

            var promise = $http.get(options.url, config);
            promise.success(function(data){
                //transform date strings into dates
                //FIXME: ajax should return date in ISO 8601 format
                data.forEach(function(item) {
                    item.date = new Date(item.date_registered);
                    //console.log("translating: %s", item);
                });

                if(options.transformData) {
                    $scope.results = options.transformData(data);
                } else {
                    $scope.results = data;
                }

            });

        };

        //update the filter whenever user updates filter UI
        $scope.updateFilter = function() {
            var data = JSON.stringify($scope.filter, null, 4);
            $scope.search();
        }

        //called when user clicks 'Add Selected Items'
        $scope.confirm = function(obj) {
            var data = JSON.stringify(obj);
            console.info("confirm:: %s", data);
            close(obj, closeWait);
        }

        //convenience function - true if specified item is in the selecteditems list
        $scope.isSelected = function(item) {
            return $scope.selectedItems.indexOf(item) > -1;
        }

        //TODO:  this could be pulled out into a commmon function,  e.g.  TDAR.common.toggleArrayValue(arr, value)

        //called when user selects/deslects one of the items in the select
        $scope.toggleSelection = function(itemId, obj) {
            console.debug("toggleSelected::");
            var items = $scope.selectedItems,
                    idx = items.indexOf(itemId);
            //if in list, remove it
            if(idx > -1 ) {
                items.splice(idx, 1);
            //otherwise, add it
            } else {
                items.push(itemId);
            }

        };

        //Execute a search() whenever user updates form control bound to the filter object
        $scope.$watch('filter', function() {
            console.log("filter changed");
            $scope.search();
        }, true);

    }]);

console.log("init done");

    //FIXME: these are hacks to auto-open the various popups
    function _hashUrl() {
        var wlh = "" + window.location.hash;
        return wlh.substring(1);
    }

    $(function() {
        var scope = angular.element($("#divIntegrationMain")).scope();
        //fire dataset modal if url ends in #dataset
        if(_hashUrl() === "dataset")
            scope.ctrl.openDatasetModal();
    })

/* global jQuery, angular  */
})(jQuery, angular, console);