(function($, $validator, angular, console){

    var app = angular.module("integrationApp");

    app.service("ValidationService", ['DataService', '$q', function(DataService, $q) {
        var self = this;
        var rules = [];  //fixme: not implemented yet
        var TYPE_INTEGRATION = "INTEGRATION";
        var TYPE_DISPLAY = "DISPLAY";
        var TYPE_COUNT = "COUNT";
        this.errors = [];


       function _addErrors(errorOrErrors) {
            if(typeof errorOrErrors === 'string') {
                self.errors.push(errorOrErrors);
            } else {
                Array.prototype.push.apply(self.errors, errorOrErrors);
            }

        }

        this.hasErrors = function() {
            return this.errors.length === 0;
        }

        this.clearErrors = function() {
            this.errors = [];
        }

        /**
         * Validate incoming jsonData from server (not the same as an inflated integration workflow object)
         * @param jsonData
         * @returns {Array} list of strings containing validation validation error messages (otherwise an empty list)
         */
        this.validateJson = function(jsonData) {
            //rule: did the server report any errors?
            //basically we pass through any errors - an empty list (or null)is a no-op,
            _addErrors(jsonData.errors || []);

            //rule: how about field-specific errors
            if(!$.isEmptyObject(jsonData.fieldErrors)) {
                //fixme: figure out how to tie broken validation rules to specific object in model. for now just consider it one big error
                _addErrors("You have field-specific errrors");
            }


            //rule: do any integration columns contain null dataTableColumn entries?
            _addErrors(jsonData.columns
                    //exclude  display columns (which are allowed to have null dataTableColumns)
                    .filter(function(c){return TYPE_INTEGRATION === c.type })
                    //exclude valid dataTableColumns
                    .filter(function(ic){return ic.dataTableColumns.some(function(col){return col == null})})
                    //convert invalid columns  to error messages
                    .map(function(invalidColumn){return "The following integration column is invalid:" + invalidColumn.name;}));

            //rule: columns must have the correct number of dataTableColumns
            _addErrors(jsonData.columns
                    .filter(function(c){return c.dataTableColumns && jsonData.dataTables;})
                    .filter(function(c){return TYPE_COUNT !== c.type;})
                    .filter(function(c){return c.dataTableColumns.length !== jsonData.dataTables.length;})
                    .map(function(invalidColumn){ return "Column '" + invalidColumn.name  + "' has the wrong number of data table columns"}));


            return self.errors;
        };


        /**
         * Validate a "fully loaded" ngIntegrationModel object. Only works if DataService has loaded/initialized the integration.
         * @param integration
         * @param jsonData the original, deflated integration
         * @returns {Array}  list of error messages, or empty list
         */
        this.validateIntegration = function(jsonData, integration) {
            //rule: selected ontology nodes still exist
            _addErrors(jsonData.columns
                    //exclude all but integration columns
                    .filter(function(c){return TYPE_INTEGRATION === c.type })
                    //exclude valid integration columns  (valid == every selected ontologyNodeId exists in the ontologyNode cache)
                    .filter(function(c){return !c.nodeSelection.every(function(node){
                        var cachedNode = DataService.ontologyNodeCache.get(node.id);
                        if(!cachedNode) {console.error("node id not found:%s", node.id)}
                        return cachedNode})})
                    //convert invalid columns to list of error messages
                    .map(function(invalidColumn){
                        return "The following ontology has been modified since creating this integration:'" +
                                invalidColumn.ontology.title +
                                "' Please review your integration column selections."}));


            //rule: dataTableColumns must still exist
            _addErrors(jsonData.columns
                        //get one big list of all the dtc ids
                        .reduce(function(dtclist,c){
                            return dtclist.concat(c.dataTableColumns.map(function(dtc){if(dtc) {return dtc.id} else {return -1;}}))
                        }, [])
                        //then include only the ones that arent' found in dtc cache
                        .filter(function(dtcid){
                            return !DataService.dataTableColumnCache.get(dtcid)
                        })
                        //convert to list of passive-aggressive error messages.
                        .map(function(invalidDtcId){
                            return "dataTableColumn id is obsolete:" + invalidDtcId;
                        })
            );
            console.debug("validateIntegration::");
            console.debug(self.errors);

        }
    }]);

/* global jQuery, angular */
})(jQuery,jQuery.validator, angular, console);