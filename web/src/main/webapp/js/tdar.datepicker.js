(function (TDAR, $) {
    'use strict';


    //scan for any inputs with .datepicker class and initialize them.
    var _init = function() {
        $('input.datepicker').each(function(idx, el) {
            _bind(el);
        });
    };

    var _bind = function(el) {
        $(el).datepicker().on('changeDate', function(ev){
            $(ev.target).datepicker('hide');
        })
    }
    
    //expose public elements
    TDAR.datepicker = {
        "init": _init,
        "bind": _bind,
        main : function() {
            TDAR.datepicker.init();
        }

    };

})(TDAR, jQuery);
