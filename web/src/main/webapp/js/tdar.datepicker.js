(function (TDAR, $) {
    'use strict';

    var _defaults = {format: "mm/dd/yyyy"}
    //scan for any inputs with .datepicker class and initialize them.
    var _init = function() {
        $('input.datepicker').each(function(idx, el) {
            _bind(el);
        });
    };

    var _bind = function(el) {
        var format = "mm/dd/yyyy"
        if($(el).data("date-format")) {
            format = $(el).data("date-format");
        }
        $(el).datepicker({autoclose:true, format: format}).on('changeDate', _handleEvent);
    }

    var _apply = function(el) {
        el.datepicker(_defaults).on('changeDate',_handleEvent);
    }

    var _handleEvent = function(ev) {
        var $t = $(ev.target);
        if ($t.val() != '') {
            $t.attr('placeholder',"");
        }
        console.log('handling changeDateEvent', $t.val());
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
