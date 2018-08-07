/**
 * TDAR Vue Selectize.
 * 
 * vue-selectize.js
 * 
 * This integrates Selectize with Vue so it can be used as a component.
 */

import Vue from "vue";

Vue.component('selectize', {
    props : [ 'options', 'value' ],
    template : '<select><slot></slot></select>',
    mounted : function() {
        var vm = this;
        var $vm = $(this);
        var $tag = $(this.$el);
        var opt = $.extend({}, $(this.$el).data());
        if (this.options != null) {
            opt.options = this.options;
        }
        var method = $tag.data('config');
        console.log("using init method: " + method);
        var opts = {};
        if (method != undefined) {
            var method_ = window[method];
            // FIXME: There has to be a better way to bind these
            if (window['TDAR']['vuejs'] != undefined) {
                // fixme: make generic to search subtree
                if ($.isFunction(window['TDAR']['vuejs']['collectionwidget'][method])) {
                    method_ = window['TDAR']['vuejs']['collectionwidget'][method];
                }
            }
            if ($.isFunction(window[method])) {
                method_ = window[method];
            }
            if (method_ != undefined) {
                // add options based on method ... here's where we implicitly call initBasicForm
                opts = method_(this);
            } else {
                console.log("init method specified, but not a function");
            }
        }

        this.sel = $(this.$el).selectize(opts).on("change", function() {
            vm.$emit('input', vm.sel.getValue());
        })[0].selectize;
        this.sel.setValue(this.value, true);
    },
    watch : {
        value : function(value) {
            this.sel.setValue(value, true);
        },
        options : function(options) {
            var val = this.sel.getValue();
            this.sel.clearOptions();
            this.sel.addOption(options);
            this.sel.refreshOptions(false);
            this.sel.setValue(val);
        }
    },
    destroyed : function() {
        this.sel.destroy();
    }
})