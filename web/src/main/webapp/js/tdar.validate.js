(function(TDAR, $) {
    'use strict';

    /**
     * if there's a data attribute to associate with valdiateMethod, then see if it's a function. if it's not a function, then call validate() plain.
     */
    
    
    
    var _init = function() {
        $("form.tdarvalidate").each(function() {
            var $t = $(this);
            var method = $t.data('validateInitMethod');
            if (method) {
                if ($.isFunction(window[method])) {
                    window[method]();
                    $t.data("tdar-validate-status","valid-custom");
                } else {
                    console.log("validate method specified, but not a function");
                    $t.data("tdar-validate-status","failed-invalid-method");
                }
            } else {
                $t.validate();
                $t.data("tdar-validate-status","valid-default");
            }
        });
    };

    TDAR.validate = {
        "init" : _init,
    }

})(TDAR, jQuery);
$(function() {
    TDAR.validate.init();
});
