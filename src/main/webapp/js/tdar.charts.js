/**
 * additional chart and graph support
 */

(function ($, TDAR) {
    "use strict";

    var _defaults = {
        id: 'chart1',
        rawData: [],
        title: 'A Chart'
    };

    /**
     * Maps an array of objects into a jqplot series
     * 
     * @param data
     *            untranslated array of objects
     * @param colx
     *            name of property which holds x-value or callback that accepts rawval and returns translated value
     * @param coly
     *            name of property which holds y-value or callback that accepts rawval and returns translated val
     * @returns {*} jqplot series - array of [x, y] arrays.
     * @private
     */
    var _toSeries = function (data, colx, coly) {
        var tdata = $.map(data, function (val) {
            var xval = typeof colx === "function" ? colx(val) : val[colx];
            var yval = typeof coly === "function" ? coly(val) : val[coly];

            // this double-walled array is intentional: jqplot expects 2d array but $.map 'unrolls' nested array.
            return [
                [xval, yval]
            ];
        });
        return tdata;
    }

    /**
     * sort a series by date, assuming array in the format [[date1, num1], [date2, num2], ...]
     * 
     * @param a
     * @param b
     * @returns {number}
     * @private
     */
    var _datesort = function (a, b) {
        return a[0] - b[0]
    };

    TDAR.charts = {

        /**
         * Generate admin usage stats graph
         * 
         * @param options
         */
        adminUsageStats: function (options) {
            var opts = $.extend({}, _defaults, options);
            var $chart = $("#" + opts.id);
            var chartOpts = {
                title: $chart[0].title
                // TODO: pull more options from chart container element attributes.
            };
            $.extend(opts, chartOpts);
            var seriesList = [];
            var seriesLabels = [];
            var getxval = function (val) {
                return new Date(val["date"]);
            };

            // first series is the view count of the resource view page
            var viewseries = _toSeries(opts.rawData["view"], getxval, "count");
            viewseries.sort(_datesort);
            seriesLabels.push({label: "Page Views"});

            seriesList.push(viewseries);
            for (var key in opts.rawData.download) {
                var dldata = opts.rawData.download[key], filename = key, dlseries = _toSeries(dldata, getxval, "count");

                dlseries.sort(_datesort);
                seriesList.push(dlseries);
                seriesLabels.push({label: TDAR.ellipsify(filename, 20)});
            }

            var plot1 = $.jqplot(opts.id, seriesList, {
                // FIXME: TDAR.colors should be driven by tdar theme file.
                seriesColors: TDAR.colors || ["#EBD790", "#4B514D", "#2C4D56", "#C3AA72", "#DC7612", "#BD3200", "#A09D5B", "#F6D86B", "#660000", "#909D5B"],
                title: opts.title,
                // stackSeries: true, //jtd: i think stacked looks better, especially when several files involved
                axes: {
                    xaxis: {
                        renderer: $.jqplot.DateAxisRenderer
                    }
                },
                legend: {
                    show: true,
                    location: 'e',
                    placement: 'outsideGrid'
                },
                series: seriesLabels,
                animate: !$.jqplot.use_excanvas,
                seriesDefaults: {
                    renderer: $.jqplot.BarRenderer,
                    pointLabels: {
                        show: true,
                        location: 'n',
                        edgeTolerance: -35
                    },
                    rendererOptions: {
                        // barDirection: 'horizontal' //jtd: horiz bars look better with several series, but note you also have to flip x/y as well.
                        barWidth: 4,
// shadowDepth: 2,
                        // Set varyBarColor to tru to use the custom colors on the bars.
                        fillToZero: true,
                        varyBarColor: false,
                        animation: {
                            speed: 1500
                        }
                    }
                },
                grid: {
                    background: 'rgba(0,0,0,0)',
                    drawBorder: false,
                    shadow: false,
                    gridLineColor: 'none',
                    borderWidth: 0,
                    gridLineWidth: 0,
                    drawGridlines: false
                }

            });
        },

        /**
         * generate color palette of specified size that attempts to compliment specified background color
         * 
         * @param size
         *            number of colors in palette
         * @param bgcolor
         *            in '#rrggbb' format
         */
        generateSeriesColors: function (size, bgcolor) {
            // TODO: all the awesome stuff that the docs say it does
        },

        barGraph : function(props, data, id, config) {
            $.jqplot.config.enablePlugins = true;
            var graphId = 'graph' + id;
            var $graph = $("#" + graphId);
            if (data.length < 1) {
                return;
            } 
            var _defaults = {
                // Only animate if we're not using excanvas (not in IE 7 or IE 8)..
                title: props.title,
                animate: !$.jqplot.use_excanvas,
                seriesDefaults: {
                    renderer: $.jqplot.BarRenderer,
                    pointLabels: {
                        show: true,
                        location: 'n',
                        edgeTolerance: -35
                    },
                    rendererOptions: {
                        // Set varyBarColor to tru to use the custom colors on the bars.
                        varyBarColor: true
                    }
                },
                seriesColors: props.seriesColors,
                grid: {
                    background: 'rgba(0,0,0,0)',
                    drawBorder: false,
                    shadow: false,
                    gridLineColor: 'none',
                    borderWidth: 0,
                    gridLineWidth: 0,
                    drawGridlines: false
                },
                axes: {
                    xaxis: {
                        labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                        renderer: $.jqplot.CategoryAxisRenderer,
                        tickOptions: {
                            fontFamily: 'Georgia',
                            fontSize: '8pt',
                            showGridline: false
                        }
                    },
                    yaxis: {
                        showTicks: false,
                        show: false,
                        showGridline: false
                    }
                },
                highlighter: { show: false },
                cursor: {
                    style: "pointer",
                    showTooltip: false,
                    useAxesFormatters: false
                }
            };
            if (props.xaxis == "log") {
                _defaults.axes.xaxis.renderer = $.jqplot.LogAxisRenderer;
            }
            if (props.xaxis == "date") {
                _defaults.axes.xaxis.renderer = $.jqplot.DateAxisRenderer;
            }
            if (props.yaxis == "log") {
                _defaults.axes.yaxis.renderer = $.jqplot.LogAxisRenderer;
            }
            if (props.yaxis == "date") {
                _defaults.axes.yaxis.renderer = $.jqplot.DateAxisRenderer;
            }
            if (props.rotate != undefined  && props.rotate != 0) {
                _defaults.axes.xaxis.tickRenderer= $.jqplot.CanvasAxisTickRenderer;
                _defaults.axes.xaxis.tickOptions.angle = props.rotate;
            }

            if (config != undefined) {
                $.extend(true, _defaults,config);
            }

            var plot = $.jqplot(graphId, [data], _defaults);
            var context = "";

            if (props.context != undefined && props.context == false) {
                context = "&useSubmitterContext=true";
            }
            $graph.bind('jqplotDataClick',
                    function (ev, seriesIndex, pointIndex, data) {
                    $('#info1').html('series: '+seriesIndex+', point: '+pointIndex+', data: '+data+ ', pageX: '+ev.pageX+', pageY: '+ev.pageY);
                    window.location.href="/search/results?" + props.searchKey + "=" +data[2] + context;
                    }
            );
        },


        pieChart : function(props, data, id, config) {
            $.jqplot.config.enablePlugins = true;
            var graphId = 'graph' + id;
            var $graph = $("#" + graphId);
            if (data.length < 1) {
                return;
            } 
            
            var _defaults = {
                // Only animate if we're not using excanvas (not in IE 7 or IE 8)..
                fontSize: 10,
                title: props.title,
                animate: !$.jqplot.use_excanvas,
                seriesDefaults: {
                    renderer: jQuery.jqplot.PieRenderer,
                    rendererOptions: {
                        fill: true,
                        animate: !$.jqplot.use_excanvas,
                        showDataLabels: true,
                        // Add a margin to seperate the slices.
                        sliceMargin: 4,
                        // stroke the slices with a little thicker line.
                        lineWidth: 5,
                        padding: 5,
                        dataLabels: 'value'
                    }
                },
                seriesColors: props.seriesColors,
                grid: {
                    background: 'rgba(0,0,0,0)',
                    drawBorder: false,
                    shadow: false,
                    gridLineColor: 'none',
                    borderWidth: 0,
                    gridLineWidth: 0,
                    drawGridlines: false
                },
                legend: {
                    renderer: $.jqplot.EnhancedLegendRenderer,
                    show: true,
                    location: 'e',
                    fontSize: 10,
                    showSwatch: true
                },
                cursor: {
                    style: "pointer",
                    showTooltip: false,
                    useAxesFormatters: false
                },
                highlighter: {
                    show: true,
                    formatString: '%s (%s)',
                    tooltipLocation: 'ne',
                    useAxesFormatters: false
                }
            };

            if (config != undefined) {
                $.extend(true, _defaults,config);
            }

            var plot = $.jqplot(graphId, [data], _defaults);
            var context = "";
            if (props.context != undefined) {
                context = "&useSubmitterContext=true";
            }
            $graph.bind('jqplotDataClick',
                    function (ev, seriesIndex, pointIndex, data) {
                    $('#info1').html('series: '+seriesIndex+', point: '+pointIndex+', data: '+data+ ', pageX: '+ev.pageX+', pageY: '+ev.pageY);
                    window.location.href="/search/results?" + props.searchKey + "=" +data[2] + context;
                    }
            );
        },

        lineChart : function(props, labels, data, id, config) {
            $.jqplot.config.enablePlugins = true;
            var graphId = 'graph' + id;
            var $graph = $("#" + graphId);
            if (data.length < 1) {
                return;
            }

            var _defaults = {
                title: props.title,
                animate: !$.jqplot.use_excanvas,
                seriesColors: props.seriesColors,
                axes: {
                    xaxis: {},
                    yaxis: {min: 0}
                },
                grid: {
                    background: 'rgba(0,0,0,0)',
                    shadow: false,
                    borderWidth: 0,
                    gridLineWidth: 0,
                },
                highlighter: {
                    show: true,
                    sizeAdjust: 7.5
                },
                legend: {
                    show: true,
                    placement: 'outsideGrid',
                    labels: labels,
                    location: 'ne',
                    rowSpacing: '0px'
                },
                seriesDefaults: {lineWidth: 1, showLabel: true, showMarker: false}
            };
            
            if (props.xaxis == "log") {
                _defaults.axes.xaxis.renderer = $.jqplot.LogAxisRenderer;
            }
            if (props.xaxis == "date") {
                _defaults.axes.xaxis.renderer = $.jqplot.DateAxisRenderer;
            }
            if (props.yaxis == "log") {
                _defaults.axes.yaxis.renderer = $.jqplot.LogAxisRenderer;
            }
            if (props.yaxis == "date") {
                _defaults.axes.yaxis.renderer = $.jqplot.DateAxisRenderer;
            }
            if (props.rotate != undefined  && props.rotate != 0) {
                _defaults.axes.xaxis.tickRenderer= $.jqplot.CanvasAxisTickRenderer;
                _defaults.axes.xaxis.tickOptions.angle = props.rotate;
            }

            if (config != undefined) {
                $.extend(true, _defaults,config);
            }

            var plot = $.jqplot(graphId, data , _defaults); 


        },
        worldMap: function() {
            
                $('.worldmap').maphilight({
                    fade: true,
                    groupBy: "alt",
                    strokeColor: '#ffffff'
                });

                $(".worldmap").delegate('area', 'mouseover', function (e) {
                    $('[iso=' + $(this).attr('iso') + ']').each(function (index, val) {
                        hightlight(true, val);
                    });
                });

                $(".worldmap").delegate('area', 'mouseout', function (e) {
                    $('[iso=' + $(this).attr('iso') + ']').each(function (index, val) {
                        hightlight(false, val);
                    });
                });

            },

           highlight: function (on, element) {
                var data = $(element).data('maphilight') || {};
                if (on) {
                    data.oldFillColor = data.fillColor;
                    data.oldFillOpacity = data.fillOpacity;
                    data.oldStrokeColor = data.strokeColor;
                    data.oldStrokeWidth = data.strokeWidth;

                    data.fillColor = '4B514D';
                    data.fillOpacity = .5;
                    data.strokeColor = '111111';
                    data.strokeWidth = '.6';
                } else {
                    data.fillColor = data.oldFillColor;
                    data.fillOpacity = data.oldFillOpacity;
                    data.strokeColor = data.oldStrokeColor;
                    data.strokeWidth = data.oldStrokeWidth;
                }
                $(element).data('maphilight', data).trigger('alwaysOn.maphilight');
        }
    }

})(jQuery, TDAR);
