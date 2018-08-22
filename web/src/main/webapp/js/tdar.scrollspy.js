TDAR.scrollspy = (function(TDAR, $) {
    'use strict';

    var _init = function() {
        var $nav = $('#subnavbar');
        if ($nav.length == 0) {
            return;
        }
        console.log('binding scrollspy');
        $(document).ready(function() {
            $(window).scroll(function(e) {
                if ($nav.offset() == undefined) {
                    return;
                }
                var scrollTop = $(window).scrollTop();
                var viewableOffset = $nav.offset().top - scrollTop;
                var footerOffset = $("#footer").offset().top - scrollTop;
                var viewableBottom = viewableOffset + $nav.height();
                if (viewableOffset < 10 && scrollTop > 189) {
                    $nav.removeClass('hidden');
                    // $nav.css({'width':$nav.width()});
                    $nav.addClass('affix');
                }
                if (footerOffset < viewableBottom) {
                    $nav.addClass('hidden');
                } else if (scrollTop < 189) {
                    $nav.removeClass('hidden');
                    $nav.removeClass('affix');
                }
            });
        });
    }

    return {
        "init" : _init,
        main : function() {
             TDAR.scrollspy.init();
        }
    }

})(TDAR, jQuery);