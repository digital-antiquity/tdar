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
            },
            statusbar: {
                type: Boolean,
                default: function() {return false}
            }

        },

        mounted: function() {},
        computed: {
            statusMessage: function() {
                var msg = " ";
                var len = this.selectedOptions.length;
                if(len === 1) {
                    msg = "1 item selected";
                } else if (len > 1) {
                    msg = len + " items selected";
                }
                return msg;
            }
        },
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

            selectedOptionsChanged: function(sel) {
                console.log("options changed");

                //instead of changing prop directly, emit new value
                var selopts = []
                for (var i = 0; i < sel.options.length; i++) {
                    if (sel.options[i].selected) {
                        selopts.push(this.valueFor(this.options[i]));
                    }
                }
                this.$emit("listupdate", selopts);
            }
        }
    });

    return _selectlist;

})(console, window, Vue, axios, TDAR, jQuery);
