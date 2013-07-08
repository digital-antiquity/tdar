
if(typeof TDAR === "undefined") TDAR = {};
TDAR.ontology = (function(){
    "use strict";


    function _makedraggable(){
        var onMouseDown, onMouseUp, onStartDrag;
        var el, dragging

        //el = $( '.orgChart.interactive' );
        el = $( '#ontologyViewerPan' );
        //swallow click events when dragging

        el.draggable( {
            start: function() {$(this).data("dragging", true)},
            stop: function() {$(this).data("dragging", false) }
        } );

    }



    var ontology =  {
        view: function() {
            ontology.registerOrgChart($("#ontology-nodes-root"));

            //register the 'show hidden nodes' feature
            $('#btnOntologyShowMore').click(function() {
                $('#divOntologyShowMore').hide();
                $('#ontology-nodes .hidden-nodes').removeClass("hidden-nodes");
                return false;
            });

        },

        registerOrgChart: function($elem) {

            //FIXME: I don't think we should make this "draggable" as most browsers will implicitly make the div scrollable whenever it needs to be
            _makedraggable();
            $elem.orgChart({container: $("#ontologyViewerPan"), interactive:true, showLevels:2, stack:true});
        }

    };
    return ontology;
})();