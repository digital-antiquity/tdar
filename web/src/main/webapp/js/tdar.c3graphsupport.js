TDAR.c3graphsupport = {};

TDAR.c3graphsupport = (function(console, $, ctx) {
    var _resourceBarGraphClick = function(d, element) {
        var $parent = $("#resourceBarGraph");
        var source = JSON.parse($($parent.data("source")).html());
        window.location.href = TDAR.uri( "/search/results?resourceTypes=" + source[d.x].key);
    }

    var _dashboardResourcePieChartClick = function(d, element) {
        var $parent = $("#resourceTypeChart");
        var source = JSON.parse($($parent.data("columns")).html());
        window.location.href = TDAR.uri("/search/results?useSubmitterContext=true&resourceTypes=" + source[d.index][2]);
    }

    var _dashboardStatusPieChartClick = function(d, element) {
        var $parent = $("#statusChart");
        var source = JSON.parse($($parent.data("columns")).html());
        window.location.href = TDAR.uri( "/search/results?useSubmitterContext=true&status=" + source[d.index][2]);
    }
    
    return {
        resourceBarGraphClick : _resourceBarGraphClick,
        dashboardStatusPieChartClick : _dashboardStatusPieChartClick,
        dashboardResourcePieChartClick : _dashboardResourcePieChartClick
    }
    
})(console, jQuery, window);