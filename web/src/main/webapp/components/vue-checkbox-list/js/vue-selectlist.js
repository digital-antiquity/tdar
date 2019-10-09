TDAR.vuejs.selectlist = (function(console, ctx, Vue, axios, TDAR, _jq) {
    "use strict";
    var _selectlist = Vue.component('selectlist', {
        // name: "SelectList",
        template: "#selectlist-template",
        model: {
            prop: "selectedOptions",
            event: "listupdate"
        },

        props: {
            blankrow: {
                type: Boolean,
                default: function() {
                    return false;
                }
            },
            name: {
                type: String,
                required: true
            },
            options: {
                type: [Object, Array],
                required: true
            },
            selectedOptions: {
                required: false,
                type: Array,
                default: function() {
                    return [];
                }
            },
            labelKey: {
                type: String,
                required: false,
                default: "label"
            },

            valueKey: {
                type: String,
                required: false,
                default: "value"
            },

            size: {
                type: Number,
                required: false,
                default: 1
            }
        },

        mounted: function() {},
        computed: {},
        methods: {
            labelFor: function(opt) {
                return opt[this.labelKey];
            },

            valueFor: function(opt) {
                return opt[this.valueKey];
            },

            isSelected: function(val) {
                var selo = this.selectedOptions;
                var idx = selo.indexOf(val);
                return idx > -1;
            },

            selectedOptionsChanged: function(sel) {
                console.log("options changed");

                this.selectedOptions.splice(0, this.selectedOptions.length);
                for(var i = 0; i < sel.options.length; i++) {
                    if(sel.options[i].selected) {
                        this.selectedOptions.push(this.valueFor(this.options[i]));
                    }
                }


            }
        }
    });

    return _selectlist;

})(console, window, Vue, axios, TDAR, jQuery);
