/**
 * Note tdar.upload.js is no longer being used. 
 */

const TDAR          = require("./tdar.core");
TDAR.common         = require("./tdar.common");
TDAR.auth           = require("./tdar.auth");
TDAR.autocomplete   = require("./tdar.autocomplete");
TDAR.advancedSearch = require("./tdar.advanced-search");
TDAR.validate       = require("./tdar.validate");
TDAR.authority      = require("./tdar.authority-management");
TDAR.bookmark       = require("./tdar.bookmark");
TDAR.bulk           = require("./tdar.bulk");
TDAR.c3graph        = require("./tdar.c3graph");
TDAR.c3graphsupport = require("./tdar.c3graphsupport");
TDAR.contexthelp    = require("./tdar.contexthelp");
TDAR.d3tree         = require("./tdar.d3tree");
TDAR.datasetMetadata = require("./tdar.dataset-metadata"); 
TDAR.datatable       = require("./tdar.datatable");
TDAR.datepicker      = require("./tdar.datepicker");
TDAR.menu            = require("./tdar.menu");
TDAR.moreinfo        = require("./tdar.moreinfo");
TDAR.notifications   = require("./tdar.notifications");
TDAR.ontologyMapping = require("./tdar.ontology-mapping");
TDAR.pricing         = require("./tdar.pricing");
TDAR.repeatrow       = require("./tdar.repeatrow");
TDAR.sensoryData     = require("./tdar.sensorydata");
TDAR.inheritance     = require("./tdar.inheritance");
TDAR.leaflet         = require("./maps/tdar.leaflet");
TDAR.worldmap        = require("./maps/tdar.worldmap");

TDAR.vuejs = {};
TDAR.vuejs.collectionwidget =   require("./vue/view-collection-widget.js");
TDAR.vuejs.uploadWidget     =   require("./vue/vue-edit-file-upload-component");
TDAR.vuejs.upload           =   require("./vue/vue-base-upload");
TDAR.vuejs.balk             =   require("./vue/vue-balk-upload-component");
TDAR.vuejs.advancedSearch   =   require("./vue/vue-autocomplete");
TDAR.vuejs.editcollectionapp =  require("./vue/edit-collection");
require("./tdar.messages_en");

window.TDAR = TDAR;
if (TDAR['vuejs'] == undefined) {
	TDAR['vuejs'] = {};
}


require("../css/tdar-svg.css");
require("../css/tdar-bootstrap.css");
require("../css/tdar.dashboard.css");
require("../css/famfamfam.css");
require("../css/tdar-svg.css");
require("../css/tdar.homepage.css");
require("../css/tdar.d3tree.css");
require("../css/tdar.c3graph.css");
require("../css/tdar.leaflet.css");
require("../css/tdar.worldmap.css");
require("../css/tdar.datatablemapping.css");
require("../css/tdar.sprites.css");
require("../css/tdar.datatables.css");
require("../css/tdar.searchresults.css");
require("../css/tdar.invoice.css");
require("../css/tdar-integration.css");
require("../includes/jquery-treeview/jquery.treeview.css");
require("../includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css");
require("../includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css");
require("../components/bootstrap-datepicker-eyecon/css/datepicker.css");
require("leaflet/dist/leaflet.css");
require("leaflet-draw/dist/leaflet.draw.css");
require("leaflet.markercluster/dist/MarkerCluster.css");
require("leaflet.markercluster/dist/MarkerCluster.Default.css");
require("c3/c3.min.css");
require("leaflet-control-geocoder/dist/Control.Geocoder.css");
module.exports = TDAR;