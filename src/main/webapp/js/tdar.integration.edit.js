(function($, ko, console){
    "use strict";
    var tab = 0, viewModel;

    var data = {
        integration: {
            title: "title",
            description: "description",
            columns: []
        }
    }

    function IntegrationViewModel(data) {
        var self = this;
        if(self.columns && self.columns.length === 0) {
            self.columns = []
        }
        ko.mapping.fromJS(data, {}, self);
    }

    function addIntegrationColumns() {
        var columnId = viewModel.integration.columns().length + 1;
        var column = {
            name: "Integration Column" + columnId,
            id: columnId,
            type: 'integration'
        }

        viewModel.integration.columns.push(column);
    }

    viewModel = ko.mapping.fromJS(data, {
        "integration": {
            "create": function(options) {
                return new IntegrationViewModel(options.data);
            }
        }
    });

    $.extend(viewModel, {
        saveClicked: function(arg) {
            console.log("saveClicked:%s", arg);
            console.log(JSON.stringify(ko.mapping.toJS(viewModel), null, 4));
        },

        integrateClicked: function(arg) {
            console.log("integrateClicked:%s", arg);
        },

        addDatasetsClicked: function(arg) {
            console.log("addDatasetsClicked:%s", arg);
        },

        addIntegrationColumnsClicked: function(arg) {
            console.log("addIntegrationColumnsClicked:%s", arg);
            addIntegrationColumns();
        },

        addDisplayColumnClicked: function(arg) {
            console.log("addDisplayColumnClicked:%s", arg);
        },

        removeSelectedDatasetClicked: function(arg) {
            console.log("removeSelectedDatasetClicked:%s", arg);
        },

        tab: ko.computed(function(){
            console.log("computed value is:%s", tab);
            return tab}, viewModel),

        setTab: function(idx) {
            console.log("setTab:%s", idx);
            tab = idx;
        },

        isTabSet: function(idx) {
            return tab === idx;
        },

        closeTab: function() {
            console.log(arguments);
            var idx = 0;
            viewModel.integration.columns.splice(idx, 1);
        }
    });




    ko.applyBindings(viewModel);

    window.viewModel = viewModel;

})(jQuery, ko, console);