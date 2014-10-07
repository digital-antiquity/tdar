/**
 * (note: this file will likely be moved to /webapp/js.  Placed here mostly out of laziness)
 *
 * dataset ontology node filter init and data mapping
 */

console.log("tdar.filter-ng.js");


function OntologyNode(data) {
    var self = this;
    ko.mapping.fromJS(data, {}, self);
    self.label = ko.observable();
    self.index = ko.observable();
    self.id = ko.observable();
    self.integrationColumns = ko.observableArray([]); // possibly just a bit array of whether the column has data for this ontology node
    self.criteria = ko.observable(); // NOT_SELECTED, SELECT_IF_SOME, SELECT_IF_ALL
    self.included = ko.observable(false);
    return self;
}
function DisplayColumn(data) {
    var self = this;

}
function OntologyViewModel(data) {
    var self = this;
    if (data) {
        ko.mapping.fromJS(data, {}, self);
    }
    self.ontologyNodes = ko.observableArray([]);
    self.name = ko.observable();
    self.integrationColumns = ko.observableArray([]);
}
function OntologyFilterViewModel(data) {
    // master view model for the entire page
    var self = this;
    if (data) {
        ko.mapping.fromJS(data, {}, self);
    }
    // a list of OntologyViewModels
    self.ontologies = ko.observableArray([]);
    self.displayColumns = ko.observableArray([]);
}



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