(function (TDAR, $) {
    'use strict';


    //scan for any inputs with .datepicker class and initialize them.
    var _init = function() {
        $('input.datepicker').each(function(idx, el) {
            $(el).datepicker().on('changeDate', function(ev){
                $(ev.target).datepicker('hide');
            })
        });
    };
    
    //expose public elements
    TDAR.datepicker = {
        "init": _init,
        main : function() {
            TDAR.datepicker.init();
        }

    };

})(TDAR, jQuery);
