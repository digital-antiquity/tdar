TDAR.c3graphsupport = {};

TDAR.c3graphsupport = (function(console, $, ctx) {
    var _resourceBarGraphClick = function(d, element) {
        var $parent = $("#resourceBarGraph");
        var source = JSON.parse($($parent.data("source")).html());
        window.location.href = "/search/results?resourceTypes=" + source[d.x].key
    }
    
    return {
        resourceBarGraphClick : _resourceBarGraphClick
    }
    
})(console, jQuery, window);