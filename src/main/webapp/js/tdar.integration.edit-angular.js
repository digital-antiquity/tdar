(function($, angular, console){
    "use strict";
    var app,_projects, _collections, _categories, _documentData;
    app = angular.module('integrationApp', ['angularModalService']);

    //Our integration model
    function Integration() {
        var self = this;

        /**
         * Name for the current integration workflow, specified by the user. This is for organizational purposes only(e.g. picking a previous integration out
         * of a list).  This name does not appear in the final integration report.
         * @type {string}
         */
        self.title = "";

        /**
         * Description for the current workflow.  For organizational purposes (it does not appear in final integration report).
         * @type {string}
         */
        self.description = "";

        /**
         * This list describes columns in the final integration results.  Here a column may be either an "integration" column or a "display" column.
         * @type {Array}
         */
        self.columns = [];

        /**
         * List of datatables included in the integration.
         * @type {Array}
         */
        self.datatables = [];

        /**
         * List of ontologies that the user may integrate over. To be added as an integration column,   the ontology must be "shared" by all of this integration
         * object's datatables.  That is, the system can only create an integration column for a given ontology if, for every datatable in Integration#datatables, there is at least one
         * datataneColumn with a default_ontology_id that equals ontology.id.
         * @type {Array}
         */
        self.ontologies = [];

        /**
         * Build the datatable participation information for the specified ongology and store the results in ontology.compatibleDatatableColumns
         *
         * compatibleDatatableColumns is a 2d array of datatableColumns that map to the specified ontology.
         *
         * Not to be
         * confused with datatableColumn list in an integration context.
         * @param ontology
         * @private
         */
        function _buildCompatibleDatatableColumns(ontology) {

            //list of maps
            var compatTables = [];

            self.datatables.forEach(function(datatable) {
                compatTables.push({
                    datatable: datatable,
                    compatCols:datatable.columns.filter(function(col){
                        return col.default_ontology_id === ontology.id;
                    })
                });
            });
            ontology.compatibleDatatableColumns = compatTables;
        }

        /**
         * Append an 'integration column' to the columns list.
         * @param title
         * @param ontology
         * @returns {{type: string, title: *, ontologyId: *, nodeSelections: Array, datatableColumnIds: Array}}
         */
        self.addIntegrationColumn =  function _addIntegrationColumn(title, ontology) {
            var self = this;
            var col = {
                type: "integration",
                title: title,
                ontologyId: ontology.id,
                nodeSelections: [],
                selectedDatatableColumns: ontology.compatibleDatatableColumns.map(function(dt){
                    if(!dt.compatCols.length) return null;
                    return dt.compatCols[0];
                })
            }

            col.nodeSelections = ontology.nodes.map(function(node){
                return {
                    selected: false,
                    node: node
                }
            });
            self.columns.push(col);
            return col;
        };

        self.updateSharedOntologies = function _updateSharedOntologies(ontologies) {
            _setAddAll(self.ontologies, ontologies, "id");
            ontologies.forEach(_buildCompatibleDatatableColumns);
        };

        self.removeDatatable = function _removeDatatable(datatable) {
            _setRemove(self.datatables, datatable);
        };


        /**
         * Add a 'display column' to the columns list.  A display column contains a list of datatableColumn selections, which the system
         * will include in the final integration report file.   The user may choose 0 or 1 datatableColumn from each
         * datatable.  This method primes the datatableColumnSelections list to be size N with each slot containing null (where
         * N is the count of data tables in the current integration workflow.
         * @param title
         * @private
         */
        self.addDisplayColumn = function _addDisplayColumn(title) {

            var displayColumn = {
                type: 'display',
                title: title,
                datatableColumnSelections: []
            };

            self.datatables.forEach(function(table){
                var selection = {
                    datatable: table,
                    datatableColumn: null
                }
                displayColumn.datatableColumnSelections.push(selection);
            });
            self.columns.push(displayColumn);
        }
    }

    /**
     * SearchFilter stores the current filter values specified by the user when interacting with the "Find Ontologies" and "Find Datasets" popup control
     * @constructor
     */
    function SearchFilter() {
        var self = this;
        var _properties = {
            title: "",
            projectId: null,
            collectionId: null,
            categoryId: null,
            bookmarked: false,
            incompatible: false,
            //fixme: get pagination info from paginationHelper
            startRecord: 0,
            recordsPerPage: 500
        };

        $.extend(self, _properties);
        // FIXME: for Jim: do this dynamically so that we can just choose a prefix and all properties get prefixed by
        // the prefix
        self.toStrutsParams = function() {
            return {
                "searchFilter.title": self.title,
                "searchFilter.projectId": self.projectId,
                "searchFilter.collectionId": self.collectionId,
                "searchFilter.categoryId": self.categoryId,
                "searchFilter.bookmarked": self.bookmarked,
                "searchFilter.incompatible": self.incompatible,
                "searchFilter.startRecord": self.startRecord,
                "searchFilter.recordsPerPage": self.recordsPerPage
            }
        }
    }

    //TODO: this function is handy - move it to tdar.core.js (blocker: TDAR is not defined yet. I really need to untangle the <script> load order.  )
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

    //FIXME: HACK: this iife-global should be moved to a bootstrap/init section (what's angular way to bootstrap an app?), or maybe wrap this inside an angular service?
    _documentData = _loadDocumentData();

    /**
     * Add specified object to specified array if specified array does not already contain the object.
     *
     * @param arr an array of objects
     * @param obj the object to potentially add to the array
     * @param propName the property to consider when looking for duplicate items. If undefined, function uses strict identity to find dupes.
     * @private
     */
    function _setAdd(arr, obj, propName) {
        var fn, dupes=[];
        if(propName) {
            fn = function(testObj) {
                return testObj[propName] === obj[propName]
            };
        } else {
            fn = function(testObj) {
                return testObj === obj;
            }
        }
        dupes = arr.filter(fn);
        if(dupes.length === 0) {
            arr.push(obj);
        }
        return dupes.length === 0
    }

    function _setAddAll(arr, objs, propName) {
        objs.forEach(function(obj) {
           _setAdd(arr, obj, propName);
        });
    }

    /**
     * Retrieve item from an array (by key), treating array as a set.  Yes, I realize setGet is a stupid function name.  Go away.
     * @param arr
     * @param keyName
     * @param key
     * @returns  object located by key, or null if not found
     * @private
     */
    function _setGet(arr, keyName, key) {
        var val = null;
        for(var i = 0; i < arr.length; i++) {
            if(arr[i][keyName] === key) {
                val = arr[i];
                break;
            }
        }
        return val;
    }

    /**
     * Remove specified item from the specified array
     * @param arr
     * @param item
     * @returns {*} original, mutated array
     * @private
     */
    function _setRemove(arr, item) {
        if(arr.indexOf(item) === -1) return arr;
        arr.splice(arr.indexOf(item), 1);
        return arr;
    }

    /*
     Make the integration viewmodel available to all components in this angular app.  Currently overkill (we could put this object in iife scope), but we may choose
     to break out this code into multiple .js files later.

     More info:
        more info: http://stackoverflow.com/questions/16508666/sharing-a-variable-between-controllers-in-angular-js
        http://stackoverflow.com/questions/21919962/angular-share-data-between-controllers
     */
    app.service('integrationService', Integration);

    //top-level controller for the integration viewmodel
    app.controller('IntegrationCtrl', ['$scope', 'ModalService', '$http', 'integrationService',  function($scope, ModalService, $http, integration){
        var self = this,
            openModal,
            designatedOntologies = {};

        //fixme: remove later, expose viewmodel for debugging
        window.__viewModel= self;

        //controller public fields
        self.integration = integration;
        self.tab = 0;
        self.sharedOntologies = [];

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
                _setAddAll(self.integration.datatables, data[0].dataTables, "data_table_id");
                self.integration.updateSharedOntologies(data[0].sharedOntologies);
            });
        };

        /**
         * Build list of integration columns.  Called after user selects list of ontology id's.
         * @param ontologyIds
         */
        self.addIntegrationColumns = function(ontologyIds) {
            //If user chooses ontologies that arent (yet) in the sharedOntologies list, we'll need to look them up.
            var columnsToAdd = [];
            var missingIds = [];
            ontologyIds.forEach(function(id){
                var ontology = _setGet(self.sharedOntologies, "id", id);
                if(ontology) {
                    columnsToAdd.push(ontology);
                } else {
                    missingIds.push(id);
                }
            });

            //Lookup any missing ontologies and then add them to integration.columns.
            if(missingIds.length) {
                $http.get("/workspace/ajax/ontology-details?" + $.param({ontologyIds: missingIds}, true), {
                }).success(function(data) {
                    data.forEach(function(ontology){
                        columnsToAdd.push(ontology);
                    });
                    processAddedIntegrationColumns(columnsToAdd);
                });

            //if no missing id's add the integration columns immediately
            } else {
                processAddedIntegrationColumns(columnsToAdd);
            }

        };

        //add and initialize an integration column associated with with the specified ontology ID
        var processAddedIntegrationColumns = function(ontologies) {
            console.debug("processAddedIntegrationColumns ::");
            ontologies.forEach(function(ontology){
                self.integration.addIntegrationColumn('intcol' + ontology.id, ontology);
            });
        }


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
        }

        self.integrateClicked = function() {
            console.debug('integrate clicked');
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
            if(!$scope.selectedDatatables) {return;}

            $scope.selectedDatatables.forEach(function(item){
                integration.removeDatatable(item);
            });
        };

        self.addToIntegrationColumnsClicked = function() {
            console.log("addToIntegrationColumnsClicked: %s", $scope.selectedOntologies);
            $scope.selectedOntologies.forEach(function(ontology, idx){
                self.integration.addIntegrationColumn(ontology.name, ontology);
            });
        }

        $scope.lookupCompatibleColumns = function(id) {
            var ontology = _setGet(integration.ontologies, "id", id);
            return ontology.compatibleDatatableColumns;

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
            var config = {
                params: $scope.filter.toStrutsParams()
            };
            //console.debug(config.params);
            var promise = $http.get(url, config);
            promise.success(function(data){
                //transform date strings into dates

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
        $scope.confirm = function(selectedIds) {
            close(selectedIds, closeWait);
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
            console.debug("filter changed");
            $scope.search();
        }, true);

    }]);

    app.controller('LegacyFormController', ['$scope', '$http', 'integrationService', function($scope, $http, integration){
        var self = this, fields = [];
        self.fields = fields;
        self.integration = integration;
        self.showForm = false;
        self.hideForm = function() {
            self.showForm = false;
        };

        self.dumpdata = function() {
            console.log("hello");
            console.log($("#frmLegacy").serializeArray());
            JSON.stringify($("#frmLegacy").serializeArray(), null, "4");
        };





    }]);

/* global jQuery, angular  */
})(jQuery, angular, console);
