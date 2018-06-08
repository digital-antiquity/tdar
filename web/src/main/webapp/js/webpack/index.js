
//These are all of the components that we have written.
//They will be bundled together into one file. 
import './../tdar.core';
// window.TDAR = TDAR;
import $ from 'jquery/dist/jquery';
//import 'vue';
//import 'axios';
window.jQuery = $;
window.$ = $;
import "vue";
import "jquery-validation/dist/jquery.validate";
import "jquery-validation/dist/additional-methods";
import 'leaflet/dist/leaflet';
import 'leaflet-draw/dist/leaflet.draw';
import 'leaflet-choropleth/dist/choropleth.js';
import 'd3/d3';
import c3 from 'c3/c3';
window.c3 = c3;


//import "bootstrap-2.3.2/js/bootstrap-affix.js";
//import "bootstrap-2.3.2/js/bootstrap-alert.js";
//import "bootstrap-2.3.2/js/bootstrap-button.js";
//import "bootstrap-2.3.2/js/bootstrap-carousel.js";
//import "bootstrap-2.3.2/js/bootstrap-collapse.js";
//import "bootstrap-2.3.2/js/bootstrap-dropdown.js";
//import "bootstrap-2.3.2/js/bootstrap-modal.js";
//import "bootstrap-2.3.2/js/bootstrap-popover.js";
//import "bootstrap-2.3.2/js/bootstrap-scrollspy.js";
//import "bootstrap-2.3.2/js/bootstrap-tab.js";
//import "bootstrap-2.3.2/js/bootstrap-tooltip.js";
//import "bootstrap-2.3.2/js/bootstrap-transition.js";
//import "bootstrap-2.3.2/js/bootstrap-typeahead.js";
//import './src/main/webapp/css/tdar-svg.css';

//import 'svgxuse';


/**These are jQuery Plugins**/
import './../../includes/jquery-ui-1.11.4.custom/jquery-ui.js';
import './../../includes/jquery.cookie.js';
import './../../includes/jquery.textarearesizer.js';
/* LICENSE: MIT */
//import './../jquery.FormNavigate.js';
import './../../includes/jquery.watermark-3.1.3.min.js';
/* LICENSE: MIT */
import './../../includes/jquery.datatables-1.9.4/media/js/jquery.dataTables.js';
/* LICENSE: BSD /GPL 2.0 */
import './../../includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.js';
/* LICENSE: BSD /GPL 2.0 */

/**Hmm this is a bower module**/
//import 'jquery.treeview/jquery.treeview.js';

/**This breaks $.validator. **/
//import './../tdar.jquery-upload-validation';
import './../tdar.validate';

import './../tdar.common';

/**
import './../vue/view-collection-widget.js';
import './../vue/edit-collection';
*/

import './../tdar.datepicker';
import './../tdar.moreinfo';
import './../tdar.upload';
import './../tdar.bulk';
import './../tdar.repeatrow';
//import './../tdar.autocomplete';

import './../tdar.c3graphsupport';
import './../tdar.c3graph';
import './../tdar.d3tree';
//

import './../tdar.datatable';
//import './../tdar.dataset-metadata';
//import './../tdar.ontology-mapping';

import './../tdar.sensorydata';
import './../tdar.notifications';
import './../tdar.advanced-search';
//import './../tdar.authority-management';

import './../tdar.auth';
import './../tdar.inheritance';

import './../tdar.download';
import './../tdar.pricing';
import './../tdar.contexthelp';
import './../tdar.menu';
////

import './../tdar.formValidateExtensions';

import './../tdar.messages_en';
//import './../../includes/bindWithDelay';
//import './../tdar.bookmark';

import './../maps/tdar.leaflet';
import './../maps/tdar.worldmap';




//TDAR.main();