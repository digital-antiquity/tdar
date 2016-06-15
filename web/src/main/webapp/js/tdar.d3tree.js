TDAR.d3tree = {};

TDAR.d3tree = (function(console, $, ctx) {

	//basically a way to get the path to an object
	function searchTree(obj,search,path){
        var found = [];
        allNodes.forEach(function(n) {
    		if(n.displayName.toLowerCase().indexOf( search) > -1){ //if search is found return, add the object to the path and return it
    			found.push(n);
    		}
        });
        return found;
	}
    var div, root, tree, svg;
	var duration = 750;
	var diameter = 960;
    var diagonal = d3.svg.diagonal().projection(function(d) { return [d.y, d.x]; });
    var allNodes = [];
    var panSpeed = 200;
   var panBoundary = 20; // Within 20px from edges will pan when dragging.
    
    function _init() {
        root = JSON.parse($("#ontId").text());
        
        if (!root || !root.children) {
            return;
        }
	div = d3.select("#d3")
		.append("div") // declare the tooltip div
		.attr("class", "tooltip")
		.style("opacity", 0);

	var margin = {top: 20, right: 120, bottom: 20, left: 120},
		width = $("#d3").width() - margin.right - margin.left,
		height = $("#d3").height() - margin.top - margin.bottom;

    var i = 0;

	tree = d3.layout.tree()
		.size([height, width]);

	svg = d3.select("#d3").append("svg")
		.attr("width", width + margin.right + margin.left)
		.attr("height", height + margin.top + margin.bottom)
        .attr("class", "overlay")
        .call(zoomListener)
        .append("g")
		.attr("transform", "translate(" + margin.left + "," + margin.top + ")")

        		root.x0 = height / 2;
        		root.y0 = 0;
        		_initParents(root);
        		root.children.forEach(_collapse);
        		_update(root);

        	//attach search box listener
        	$("#search").on("keypress", function(e) {
                var key = e.which;
                var search = e.target.value.toLowerCase();
                 if(search == undefined || _trim(search) === "" || key != 13 )  // the enter key code
                  { return;}
                 
         		$("path[class='foundLink']").attr("class","link");
                allNodes.forEach(function(n) {n.class="";});
        		root.children.forEach(_collapse);
        		_update(root);
        		var paths_ = searchTree(root,search,[]);
        		if(typeof(paths_) !== "undefined"){
        			_openPaths(paths_);
        		}
                return false;
        	})

        	d3.select(self.frameElement).style("height", "800px");
        
    }

    function _trim(string) {
        var str = $.trim(string);
        if (str) {
            return str.replace(/([\n|\r])/g,'');
    } 
    return "";
    }

    function _initParents(c) {
        allNodes.push(c);
        if (c.children) {
            c.children.forEach(function(d){
                d.parent = c;
                _initParents(d);
            });
        };
    }
	//recursively collapse children
	function _collapse(d) {
		if (d.children) {
			d._children = d.children;
			d._children.forEach(_collapse);
			d.children = null;
		}

	}

	// Toggle children on click.
	function _click(d) {
		if (d.children) {
			d._children = d.children;
			d.children = null;
	  	}
	  	else{
			d.children = d._children;
			d._children = null;
	  	}
		_update(d);
	}

	function _openPaths(paths){
		for(var i =0;i<paths.length;i++){
            var n = paths[i];
			if(n.parent){//i.e. not root
				n.class = 'found';
                if (n.parent) {
                    _openParent(n);
                }
				_update(n);
			}
		}
	}

    function _openParent(child) {
        var np = child.parent;
		if(np._children){ //if children are hidden: open them, otherwise: don't do anything
			np.children = np._children;
			np.class = 'found';
			np._children = null;
            _openParent(np);
		}
        
    }
    function _nodeFillClass(d) {
        if (d.class=== 'found') {
            return "found";
        } else if (d._children && d._children.length > 0){
            return "children"
        }
        return "";
    }

	function _update(source) {
		// Compute the new tree layout.
		var nodes = tree.nodes(root).reverse(),
		links = tree.links(nodes);

		// Normalize for fixed-depth.
		nodes.forEach(function(d) { d.y = d.depth * 180; });

		// Update the nodesâ€¦
		var node = svg.selectAll("g.node")
			.data(nodes, function(d) { return d.id || (d.id = ++i); });

		// Enter any new nodes at the parent's previous position.
		var nodeEnter = node.enter().append("g")
			.attr("class", "node")
		.attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
		.on("click", _click);

		nodeEnter.append("circle")
		.attr("r", 1e-6)
		.attr("class",_nodeFillClass);

		nodeEnter.append("text")
			.attr("x", function(d) { return d.children || d._children ? -10 : 10; })
			.attr("dy", ".35em")
			.attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
			.text(function(d) { return d.displayName; })
            .attr("class", function(d) {
                if (d.class === 'found') {return "found";}
                 return "";})
			.style("fill-opacity", 1e-6).
            on("click",function(d){
                if (d.iri == undefined) {
                    return false;
                };
				var loc = window.location.href;
				if (loc.substring(loc.length-1) == "/") {
					loc = loc.substring(0,loc.length -2);
				}
				loc = loc.substring(0,loc.lastIndexOf("/"));	
				window.location = loc + "/node/" + d.iri;
            });

		// Transition nodes to their new position.
		var nodeUpdate = node.transition()
			.duration(duration)
			.attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

		nodeUpdate.select("circle")
			.attr("r", 4.5).
            attr("class",_nodeFillClass);
            
		nodeUpdate.select("text")
            .attr("class", function(d) {
                if (d.class === 'found') {return "found";}
                 return "";})
			.style("fill-opacity", 1);

		// Transition exiting nodes to the parent's new position.
		var nodeExit = node.exit().transition()
			.duration(duration)
			.attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
			.remove();

		nodeExit.select("circle")
			.attr("r", 1e-6);

		nodeExit.select("text")
			.style("fill-opacity", 1e-6);

		// Update the linksâ€¦
		var link = svg.selectAll("path.link")
			.data(links, function(d) { return d.target.id; });

		// Enter any new links at the parent's previous position.
		link.enter().insert("path", "g")
			.attr("d", function(d) {
				var o = {x: source.x0, y: source.y0};
				return diagonal({source: o, target: o});
			})
			.attr("class",function(d){
				if(d.target.class==="found"){
					return "foundLink";
				}
				return "link";
			});
            

		// Transition links to their new position.
		link.transition()
			.duration(duration)
			.attr("d", diagonal)
			.attr("class",function(d){
				if(d.target.class==="found"){
					return "foundLink";
				}
				return "link";
			});

		// Transition exiting nodes to the parent's new position.
		link.exit().transition()
			.duration(duration)
			.attr("d", function(d) {
				var o = {x: source.x, y: source.y};
				return diagonal({source: o, target: o});
			}).attr("class",function(d){
				if(d.target.class==="found"){
					return "foundLink";
				}
				return "link";
			})
			.remove();

		// Stash the old positions for transition.
		nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		  });
	}


    function _pan(domNode, direction) {
        var speed = panSpeed;
        if (panTimer) {
            clearTimeout(panTimer);
            translateCoords = d3.transform(svg.attr("transform"));
            if (direction == 'left' || direction == 'right') {
                translateX = direction == 'left' ? translateCoords.translate[0] + speed : translateCoords.translate[0] - speed;
                translateY = translateCoords.translate[1];
            } else if (direction == 'up' || direction == 'down') {
                translateX = translateCoords.translate[0];
                translateY = direction == 'up' ? translateCoords.translate[1] + speed : translateCoords.translate[1] - speed;
            }
            scaleX = translateCoords.scale[0];
            scaleY = translateCoords.scale[1];
            scale = zoomListener.scale();
            svg.transition().attr("transform", "translate(" + translateX + "," + translateY + ")scale(" + scale + ")");
            d3.select(domNode).select('g.node').attr("transform", "translate(" + translateX + "," + translateY + ")");
            zoomListener.scale(zoomListener.scale());
            zoomListener.translate([translateX, translateY]);
            panTimer = setTimeout(function() {
                _pan(domNode, speed, direction);
            }, 50);
        }
    }
 // Define the zoom function for the zoomable tree

//http://bl.ocks.org/phil-pedruco/7051552
    function _zoom() {
        svg.attr("transform", "translate(" + d3.event.translate + ")");
    }


    // define the zoomListener which calls the zoom function on the "zoom" event constrained within the scaleExtents
    var zoomListener = d3.behavior.zoom().scaleExtent([1.3, 1.3]).on("zoom", _zoom);

	return {
        init : _init
    }
})(console, jQuery, window);
