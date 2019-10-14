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
                type:  [Array, Object, String],
                default: function() {
                    return [];
                }
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
                var ret = opt;
                if(!!this.labelKey) {
                    ret = opt[this.labelKey]
                }
                return ret;
            },

            /**
             * Given an object, return the property of that object specified
             * by the valueKey.  If this control has no valueKey, return the
             * object itself.
             * @param opt
             * @returns {opt|*}
             */
            valueFor: function(opt) {
                var ret = opt;
                if(!!this.valueKey) {
                    ret = opt[this.valueKey]
                }
                return ret;
            },

            isSelected: function(val) {
                var selo = this.selectedOptions;
                var idx = selo.indexOf(val);
                return idx > -1;
            },

            selectedOptionsChanged: function(evt, sel) {
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
