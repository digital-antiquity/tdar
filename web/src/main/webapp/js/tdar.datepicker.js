(function (TDAR, $) {
    'use strict';

    var _defaults = {dateFormat: "mm/dd/yyyy"}
    //scan for any inputs with .datepicker class and initialize them.
    var _init = function() {
        $('input.datepicker').each(function(idx, el) {
            _bind(el);
        });
    };

    var _bind = function(el) {
        $(el).datepicker({autoclose:true, dateFormat: "mm/dd/yyyy"}).on('changeDate', _handleEvent);
    }

    var _apply = function(el) {
        el.datepicker(_defaults).on('changeDate',_handleEvent);
    }

    var _handleEvent = function(ev) {
        var $t = $(ev.target);
        if ($t.val() != '') {
            $t.attr('placeholder',"");
        }
        $t.datepicker('hide');
        $t.trigger("datechanged");
    }
    
    var _applyHidden = function(el) {
        el.datepicker(_defaults).on('changeDate', _handleEvent).datepicker("hide");
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
