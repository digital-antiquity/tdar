(function($, angular, console){
    "use strict";
    var _projects, _collections, _categories, _documentData;

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

    var app = angular.module('integrationApp', ['angularModalService']);

    //Root-level controller for the integration viewmodel
    app.controller('IntegrationCtrl', function(){
        var self = this,
            integration = new Integration();

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

        this.integrateClicked = function() {
            console.log('integrate clicked');
        }

        this.addDatasetsClicked = function(arg) {
            console.log('Add Datasets clicked');
        };

        this.addIntegrationColumnsClicked = function(arg) {
            console.log('Add Integration Columns Clicked');
            integration.columns.push({name: 'integration column: ' + integration.columns.length })
        };

        this.addDisplayColumnClicked = function(arg) {
            console.log('add display column clicked');
            integration.columns.push({name: 'display column: ' + integration.columns.length })
        };

        this.removeSelectedDatasetClicked = function(arg) {
            console.log('remove selected dataset clicked');
        }
    });

    //This is the controller that drives the modal window
    app.controller('DatasetController', ['$scope', '$http', 'close',  function($scope, $http, close) {
        var animDelay = 500, returnData = {};
        console.info("DatasetController  close:%s", close);
        var self = this;  //modal library doesn't support controller-as syntax yet
        $scope.greeting = "I'm the scope";

        $scope.returnData = returnData;


        $scope.confirm  = function(result) {
            //console.log("close  result:%s", result);
            close(result, animDelay);
        };

        $scope.cancel = function() {
            close('cancel', animDelay)
        }

    }]);


    //FIXME: this controller isn't necessary,  it's mostly copypasta from a tutorial. i'll refactor it soon.
    app.controller('ModalController', function($scope, ModalService) {
        console.info("ModalController");
        var self = this;
        self.buttonClicked = function() {
            console.log('buttonClicked');
        };

        $scope.showDatasetsModal = function() {
            console.log("showDatasetsModal:");
            ModalService.showModal({
                templateUrl: "workspace/add-datasets.html",
                controller: "DatasetController"
            }).then(function(modal){

                //this is the bootstrap modal function.
                modal.element.modal();


                modal.close.then(function(result){
                    modal.element.modal('hide');
                    console.log("modal closed.  result:%s", result);
                    $scope.message = result ? "result was yes" : "result was no";
                });
            }).catch(function(error){
                console.error(error);
            });
        }
    });

    //Controller that drives the add-integration-column controller
    //FIXME: given the similarity we should probably refactor this to use one controller for both dialogs
    app.controller('OntologyController', ['$scope', '$http', function($scope, $http){
        $scope.title = "Add Integration Columns";
        $scope.filter = new SearchFilter();
        $scope.selectedItems = [];

        //initialize lookup lists
        $scope.projects = _documentData.allProjects;
        $scope.collections = _documentData.allCollections;
        $scope.categories = _documentData.allCategories;


        //perform initial search
        $scope.results = [];
        for(var i = 0; i < 20; i++) {
            $scope.results.push({
                id: i,
                title: 'sample ontology title',
                author: 'sample ontology author',
                date: 'mm/dd/yyyy'
            });
        }

        //update the filter whenever user updates filter UI
        $scope.updateFilter = function() {
            var data = JSON.stringify($scope.filter, null, 4);
            console.info("updateFilter:: %s", data);
        }

        //called when user clicks 'Add Selected Items'
        $scope.confirm = function(obj) {
            var data = JSON.stringify(obj);
            console.info("confirm:: %s", data);
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

        }
    }]);

console.log("init done");






/* global jQuery, angular  */
})(jQuery, angular, console);