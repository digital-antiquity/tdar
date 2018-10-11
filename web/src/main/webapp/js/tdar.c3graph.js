/**
 * TDAR.c3graph
 * 
 * This library is designed to provide a pass-through translation between the c3 graphing library and tDAR. In general, all configuration is done through data
 * attributes on DIV elements
 * 
 * look at _initJSON for detailed configuration options. But, in general, the most important data attributes are: data-table -- the ID of a table to use for the
 * data from the graph data-json -- the ID of an element that has an array of JSON data that can be used along with a set of keys to produce graphs from raw
 * json. (customized JSON options)
 * 
 * data-rows -- the ID of an element that has JSON that should be used for row-based data data-columns -- the ID of an element that has the json that should be
 * used for column-based data
 * 
 * 
 */

const c3 = require("c3");
const c3graphsupport = require("./tdar.c3graphsupport");
const common = require("./tdar.common");
require("c3/c3.min.css");

var _getColors = function() {
    var c3colors = $("#c3colors");
    var c3colorsobj = undefined;

    if (c3colors.length > 0) {
        c3colorsobj = JSON.parse(c3colors.html());
    }
    return c3colorsobj;
}

/**
 * To create a bar-graph, look for a class of "barChart" on divs.
 */
var _initBarChart = function() {
    var c3colors = _getColors();
    $(".barChart").each(function() {
        var $parent = $(this);
        var $table = $($parent.data("table"));

        var cdata = {
            data : {
                x : 'label',
                type : 'bar',
                keys : {
                    x : 'label',
                    value : [ 'count' ]
                },
                tooltip : {
                    show : false
                },
                labels : {
                    show : true
                }
            },
            legend : {
                hide : true
            },
            tooltip : {
                show : false
            },
            axis : {
                x : {
                    show : true,
                    type : 'category'
                },
                y : {
                    show : false
                }
            },
            bar : {
                width : {
                    ratio : .8
                // this makes bar width 50% of length between ticks
                }
            }
        };
        if ($parent.data('colorcategories') != undefined) {
            if (c3colors != undefined && c3colors.length > 0) {
                cdata.data.color = function(c, d) {
                    return c3colors[d.index];
                }
            }
        }

        _initJson($parent, cdata);
        // console.log(JSON.stringify(cdata));
        var chart = c3.generate(cdata);
    });
};

/**
 * To create a bar-graph, look for a class of "barChart" on divs.
 */
var _initGaugeChart = function() {
    $(".gaugeChart").each(function() {
        var $parent = $(this);
        var _defaultColor = {
            pattern : [ '#a09d5b', '#f6d86b', '#dc7612', '#bd3200' ], // the three color levels for the percentage values.
            threshold : {
                // unit: 'value', // percentage is default
                // max: 200, // 100 is default
                values : [ 30, 60, 90, 100 ]
            }
        }
        var cdata = {
            data : {
                columns : [ [ 'Usage', $parent.data("val") ] ],
                type : 'gauge'
            },
            gauge : {
                label : {
                    format : function(value, ratio) {
                        return value + "%";
                    },
                    show : false
                // to turn off the min/max labels.
                },
                units : ' %',
            // width: 39 // for adjusting arc thickness
            },
            tooltip : {
                format : {
                    value : function(value, ratio, id, index) {
                        return value + "%";
                    }
                }
            },
            color : _defaultColor,
            size : {
                height : $parent.data("height"),
                width : $parent.data("width")
            }
        };

        _initJson($parent, cdata);

        if ($parent.data("overridecolors") == true) {
            cdata.color = _defaultColor;
        }
        // console.log(JSON.stringify(cdata));
        var chart = c3.generate(cdata);
    });
};

/**
 * Create area chart
 */
var _initAreaGraph = function() {
    $(".areaChart").each(function() {
        var $parent = $(this);
        var $table = $($parent.data("table"));

        var cdata = {
            data : {
                x : 'label',
                type : 'area',
                keys : {
                    x : 'label',
                    value : [ 'count' ]
                },
                labels : {
                    show : true
                }
            },
            legend : {
                hide : true
            },
            tooltip : {
                show : false
            },
            axis : {
                x : {
                    show : true,
                    type : 'category'
                },
                y : {
                    show : false
                }
            },
        };
        _initJson($parent, cdata);
        var chart = c3.generate(cdata);
    });
};

var _initLineGraph = function() {
    $(".lineGraph").each(function() {
        var $parent = $(this);
        var $table = $($parent.data("table"));

        var cdata = {
            data : {
                x : 'label',
                type : 'line',
                keys : {
                    x : 'label',
                    value : [ 'count' ]
                },
            },
            point : {
                show : false
            },
            legend : {
                hide : false
            },
            tooltip : {
                show : true
            },
            axis : {
                x : {
                    show : true,
                    type : 'timeseries',
                    tick : {
                        format : '%Y-%m-%d',
                        culling : {
                            culling : true,
                            max : 5
                        }
                    }
                },
                y : {
                    show : true
                }
            },
        };

        if ($table) {
            var data = new Array();
            for (var i = 0; i < $("th", $table).length; i++) {
                data.push(new Array());
            }

            var rows = $("tr", $table);
            var max = rows.length - 1;
            if ($("td", rows[max]).length == 1) {
                max--;
            }
            for (var i = max; i >= 0; i--) {
                var row = $("td, th", rows[i]);
                for (var j = 0; j < row.length; j++) {
                    var d = $(row[j]).text().trim();
                    var d_ = d.replace(/\,/g, '');
                    if (i == 0) {
                        data[j].unshift(d);
                    } else if (parseInt(d_) && j != 0) {
                        data[j][max - i] = parseInt(d_);
                    } else {
                        data[j][max - i] = d;
                    }
                }
            }
            cdata.data.columns = data;
            cdata.data.x = 'Date';
        }
        _initJson($parent, cdata);
        var chart = c3.generate(cdata);
    });
};

var _initJson = function($parent, cdata) {
    if ($parent.data('x')) {
        cdata.data.x = $parent.data('x');
    }
    var id = "#" + $parent.attr("id");
    cdata.bindto = id;
    var c3colors = _getColors();
    if (c3colors) {
        cdata.color = {};
        cdata.color.pattern = c3colors;
    }

    var clickname = $parent.data("click");
    if ($.isFunction(window[clickname])) {
        console.log(window[clickname]);
        cdata.data.onclick = window[clickname];
    }

    if ($.isFunction(c3graphsupport[clickname])) {
        cdata.data.onclick = c3graphsupport[clickname];
    }
    if ($parent.data("legend-position") != undefined) {
        if (cdata.legend == undefined) {
            cdata.legend = {};
        }
        cdata.legend.position = $parent.data("legend-position");
    }

    if ($parent.data("source")) {
        var source = JSON.parse($($parent.data("source")).html());
        cdata.data.json = source;
        cdata.data.keys = {};
        cdata.data.keys.x = $parent.data('x');
        // cdata.data.names = {'label' : 'label'};
        if ($parent.data('values')) {
            cdata.data.keys.value = $parent.data('values').split(",");
        }
    }

    if ($parent.data("legend")) {
        if ($parent.data("legend") === 'true') {
            cdata.legend.hide = true;
        } else {
            cdata.legend.hide = false;
        }
    }

    if ($parent.data("xtype")) {
        cdata.axis.x.type = $parent.data("xtype");
    }

    if ($parent.data("columns")) {
        var source = JSON.parse($($parent.data("columns")).html());
        cdata.data.columns = source;
    }
    if ($parent.data("yaxis") && $parent.data("yaxis") === 'log') {
        if (cdata.axis == undefined) {
            cdata.axis = {};
        }
        if (cdata.axis.y == undefined) {
            cdata.axis.y = {};
        }

        cdata.axis.y.tick = {
            format : function(d) {
                return Math.pow(10, d).toFixed(2);
            }
        };

        var key = $parent.data("values");
        // take the JSON value and convert it to the Log(10) value
        if (cdata.data.json && key) {
            cdata.data.json.forEach(function(r) {
                r[key] = Math.log(r[key]) / Math.LN10;
            });
        }

        if (cdata.data.labels == undefined) {
            cdata.data.labels = {};
        }

        if (cdata.data.labels.format == undefined) {
            cdata.data.labels.format = {};
        }

        cdata.data.labels.format[$parent.data('values')] = _reverseLog;
        console.log(cdata.data.labels);
    }

}

var _reverseLog = function(d, id) {
    return Math.pow(10, d).toFixed(0);
};

var _initPieChart = function() {
    $(".pieChart").each(function() {
        var $parent = $(this);

        var cdata = {
            data : {
                type : 'pie',
            },
            tooltip : {
                format : {
                    value : function(value, ratio, id, index) {
                        return common.formatNumber(value) + " (" + (ratio * 100.00).toFixed(2) + "%)";
                    }
                }
            }
        }
        var data = new Array();
        var $table = $($parent.data("table"));
        if ($table) {
            var rows = $("tr", $table);
            var countCol = $parent.data("val");
            var ccInt = -1;
            var labCol = $parent.data("label");
            var labInt = -1;
            for (var i = 0; i < rows.length; i++) {
                var row = $("td, th", rows[i]);

                if (i == 0) {
                    for (var j = 0; j < row.length; j++) {
                        var d = $(row[j]).text().trim();
                        if (ccInt == -1 && d.valueOf() == countCol.valueOf()) {
                            ccInt = j;
                        }
                        if (labInt == -1 && d.valueOf() == labCol.valueOf()) {
                            labInt = j;
                        }
                    }
                    continue;
                }
                var d = $(row[ccInt]).text().trim();
                var ld = $(row[labInt]).text().trim();
                var rd = new Array();
                rd[0] = ld;
                if (d != undefined) {
                    d = d.replace(/\,/g, '');
                }
                rd[1] = parseInt(d);
                data.push(rd);
            }
            cdata.data.columns = data;

        }
        ;
        _initJson($parent, cdata);
        var chart = c3.generate(cdata);
    });
}

module.exports = {
    initPieChart : _initPieChart,
    initLineGraph : _initLineGraph,
    initBarChart : _initBarChart,
    initAreaGraph : _initAreaGraph,
    initGaugeChart : _initGaugeChart,
    main : function() {
        _initPieChart();
        _initLineGraph();
        _initBarChart();
        _initAreaGraph();
        _initGaugeChart();
    }
}