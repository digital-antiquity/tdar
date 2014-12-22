/**
 * (note: this file will likely be moved to /webapp/js.  Placed here mostly out of laziness)
 *
 * dataset ontology node filter init and data mapping
 */

console.log("tdar.filter-ng.js");

var incomingData = JSON.parse(document.getElementById("jsonFilterData").innerHTML);
incomingData.ontologies.forEach(function(ontology){
    console.log("processing ontology: %s", ontology.title);
    ontology.nodes.forEach(function(node){
        node.selectionPolicy = ko.observable("NOT_SELECTED");
        node.selected = false;

        //make up bogus participation data for now (let's pretend that integration column participates in 20% of node values)
        node.participation = [];
        ontology.integrationColumns.forEach(function(integrationColumn){
            node.participation.push(Math.random() < 0.2 ? "✓" : " ");
        });
    });

});

ko.applyBindings(incomingData);

console.log(incomingData);