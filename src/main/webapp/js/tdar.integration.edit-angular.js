(function($, angular, console){
    "use strict";

    //Our integration "Model" object.
    function Integration() {
        var self = this;

        self.title = "";
        self.description = "";
        self.columns = [];
    }



    var app = angular.module('integrationApp', ['angularModalService']);

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


    //his controller isn't necessary,  it's mostly copypasta from a tutorial. i'll refactor it soon.
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

console.log("init done");






/* global jQuery, angular  */
})(jQuery, angular, console);