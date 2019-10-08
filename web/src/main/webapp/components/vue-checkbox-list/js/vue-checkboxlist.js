
TDAR.vuejs.checkboxlist = (function(console, ctx, Vue, axios, TDAR, _jq) {
    "use strict";
    console.log('vue-checkboxlist.js begin');

    var _checkboxlist = Vue.component('checkboxlist', {
        // name: "CheckboxList",
        template : "#checkboxlist-template",

        // FIXME: Is it really necessary to customize the component v-model directive here?
        // FIXME: Even if it is,  'items' and 'listupdate' are lame names.
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
            }
        },

        data: function() {
            return {};
        },

        methods: {
            cbchanged: function(val, itemChecked) {
                //compare previsu
                if (itemChecked) {
                    if (this.items.indexOf(val) === -1) {
                        this.items.push(val);
                    }
                } else {
                    _removeByValue(this.items, val);
                }
                console.log("cbchanged");
            },

            isChecked: function(val) {
                return this.items.indexOf(val) >= 0;
            },


            labelFor: function(opt) {
                return opt[this.labelKey];
            },

            valueFor: function(opt) {
                return opt[this.valueKey];
            },



        }

    });

    return _checkboxlist;

   })(console, window, Vue, axios, TDAR, jQuery);

