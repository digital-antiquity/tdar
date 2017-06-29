(function (TDAR, $) {
    'use strict';

    var _defaults = {dateFormat: "mm/dd/yy"}
    //scan for any inputs with .datepicker class and initialize them.
    var _init = function() {
        $('input.datepicker').each(function(idx, el) {
            _bind(el);
        });
    };

    var _bind = function(el) {
        $(el).datepicker({autoclose:true}).on('changeDate', function(ev){
            $(ev.target).datepicker('hide');
        });
    }

    var _apply = function(el) {
        el.datepicker(_defaults).on('changeDate', function(ev){
            var $t = $(ev.target);
            $t.datepicker('hide');
            
        });
    }

    var _applyHidden = function(el) {
        el.datepicker(_defaults).on('changeDate', function(ev){
            var $t = $(ev.target);
            if ($t.val() != '') {
                $t.attr('placeholder',"");
            }
            $t.datepicker('hide');
        }).datepicker("hide");
    }

    
    
    //expose public elements
    TDAR.datepicker = {
        "init": _init,
        "bind": _bind,
        "apply": _apply,
        "applyHidden": _applyHidden,
        "defaults": _defaults,
        main : function() {
            TDAR.datepicker.init();
        }

    };

})(TDAR, jQuery);
