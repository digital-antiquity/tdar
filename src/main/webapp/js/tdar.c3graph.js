TDAR.c3graph = {};
/**
 * This library is designed to provide a pass-through translation between the c3 graphing library and tDAR.  In general, all configuration is done through data attributes on DIV elements
 * 
 * look at _initJSON for detailed configuration options.  But, in general, the most important data attributes are:
 *  data-table -- the ID of a table to use for the data from the graph
 *  data-json -- the ID of an element that has an array of JSON data that can be used along with a set of keys to produce graphs from raw json.
 *  (customized JSON options)
 *  
 *  data-rows -- the ID of an element that has JSON that should be used for row-based data
 *  data-columns -- the ID of an element that has the json that should be used for column-based data
 * 
 * 
 */
TDAR.c3graph = (function(console, $, ctx) {

    /**
     * To create a bar-graph, look for a class of "barChart" on divs.  
     */
	var _initBarChart = function() {
		$(".barChart").each(function() {
			var $parent = $(this);
			var $table = $($parent.data("table"));

			var cdata = {
				data: {
					x: 'label',
					type: 'bar',
					keys: {
						x: 'label',
						value: ['count']
					},
					tooltip: {
						show: false
					},
					labels: true,
				},
				legend: {
					hide: true
				},
				tooltip: {
					show: false
				},
				axis: {
					x: {
						show: true,
						type: 'category'
					},
					y: {
						show: false
					}
				},
				bar: {
					width: {
						ratio: .8 // this makes bar width 50% of length between ticks
					}
				}
			};
			_initJson($parent, cdata);
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
				data: {
					x: 'label',
					type: 'area',
					keys: {
						x: 'label',
						value: ['count']
					},
					labels: true
				},
				legend: {
					hide: true
				},
				tooltip: {
					show: false
				},
				axis: {
					x: {
						show: true,
						type: 'category'
					},
					y: {
						show: false
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
				data: {
					x: 'label',
					type: 'line',
					keys: {
						x: 'label',
						value: ['count']
					},
				},
				legend: {
					hide: false
				},
				tooltip: {
					show: true
				},
				axis: {
					x: {
						show: true,
						type: 'timeseries',
						tick: {
							format: '%Y-%m-%d',
							culling: {
								culling: true,
								max: 5
							}
						}
					},
					y: {
						show: true
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
						if (i == 0) {
							data[j].unshift(d);
						} else if (parseInt(d) && j != 0) {
							data[j][max - i] = parseInt(d);
						} else {
							data[j][max - i] = d;
						}
					}
				}
				cdata.data.columns = data;
				cdata.data.x = 'Date';
			}
			_initJson($parent, cdata);
			varchart = c3.generate(cdata);
		});
	};

	var _initJson = function($parent, cdata) {
		if ($parent.data('x')) {
			cdata.data.x = $parent.data('x');
		}
		var id = "#" + $parent.attr("id");
		cdata.bindto = id;
		if ($parent.data("source")) {
			var source = JSON.parse($($parent.data("source")).html());
			cdata.data.json = source;
			cdata.data.keys = {};
			cdata.data.keys.x = $parent.data('x');
			
			var clickname = $parent.data("click");
			if ($.isFunction(window[clickname])) {
			    console.log(window[clickname]);
			    cdata.data.onclick = window[clickname];
			}
			
            if ($.isFunction(TDAR.c3graphsupport[clickname])) {
                cdata.data.onclick = TDAR.c3graphsupport[clickname];
            }
			
			if ($parent.data('values')) {
				cdata.data.keys.value = $parent.data('values').split(",");
			}
		}
		
		if ($parent.data("columns")) {
			var source = JSON.parse($($parent.data("columns")).html());
			cdata.data.columns = source;
		}
	}

	var _initPieChart = function() {
		$(".pieChart").each(function() {
			var $parent = $(this);

			var cdata = {
				data: {
					type: 'pie',
				},
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
					rd[1] = parseInt(d);
					data.push(rd);
				}
				cdata.data.columns = data;

			};
			_initJson($parent, cdata);
			var chart = c3.generate(cdata);
		});
	}

	return {
		initPieChart: _initPieChart,
		initLineGraph: _initLineGraph,
		initBarChart: _initBarChart,
		initAreaGraph: _initAreaGraph
	}
})(console, jQuery, window);
$(function() {
	TDAR.c3graph.initPieChart();
	TDAR.c3graph.initLineGraph();
	TDAR.c3graph.initBarChart();
	TDAR.c3graph.initAreaGraph();
});
