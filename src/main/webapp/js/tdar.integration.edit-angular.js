(function($, angular, console){
    "use strict";

    //Our integration "Model" object.
    function Integration() {
        var self = this;

        self.title = "";
        self.description = "";
        self.columns = [];
    }



    var app = angular.module('integrationApp', []);

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






/* global jQuery, angular  */
})(jQuery, angular, console);