(function(angular) {
    "use strict";

    // var app = angular.module('integrationApp', ['angularModalService']);
    var app = angular.module("integrationApp");

    /**
     * SearchFilter stores the current filter values specified by the user when interacting with the "Find Ontologies" and "Find Datasets" popup control
     * 
     * @constructor
     */
    function SearchFilter() {
        var self = this;
        var _properties = {
            title : "",
            projectId : null,
            collectionId : null,
            categoryId : null,
            bookmarked : false,
            integrationCompatible : true,
            // fixme: get pagination info from paginationHelper / controller?
            startRecord : 0,
            recordsPerPage : 500
        };

        $.extend(self, _properties);
        // FIXME: for Jim: do this dynamically so that we can just choose a prefix and all properties get prefixed by
        // the prefix
        self.toStrutsParams = function() {
            return {
                "searchFilter.title" : self.title,
                "searchFilter.projectId" : self.projectId,
                "searchFilter.collectionId" : self.collectionId,
                "searchFilter.categoryId" : self.categoryId,
                "searchFilter.bookmarked" : self.bookmarked,
                "searchFilter.ableToIntegrate" : self.integrationCompatible,
                "startRecord" : self.startRecord,
                "recordsPerPage" : self.recordsPerPage
            };
        }
    }

    // Controller that drives the add-integration-column controller
    app.controller('ModalDialogController', [ '$scope', 'DataService', 'close', 'options', function($scope, dataService, close, options) {
        var url = options.url, closeWait = 500;

        // get map of embedded data stored in the DOM
        var documentData = dataService.getDocumentData();

        console.debug("ModalDialogController:: url:%s", url);
        $scope.title = options.title;
        $scope.filter = new SearchFilter();
        $scope.selectedItems = [];
        $scope.results = [];

        // initialize lookup lists
        $scope.projects = documentData.allProjects;
        $scope.collections = documentData.allCollections;
        $scope.categories = documentData.allCategories;

        $scope.categoryFilter = options.categoryFilter;

        // when non-null
        $scope.errorMessage = null;
        $scope.modalSearching = false;
        $scope.modalTotalResults = 0;
        // ajax search fires up at launch and whenever search terms change
        $scope.search = function() {
            var futureData;
            $scope.modalSearching = true;
            if (options.searchType === "ontology") {
                futureData = dataService.findOntologies($scope.filter);
            } else {
                futureData = dataService.findDatasets($scope.filter);
            }
            futureData.then(function(results) {
                $scope.results = results.results;
                $scope.modalSearching = false;
                $scope.modalTotalResults = results.totalRecords;
            });
        };

        // update the filter whenever user updates filter UI
        $scope.updateFilter = function() {
            var data = JSON.stringify($scope.filter, null, 4);
            $scope.search();
        }

        // called when user clicks 'Add Selected Items'
        $scope.confirm = function(selectedIds) {
            close(selectedIds, closeWait);
        }

        // convenience function - true if specified item is in the selecteditems list
        $scope.isSelected = function(item) {
            return $scope.selectedItems.indexOf(item) > -1;
        }

        // TODO: this could be pulled out into a commmon function, e.g. TDAR.common.toggleArrayValue(arr, value)

        // called when user selects/deslects one of the items in the select
        $scope.toggleSelection = function(itemId, obj) {
            console.debug("toggleSelected::");
            var items = $scope.selectedItems, idx = items.indexOf(itemId);
            // if in list, remove it
            if (idx > -1) {
                items.splice(idx, 1);
                // otherwise, add it
            } else {
                items.push(itemId);
            }
        };

        // Execute a search() whenever user updates form control bound to the filter object
        $scope.$watch('filter', function() {
            console.debug("filter changed");
            $scope.search();
        }, true);

    } ]);

    /* global angular */
})(angular);