/**
 * common functions to the tDAR homepage
 */
TDAR.menu = (function(console, $, ctx) {
    "use strict";
    function _init() {
        $("#welcome-menu a").click(function() {
            $(".welcome-drop").toggle();
            return false;
        });

        // for the last 60 pixels of the searchboxes, make it submit the parent form
        $(".searchbox").click(function(e) {
            var $t = $(e.target);
            var ar = $(e.target).offset().left + $t.width() - 60;
            if (e.pageX > ar && $t.val() != '') {
                $t.parents("form").submit();
            }
        });
        $(document).click(function() {
            $('.welcome-drop').hide();
        });

        TDAR.common.applyWatermarks($('form.searchheader, form[name=searchheader]'));
    }

    return {
        "init" : _init,
        "main": _init
    };
})(console, jQuery, window);
