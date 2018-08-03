/**
 * tdar.c3graphsupport
 */

const core = require("./tdar.core");

    var _resourceBarGraphClick = function(d, element) {
        var $parent = $("#resourceBarGraph");
        var source = JSON.parse($($parent.data("source")).html());
        var uri = "/search/results?resourceTypes=" + source[d.x].key;
        window.location.href = _getClickPath(uri);
    }

    /**
     * gets the baseUri for clicking... needs to be 'overridable' because of the www.tdar.org calls
     */
    var _getClickPath = function(uri) {
        var clickPath = $("#clickPath[href]");
        if (clickPath != undefined && clickPath.length > 0) {
            uri = clickPath.attr('href') + uri;
        } else {
            uri = core.uri( uri );
        }
        return uri;
    }
        
    var _dashboardResourcePieChartClick = function(d, element) {
        var $parent = $("#resourceTypeChart");
        var source = JSON.parse($($parent.data("columns")).html());
        window.location.href = core.uri("/search/results?useSubmitterContext=true&types=RESOURCE&resourceTypes=" + source[d.index][2]);
    }

    var _dashboardStatusPieChartClick = function(d, element) {
        var $parent = $("#statusChart");
        var source = JSON.parse($($parent.data("columns")).html());
        window.location.href = core.uri( "/search/results?useSubmitterContext=true&types=RESOURCE&status=" + source[d.index][2]);
    }
    
    module.exports =  {
        resourceBarGraphClick : _resourceBarGraphClick,
        dashboardStatusPieChartClick : _dashboardStatusPieChartClick,
        dashboardResourcePieChartClick : _dashboardResourcePieChartClick,
        getClickPath : _getClickPath
    }
    
