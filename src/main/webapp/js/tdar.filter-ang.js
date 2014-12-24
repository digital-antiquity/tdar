/**
 * (note: this file will likely be moved to /webapp/js.  Placed here mostly out of laziness)
 *
 * dataset ontology node filter init and data mapping
 */

console.log("tdar.filter-ng.js");

var _data, _scope;

(function(json){
   "use strict";

    var app;
    var incomingData = JSON.parse(json);
    _data = incomingData; //so that we can access model from the console

    app = angular.module("integration", []);

    console.log("incomingdata");
    console.log(incomingData);



    incomingData.ontologies.forEach(function(ontology){
        console.log("processing ontology: %s", ontology.title);
        ontology.nodes.forEach(function(node){
            node.selectionPolicy = "NOT_SELECTED";
            node.selected = false;

            //make up bogus participation data for now (let's pretend that integration column participates in 20% of node values)
            node.participation = [];
            ontology.integrationColumns.forEach(function(integrationColumn){
                node.participation.push(Math.random() < 0.2 ? "✓" : " ");
            });
        });
    });

    app.controller("IntegrationController", function($scope) {
        _scope = $scope;  //so that we can trigger $scope.$apply() from the console
        this.ontologies = incomingData.ontologies;
        this.columns = incomingData.displayColumns;
    });



})(document.getElementById("jsonFilterData").innerHTML);
