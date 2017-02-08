(function (TDAR, $) {
    'use strict';


    var _init = function() {
        var picker = $('input.datepicker').datepicker();
        picker.on('changeDate', function(ev){
            $(ev.target).datepicker('hide');
        });
    }
    
    //expose public elements
    TDAR.datepicker = {
        "init": _init,
        main : function() {
            TDAR.datepicker.init();
        }

    };

})(TDAR, jQuery);
