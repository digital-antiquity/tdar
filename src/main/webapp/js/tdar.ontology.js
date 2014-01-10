/**
 * ontology-specific functionality
 */
(function(TDAR, $){
    "use strict";

    /**
     * Enable 'panning' of the ontology display by making the orgchart inside of the container draggable.
     * @private
     */
    function _makedraggable(){
        var onMouseDown, onMouseUp, onStartDrag;
        var el, dragging

        el = $( '#ontologyViewerPan' );

        //swallow click events when dragging
        el.draggable( {
            start: function() {$(this).data("dragging", true)},
            stop: function() {$(this).data("dragging", false) }
        } );
    }

    /**
     * init the ontology view by displaying the ontology 'org chart' and registering necessary click events
     */
    function _view()  {
        _registerOrgChart($("#ontology-nodes-root"));

        //register the 'show hidden nodes' feature
        $('#btnOntologyShowMore').click(function() {
            $('#divOntologyShowMore').hide();
            $('#ontology-nodes .hidden-nodes').removeClass("hidden-nodes");
            return false;
        });
    }

    /**
     * Render the elements in the ontologyViewer container using the jQuery orgchart plugin
     * @param $elem
     */
    function _registerOrgChart($elem) {
        //Removing 'draggable' behavior for now since it doesn't work will with expandable nodes.
        //_makedraggable();
        $elem.orgChart({container: $("#ontologyViewerPan"), interactive:true, showLevels:2, stack:true});
    }

    TDAR.ontology = {
            view: _view,
            registerOrgChart: _registerOrgChart
    };

})(TDAR, jQuery);
