TDAR.d3tree = (function(console, $, ctx, d3) {
    "use strict";

    // var d3 = window.d3 ? window.d3 : typeof require !== 'undefined' ? require("d3") : undefined;
    
    // basically a way to get the path to an object

    function searchTree(obj, search, path) {
        var found = [];
        allNodes.forEach(function(n) {
            if (n.displayName.toLowerCase().indexOf(search) > -1) { // if search is found return, add the object to the path and return it
                found.push(n);
            }
        });
        return found;
    }




    /** "package private" variables - used by functions in this module but not publically accessible **/
    // FIXME: Use of variables in package scope is good for default and "const" values, but avoid choices that limit extensibility.  See example comments below.
    var div, root, tree, svg, diagonal;

    // FIXME: Example:  "duration" is fine since it essentially serves same purpose as a constant.
    var duration = 750;

    // FIXME: Example: "allNodes" is problematic because it implicitly assumes only one D3 graph on entire page.
    var allNodes = [];

    // FIXME: instead of referencing DOM elements by ID (e.g. "#d3", "#ontId"), consider passing them in as arguments to the init() function.
    function _init() {
        diagonal = d3.svg.diagonal().projection(function(d) {
            return [ d.y, d.x ];
        });

        $("#searchclear").click(function() {
            $("#search").val("");
            _clearSearch();
            $("#searchclear").hide();
        });
        $("#searchclear").hide();

        root = JSON.parse($("#ontId").text());

        if (!root || !root.children) {
            return;
        }
        div = d3.select("#d3").append("div") // declare the tooltip div
        .attr("class", "tooltip").style("opacity", 0);

        var margin = {
            top : 20,
            right : 120,
            bottom : 20,
            left : 120
        }, width = $("#d3").width() - margin.right - margin.left, height = $("#d3").height() - margin.top - margin.bottom;

        var i = 0;

        tree = d3.layout.tree().size([ height, width ]);

        svg = d3.select("#d3").append("svg").attr("width", width + margin.right + margin.left).attr("height", height + margin.top + margin.bottom).attr(
                "class", "overlay").call(zoomListener).append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")")

        root.x0 = height / 2;
        root.y0 = 0;
        _initParents(root);
        root.children.forEach(_collapse);
        _update(root);

        // attach search box listener
        $("#search").on("keyup", function(e) {
            var key = e.which;

            var search = _trim(e.target.value.toLowerCase());
            if (search != undefined && search != '') {
                $("#searchclear").show();
            } else {
                $("#searchclear").hide();
                return false;
            }
            console.log(search);
            _clearSearch();
            if (search == '') {
                return false;
            }
            if (search.length > 2) {
                _doSearch(search);
            }

            return true;
        })
    }

    function _doSearch(search) {
        _clearSearch();
        root.children.forEach(_collapse);
        _update(root);
        var paths_ = searchTree(root, search, []);
        if (typeof (paths_) !== "undefined") {
            _openPaths(paths_);
        }
    }
    function _trim(string) {
        var str = $.trim(string);
        if (str) {
            return str.replace(/([\n|\r])/g, '');
        }
        return "";
    }

    function _initParents(c) {
        allNodes.push(c);
        if (c.children) {
            c.children.forEach(function(d) {
                d.parent = c;
                _initParents(d);
            });
        }
        ;
    }

    // recursively collapse children
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
        } else {
            d.children = d._children;
            d._children = null;
        }
        _update(d);
    }

    function _openPaths(paths) {
        for (var i = 0; i < paths.length; i++) {
            var n = paths[i];
            if (n.parent) {// i.e. not root
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
        if (np._children) { // if children are hidden: open them, otherwise: don't do anything
            np.children = np._children;
            np.class = 'found';
            np._children = null;
            _openParent(np);
        }

    }

    function _nodeFillClass(d) {
        if (d.class === 'found') {
            return "found";
        } else if (d._children && d._children.length > 0) {
            return "children"
        }
        return "";
    }

    function _update(source) {
        // Compute the new tree layout.
        var nodes = tree.nodes(root).reverse(), links = tree.links(nodes);

        // Normalize for fixed-depth.
        nodes.forEach(function(d) {
            d.y = d.depth * 180;
        });

        // Update the nodesâ€¦
        var node = svg.selectAll("g.node").data(nodes, function(d) {
            return d.id || (d.id = ++i);
        });

        // Enter any new nodes at the parent's previous position.
        var nodeEnter = node.enter().append("g").attr("class", "node").attr("transform", function(d) {
            return "translate(" + source.y0 + "," + source.x0 + ")";
        }).on("click", _click);

        nodeEnter.append("circle").attr("r", 1e-6).attr("class", _nodeFillClass);

        nodeEnter.append("text").attr("x", function(d) {
            return d.children || d._children ? -10 : 10;
        }).attr("dy", ".35em").attr("text-anchor", function(d) {
            return d.children || d._children ? "end" : "start";
        }).text(function(d) {
            return d.displayName;
        }).attr("class", function(d) {
            if (d.class === 'found') {
                return "found";
            }
            return "";
        }).style("fill-opacity", 1e-6).on("click", function(d) {
            if (d.iri == undefined) {
                return false;
            }
            ;
            var loc = window.location.href;
            if (loc.substring(loc.length - 1) == "/") {
                loc = loc.substring(0, loc.length - 2);
            }
            loc = loc.substring(0, loc.lastIndexOf("/"));
            window.location = loc + "/node/" + d.slug;
        });

        // Transition nodes to their new position.
        var nodeUpdate = node.transition().duration(duration).attr("transform", function(d) {
            return "translate(" + d.y + "," + d.x + ")";
        });

        nodeUpdate.select("circle").attr("r", 4.5).attr("class", _nodeFillClass);

        nodeUpdate.select("text").attr("class", function(d) {
            if (d.class === 'found') {
                return "found";
            }
            return "";
        }).style("fill-opacity", 1);

        // Transition exiting nodes to the parent's new position.
        var nodeExit = node.exit().transition().duration(duration).attr("transform", function(d) {
            return "translate(" + source.y + "," + source.x + ")";
        }).remove();

        nodeExit.select("circle").attr("r", 1e-6);

        nodeExit.select("text").style("fill-opacity", 1e-6);

        // Update the linksâ€¦
        var link = svg.selectAll("path.link").data(links, function(d) {
            return d.target.id;
        });

        // Enter any new links at the parent's previous position.
        link.enter().insert("path", "g").attr("d", function(d) {
            var o = {
                x : source.x0,
                y : source.y0
            };
            return diagonal({
                source : o,
                target : o
            });
        }).attr("class", function(d) {
            if (d.target.class === "found") {
                return "foundLink";
            }
            return "link";
        });

        // Transition links to their new position.
        link.transition().duration(duration).attr("d", diagonal).attr("class", function(d) {
            if (d.target.class === "found") {
                return "foundLink";
            }
            return "link";
        });

        // Transition exiting nodes to the parent's new position.
        link.exit().transition().duration(duration).attr("d", function(d) {
            var o = {
                x : source.x,
                y : source.y
            };
            return diagonal({
                source : o,
                target : o
            });
        }).attr("class", function(d) {
            if (d.target.class === "found") {
                return "foundLink";
            }
            return "link";
        }).remove();

        // Stash the old positions for transition.
        nodes.forEach(function(d) {
            d.x0 = d.x;
            d.y0 = d.y;
        });
    }

    function _clearSearch() {
        // unhighlight nodes and paths by removing the 'found' and 'foundLink' class
        $("path[class='foundLink']").attr("class", "link");
        d3.selectAll("circle[class='found']").classed("found", false);

        //untag nodes previously marked as "found"
        allNodes.forEach(function(n) {
            n.class = "";
        });
    }

    // Define the zoom function for the zoomable tree
    // http://bl.ocks.org/phil-pedruco/7051552
    function _zoom() {
        svg.attr("transform", "translate(" + d3.event.translate + ")");
    }

    // define the zoomListener which calls the zoom function on the "zoom" event constrained within the scaleExtents
    var zoomListener = d3.behavior.zoom().scaleExtent([ 1.3, 1.3 ]).on("zoom", _zoom);

    return {
        "init" : _init,
        "clearSearch": _clearSearch,
        "allNodes": allNodes
    };
})(console, jQuery, window, d3);
