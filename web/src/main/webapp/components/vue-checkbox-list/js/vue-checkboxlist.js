
TDAR.vuejs.checkboxlist = (function(console, ctx, Vue, axios, TDAR, _jq) {
    "use strict";
    console.log('vue-checkboxlist.js begin');

    var _checkboxlist = Vue.component('checkboxlist', {
        // name: "CheckboxList",
        template : "#checkboxlist-template",

        model: {
            prop: "items",
            event: "listupdate"
        },

        props: {
            msg: String,
            name: String,
            disabled: Boolean,
            choices: {
                required: true
            },
            items: {
                type: Array,
                default: function(){return []}
            },

            labelKey: {
                type: String,
                required: false,
                default: function() {return ""}
            },

            valueKey: {
                type: String,
                required: false,
                default: function() {return ""}
            }
        },

        computed: {},

        data: function() {
            return {};
        },

        methods: {
            cbchanged: function(val, itemChecked) {
                var newitems = this.items.slice();
                if (itemChecked) {
                    if (newitems.indexOf(val) === -1) {
                        newitems.push(val);
                    }
                } else {
                    _removeByValue(newitems, val);
                }
                console.log("cbchanged");
            },

            isChecked: function(val) {
                return this.items.indexOf(val) >= 0;
            },


            /**
             * Return the object property in the location specified by the labelKey
             * prop. If no labelKey defined, return the object itself.
             */
            labelFor: function(obj) {
                var label = obj;
                if(this.valueKey) {
                    label = obj[this.labelKey];
                }
                return label;
            },

            /**
             * Return the object property in the location specified by the valueKey
             * prop. If no valueKey defined, return the object itself.
             */
            valueFor: function(obj) {
                var val = obj;
                if(this.valueKey) {
                    val = obj[this.valueKey];
                }
                return val;
            },



        }

    });

    return _checkboxlist;

   })(console, window, Vue, axios, TDAR, jQuery);

