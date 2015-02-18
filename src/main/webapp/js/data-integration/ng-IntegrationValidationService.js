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

        this.validate = function(integration) {
            //rule: did the server report any errors?
            //basically we pass through any errors - an empty list (or null)is a no-op,
            _addErrors(integration.errors || []);

            //rule: how about field-specific errors
            if(!$.isEmptyObject(integration.fieldErrors)) {
                //fixme: figure out how to tie broken validation rules to specific object in model. for now just considier it one big error
                _addErrors("You have field-specific errrors");
            }


            //rule: do any integration columns contain null dataTableColumn entries?
            integration.columns
                .filter(function(c){return TYPE_INTEGRATION === c.type })
                .filter(function(ic){return ic.dataTableColumns.some(function(col){return col == null})})
                .forEach(function(invalidColumn){
                    _addErrors("The following integration column is invalid:" + invalidColumn.name);
                });

            //fixme: consider using $.validation.format for error interpolation

            //todo rule: do the integration columns have the correct number dataTableColumns

            //fixme: hack: make sure we're wired up
            //_addErrors("*tap* *tap*  is this thing on?");

            //conversely, you could call validate() and then inspect validationService.errors
            return self.errors;
        };
    }]);

/* global jQuery, angular */
})(jQuery,jQuery.validator, angular, console);