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
TDAR.download       = require("./tdar.download");
TDAR.d3tree         = require("./tdar.d3tree");
TDAR.jstree         = require("./tdar.jstree");
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
//TDAR.vuejs.advancedSearch   =   require("./vue/vue-autocomplete");
TDAR.vuejs.editcollectionapp =  require("./vue/edit-collection");
require("./tdar.messages_en");

window.TDAR = TDAR;
if (TDAR['vuejs'] == undefined) {
	TDAR['vuejs'] = {};
}



module.exports = TDAR;