(function($, ko, console){
    "use strict";

    function Integration() {
        var self = this;
        self.title = "title";
        self.description = "description";
        self.columns = [];
        self.currentColumn = 0;
    }

    function Column() {
        var self = this;
        return $.extend(self, {
           name: "column",
           type: "integration"
        });
    }

    console.log("hello ko");
    var vm = null;
    var data = {
        integration: new Integration(),

        addDisplayColumnClicked: function(arg) {
            console.log("clicked: %s", arg);
        },

        addIntegrationColumnsClicked: function(arg) {
            console.log("clicked: %s", arg);
        },

        addDatasetsClicked: function(arg) {
            console.log("clicked: %s", arg);

            //This seems really awkward.  I need to forward-define the viewmodel prior to declaring the function that modifies he columns array.
            //It feels like it would make more sense to have the Integration object have an addColumn. But you can't do that because the columns field
            // that gets modified is not actually what you want to modify directly.  Instead you must modify the viewmodel.columns() field.
            vm.integration.columns.push(new Column());
        },

        removeDatasetClicked: function(arg) {
            console.log("clicked: %s", arg);
        }

    };

    vm = ko.mapping.fromJS(data);

    vm.addColumn = function() {
        console.log("adding column")
        vm.integration.columns.push(new Column());
    };

    ko.applyBindings(vm);

    vm.integration.title("foo");
    vm.integration.description("a description");

    window.vm = vm;

})(jQuery, ko, console);