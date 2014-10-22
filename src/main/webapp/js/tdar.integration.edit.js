(function($, ko, console){
    "use strict";

    var data = {
        integration: {
            title: "title",
            description: "description",
            columns: []
        }
    }

    function IntegrationViewModel(data) {
        var self = this;
        ko.mapping.fromJS(data, {}, self);


        //HACK: this method doesn't do anything really right now. Eventually a modelWindowClose eventhandler will call this function along w/
        // the data that it needs to populate the new tab
        self.addColumn = function() {
            self.columns.push(ko.mapping.fromJS(new Column()));
            console.log("new size of columns: %s", self.columns().length);
        }
    }

    function Column() {
        var self = this;
        return $.extend(self, {
           name: "column",
           type: "integration"
        });
    }

    var viewModel = {
        addDisplayColumnClicked: function(arg) {
            console.log("clicked: %s", arg);
        },

        addIntegrationColumnsClicked: function(arg) {
            console.log("clicked: %s", arg);
        },

        addDatasetsClicked: function(arg) {
            console.log("clicked: %s", arg);
        },

        removeDatasetClicked: function(arg) {
            console.log("clicked: %s", arg);
        }
    };

    ko.mapping.fromJS(data, {
            "integration": {
                "create": function(options) {
                    return new IntegrationViewModel(options.data);
                }
            }
        }, viewModel);

    ko.applyBindings(viewModel);

})(jQuery, ko, console);