const angular = require("angular");
var app = angular.module("integrationApp");
require("./../../includes/bootstrap-2.32/js/bootstrap.js")

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
        recordsPerPage : 10,
        recordCount : true

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
            "searchFilter.startRecord" : self.startRecord,
            "searchFilter.recordsPerPage" : self.recordsPerPage,
            "fetchRecordCount" : self.recordCount

        };
    }
}

// Controller that drives the add-integration-column controller
app.controller('ModalDialogController', [ '$scope', 'DataService',  function($scope, dataService) {
    var options = {}; // will be set by scope.openModal()
    var closeWait = 500;

    // get map of embedded data stored in the DOM
    var documentData = dataService.getDocumentData();

    $scope.title = "";
    var filter = new SearchFilter();
    $scope.filter = filter;

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
        filter.startRecord = 0;
        filter.recordCount = true;
    }

    // called when user clicks 'Add Selected Items'
    $scope.confirm = function(selectedIds) {
        $scope.close(selectedIds, closeWait);
    }

    // convenience function - true if specified item is in the selecteditems list
    $scope.isSelected = function(item) {
        return $scope.selectedItems.indexOf(item) > -1;
    }

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

    $scope.$on("openTdarModal", function(e, modalOptions) {
        console.log("openTdarModal:: ");
        console.log(modalOptions);
        console.log(arguments);
        options = modalOptions;
        $scope.openTdarModal(options);
    });

    $scope.reset = function() {
        console.log("reset called");
        $scope.title = options.title;
        $scope.categoryFilter = options.categoryFilter;
        // $scope.selectedItems = [];
    };

    $scope.clearSelectedItems = function() {
        $scope.selectedItems = [];
        return false;
    }

    var _hasNextPage = function() {
        return $scope.filter.startRecord + $scope.filter.recordsPerPage < $scope.modalTotalResults;
    };

    var _hasPreviousPage = function() {
        return filter.startRecord > 0;
    }

    $scope.hasNextPage = _hasNextPage;

    $scope.hasPreviousPage = _hasPreviousPage;

    $scope.nextPage = function() {
        console.debug("nextPage::");
        if (_hasNextPage()) {
            filter.startRecord += filter.recordsPerPage;
            filter.recordCount = false;
            $scope.search();
        }
    };

    $scope.previousPage = function() {
        console.debug("previousPage::");
        if (_hasPreviousPage()) {
            filter.startRecord -= filter.recordsPerPage;
            filter.recordCount = false;
            $scope.search();
        }
    }

    $scope.startRecord = function() {
        return filter.startRecord;
    };

    $scope.endRecord = function() {
        return filter.startRecord + $scope.results.length;
    };

} ]);

app.directive("tdarModal", function() {
    return {
        restrict : 'E',
        link : function(scope, element, attr) {
            var modalRoot = element.children();
            console.log(modalRoot);
            var closedCallback;
            scope.openTdarModal = function(options) {
                modalRoot.modal();
                scope.reset();
                closedCallback = options.close;
            };

            scope.close = function(data, closeWait) {
                var wait = closeWait ? closeWait : 1;
                modalRoot.modal("hide");
                setTimeout(function() {
                    closedCallback(data);
                }, wait);
            };

        },
        templateUrl : "workspace/modal-dialog.html"
    }
});

module.exports = {}