TDAR.vuejs.tagging= (function(console, ctx, Vue, axios, TDAR) {
    "use strict";
    
    var app = new Vue({
        el : "#sel",
        data : {values: [ 
                {val: "science"} , 
                {val: "biology"} , 
                {val: "chemistry"},
                {val: "physics"} ],
                separator: ";" ,
                autocompleteUrl:'https://localhost:8443/api/lookup/resource',
                autocompletesuffix:'',
                resultsuffix:'resources',
                allowCreate:true,
                nameField: 'title',
                idField: 'id' },
        mounted: function() {
        },
        methods: {
            remove: function(index) {
                 this.values.splice(index,1);
            },
            addEntry: function(input) {
                if (this.allowCreate == true) {
//                    console.log("allowCreate: ", this.allowCreate, input);
                    this._addEntry(input);
                    var $input = this.$refs.input;
                    $input.reset();
                }
            },
            focus: function() {
                this.$refs.input.focus();
            },
            addAutocompleteValue: function(result) {
                if (result != undefined) {
//                    console.log("allowCreate: ", this.allowCreate, result, result.title);
                    if (this.allowCreate == false && result[this.idField] != undefined) {
                        this._addEntry(result[this.nameField], result[this.idField]);
                        this.$refs.input.reset();
                    } else {
                        this._addEntry(result[this.nameField], result[this.idField]);
                        this.$refs.input.reset();
                    }
                }
            },
            _addEntry: function(entry, id) {
                var seen = false;
                if (entry == undefined || entry.trim() == '') {
                    return;
                }
                this.values.forEach(function(val) {
                    if (val.val == entry) {
                        seen = true;
                    }
                });
                if (!seen) {
                    this.values.push({val: entry.trim(), id: id});
                }
            },
            watchEntry: function() {
                if (this.allowCreate == false) {
                    return;
                }
                var $input = this.$refs.input;
                var val = $input.getValue();
                var sepPos = val.indexOf(this.separator);
                while (sepPos > -1) {
                    var part = val.substring(0, sepPos);
                    var quotes = part.split("\"");
                    if ((quotes.length -1 ) % 2 == 0) {
                        this._addEntry(part);
                        $input.setValue( val.substring(sepPos + 1).trim());
                        sepPos = $input.getValue().indexOf(this.separator );
                    } else {
                        sepPos = $input.getValue().indexOf(this.separator, sepPos + 1 );
                    }
                }
                if ($input.getValue() == '' || $input.getValue() == undefined ){
                    $input.reset();
                }
            },
            deleteLast: function(input) {
                console.log(input.selectionStart ,input.selectionEnd);
                if (input.selectionStart == 0 && input.selectionEnd == 0) {
                    this.remove(this.values.length -1);
                }
            }
        }                   
    });
})(console, window, Vue, axios);