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
                label_class: {
                    type: String,
                    default: "badge badge-secondary"
                },
                disabled_initially: {
                    type:Boolean,
                    default: false
                },
                prefix: {type: String,
                    default: ""},
                include_id: {type: Boolean,
                    default: true},
                use_name_field: {type: Boolean,
                        default: true},
                name_field: {
                    type: String,
                    default: "name"
                },
                id_field: {
                    type: String,
                    default: "id"
                }
            },
            data: function() {
                return {
                    disabled: false
                }
            },
        mounted: function() {
            if (this.disabled_initially) {
                this.disabled = true;
            }
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
            nameField: function(idx) {
                var ret = "";
                if (this.prefix != undefined) {
                    ret = ret + this.prefix;
                }

                ret = ret + "[" + idx + "]";
                
                if (this.prefix != undefined && this.use_name_field) {
                    ret = ret + ".";
                }
                
                if (this.use_name_field) {
                    ret = ret + this.name_field;
                }
                return ret;
            },
            idField: function(idx) {
                var ret = "";
                if (this.prefix != undefined) {
                    ret = ret + this.prefix + ".";
                }
                ret = ret + "[" + idx + "]";

                if (this.prefix != undefined) {
                    ret = ret + ".";
                }
                ret = ret + this.id_field;
                return ret;

            },
            getLabel: function(value) {
                return value[this.name_field];
            },
            remove: function(index) {
                if (this.disabled) {
                    return;
                }
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
            labelFocus: function(event) {
                if (event) event.preventDefault()
                return false;
            },
            addAutocompleteValue: function(result) {
                console.log(result);
                if (result != undefined) {
                    if (this.allow_create == false && result[this.id_field] != undefined) {
                        this._addEntry(result[this.name_field], result[this.id_field]);
                     } 
                    if (this.allow_create == true) {
                        if (typeof result == 'string') {
                            this._addEntry(result, undefined);
                        } else {
                            this._addEntry(result[this.name_field], result[this.id_field]);
                        }
                    }
                    this.$refs.input.reset();
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
                    var val = {};
                    val[this.id_field] = id;
                    val[this.name_field] = entry.trim();
                    this.values.push(val);
                }
            },
            reset: function() {
                this.values.length = 0;
            },
            disable: function() {
                this.disabled = true;
                Vue.set(this, "disabled", true);
            },
            enable: function() {
                this.disabled = false;
                Vue.set(this, "disabled", false);
            },
            setValues: function(newvalues) {
                console.log("setting values to:", newvalues);
                this.reset();
                for (var i =0; i < newvalues.length; i++) {
                    this.values.push(newvalues[i]);
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
                if (input.selectionStart == 0 && input.selectionEnd == 0) {
                    this.remove(this.values.length -1);
                }
            }
        }
    });

    }
})(console, window, Vue, axios);