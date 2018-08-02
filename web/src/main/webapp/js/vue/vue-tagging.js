TDAR.vuejs.tagging= (function(console, ctx, Vue, axios, TDAR) {
    "use strict";

    if (document.getElementById("sel") != undefined) {
        
        var tagging = Vue.component('tagbox', {
            name: "tagbox",
            template: "#tagging-template",
            props: {
                values: {
                type: Array,
                    default: function() {return []},
                },
                separator: {
                    type:String,
                    default: ";"
                },
                autocomplete_url: {
                    type:String,
                    required: true
                },
                autocomplete_suffix: {
                    type:String,
                    default: ""
                },
                result_suffix: {
                    type:String,
                    default: ""
                },
                allow_create: {
                    type:Boolean,
                    default: true
                },
                disabled: {
                    type:Boolean,
                    default: false
                },
                name_field: {
                    type: String,
                    default: "name"
                }
            },
            data: function() {
                return {
                    
                }
            },
        mounted: function() {
        },
        computed: {
            rootClass: function() {
                var ret = "sandbox ";
                if (this.disabled) {
                    ret = ret + " disabled";
                }
                return ret;
            }
        },
        methods: {
            getLabel: function(value) {
                return value[this.name_field];
            },
            remove: function(index) {
                 this.values.splice(index,1);
            },
            addEntry: function(input) {
                if (this.allow_create == true) {
                    this._addEntry(input);
                    var $input = this.$refs.input;
                    $input.reset();
                }
            },
            focus: function() {
                if (this.disabled == false ) {
                    this.$refs.input.focus();
                }
            },
            addAutocompleteValue: function(result) {
                if (result != undefined) {
                    if (this.allow_create == false && result[this.idField] != undefined || this.allow_create == true) {
                        this._addEntry(result[this.name_field], result[this.idField]);
                        this.$refs.input.reset();
                     } else {
                         this._addEntry(result[this.name_field], result[this.idField]);
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
                    var val = {id: id};
                    val[this.name_field] = entry.trim();
                    this.values.push(val);
                }
            },
            reset: function() {
                Vue.set(this, "values", []);
            },
            setValues: function(values) {
                console.log("setting values to:", values);
                this.reset();
                for (var i =0; i < values.length; i++) {
                    this.values.push(values[i]);
                }
            },
            watchEntry: function() {
                if (this.allow_create == false) {
                    return;
                }
                var $input = this.$refs.input;
                var sepPos = $input.getValue().indexOf(this.separator);
                while (sepPos > -1) {
                    var val = $input.getValue();
// console.log(val, sepPos);
                    var part = val.substring(0, sepPos);
                    var quotes = part.split("\"");
                    if ((quotes.length -1 ) % 2 == 0) {
                        this._addEntry(part);
                        $input.setValue( val.substring(sepPos + 1).trim());
// console.log("setting to:", $input.getValue());
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

    }
})(console, window, Vue, axios);