/**
 * additional chart and graph support
 */

(function($, TDAR){
    "use strict";
    TDAR.namespace("charts");

    var _defaults = {
        id: 'chart1',
        rawData: [],
        title: 'A Chart'
    };

    //convert an array of rawdata into a series as expected by jqplog
    //colx - name of column which holds x-value or callback in form of function(rawval){}
    //colx - name of column which holds x-value or callback in form of function(rawval){}
    var _toSeries = function(data, colx, coly) {
        var tdata = $.map(data, function(val){
            var xval = typeof colx === "function" ? colx(val) : val[colx];
            var yval = typeof coly === "function" ? coly(val) : val[coly];

            //this double-walled array is intentional: jqplot expects 2d array but $.map 'unrolls' nested array.
            return [[xval, yval]];
        });
        return tdata;
    }
    //sort a series by date,  assuming array in the format [[date1, num1], [date2, num2], ...]
    var _datesort = function(a,b) {return a[0] - b[0]};


    TDAR.charts = {
        adminUsageStats: function(options) {
            var opts = $.extend({}, _defaults, options);
            var $chart = $("#" + opts.id);
            var chartOpts = {
                title: $chart[0].title
                //TODO: pull more options from chart container element attributes.
            };
            $.extend(opts, chartOpts);
            var seriesList = [];
            var seriesLabels = [];
            var getxval = function(val) {
                return new Date(val["date"]);
            };

            //first series is the view count of the resource view page
            var viewseries = _toSeries(opts.rawData["view"], getxval, "count");
            viewseries.sort(_datesort);
            seriesLabels.push({label: "Page Views"});

            seriesList.push(viewseries);
            for (var key in opts.rawData.download) {
                var dldata = opts.rawData.download[key],
                    filename = key,
                    dlseries = _toSeries(dldata, getxval, "count");

                dlseries.sort(_datesort);
                seriesList.push(dlseries);
                seriesLabels.push({label: TDAR.ellipsify(filename, 20)});
            }

            var plot1 = $.jqplot(opts.id, seriesList, {
                //FIXME: TDAR.colors should be driven by tdar theme file.
                seriesColors: TDAR.colors || ["#EBD790","#4B514D","#2C4D56","#C3AA72","#DC7612","#BD3200","#A09D5B","#F6D86B","#660000","#909D5B"],
                title: opts.title,
                //stackSeries: true,  //jtd: i think stacked looks better, especially when several files involved
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
                seriesDefaults:{
                    renderer:$.jqplot.BarRenderer,
                    pointLabels: { 
                        show: true,
                        location: 'n',
                        edgeTolerance: -35
                    },
                    rendererOptions: {
                        //barDirection: 'horizontal'  //jtd: horiz bars look better with several series, but note you also have to flip x/y as well.
                        barWidth: 4,
//                        shadowDepth: 2,
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
                    borderWidth:0,
                    gridLineWidth: 0,
                    drawGridlines:false
              }
                
            });
        },

        generateSeriesColors: function(size, hexBackground) {

        }
    }


})(jQuery, TDAR);
