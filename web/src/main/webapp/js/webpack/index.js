
//These are all of the components that we have written.
//They will be bundled together into one file. 
import './../tdar.core';
// window.TDAR = TDAR;
import $ from 'jquery/dist/jquery';
//import 'vue';
//import 'axios';
window.jQuery = $;
window.$ = $;

//used by jquery.treeview 
import './../../includes/jquery.cookie.js';

import 'svgxuse';

import './../../includes/jquery.textarearesizer.js';

/* LICENSE: MIT */
import './../jquery.FormNavigate.js';
import './../../includes/jquery.watermark-3.1.3.min.js';

/* LICENSE: MIT */
import './../../includes/jquery.datatables-1.9.4/media/js/jquery.dataTables.js';

/* LICENSE: BSD /GPL 2.0 */
import './../../includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.js';

/* LICENSE: BSD /GPL 2.0 */
import './../../includes/jquery.treeview/jquery.treeview.js';

//import './../../includes/blueimp-tmpl/js/tmpl.min.js';
require('script-loader!blueimp-tmpl/js/tmpl.js');

require('script-loader!./../../includes/bootstrap-datepicker-eyecon/js/bootstrap-datepicker.js');



//Used this fix from https://stackoverflow.com/questions/44187714/import-blueimp-jquery-file-upload-in-webpack/48236429
/**These weren't originally in the WRO file**/
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-image.js');
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-audio.js');
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-video.js');
//require('script-loader!blueimp-load-image/js/load-image.all.min.js');
//require('script-loader!blueimp-canvas-to-blob/js/canvas-to-blob.js');

import './../../includes/jquery-ui-1.11.4.custom/jquery-ui.min.js';
require('script-loader!blueimp-file-upload/js/vendor/jquery.ui.widget.js');
require('script-loader!blueimp-file-upload/js/jquery.iframe-transport.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-process.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-validate.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-ui.js');

/* LICENSE: MIT */
import './../../includes/jquery.populate.js';

/* LICENSE: MIT */
import './../../includes/jquery.tabby-0.12.js';

/* LICENSE:CC Attribution 3.0 Unported */
import './../latLongUtil-1.0.js';

import 'leaflet/dist/leaflet';

import 'leaflet-choropleth/dist/choropleth.js';

import './../../includes/es6-promise/lib/es6-promise.js';
import './../../includes/es6-promise/lib/es6-promise.auto.js';

import 'axios/dist/axios.min.js';
import 'qs/dist/qs.js';

import './../../includes/Leaflet.Sleep.js';

import 'leaflet-draw/dist/leaflet.draw.js';
import './../../includes/leaflet-cluster/dist/leaflet.markercluster.js';

import 'd3/d3';
import c3 from 'c3/c3';
window.c3 = c3;

import 'leaflet-control-geocoder/dist/Control.Geocoder.js';

import "jquery-validation/dist/jquery.validate";
import "jquery-validation/dist/additional-methods";

import './../tdar.datepicker';
import './../tdar.moreinfo';
import './../tdar.upload';
import './../tdar.jquery-upload-validation';
import './../tdar.common';
import './../tdar.bulk';
import './../tdar.repeatrow';
import './../tdar.autocomplete';

import './../tdar.c3graphsupport';
import './../tdar.c3graph';
import './../tdar.validate';
import './../tdar.messages_en';
import './../tdar.d3tree';

import './../tdar.datatable';
import './../tdar.dataset-metadata';
import './../tdar.ontology-mapping';

import './../tdar.sensorydata';
import './../tdar.notifications';
import './../tdar.advanced-search';
import './../tdar.authority-management';
import './../tdar.auth';
import './../tdar.inheritance';
import './../tdar.download';
import './../tdar.pricing';
import './../tdar.contexthelp';
import './../tdar.menu';
import './../tdar.formValidateExtensions';
import './../../includes/bindWithDelay';
import './../tdar.bookmark';
import './../maps/tdar.leaflet';

import './../../includes/js-emca-5.1-polyfill.js';
import 'selectize/dist/js/standalone/selectize.js';

import './../maps/tdar.worldmap';

import "vue";

import './../vue/vue-selectize.js';
require('script-loader!../../includes/bootstrap-2.32/js/bootstrap.js');
import './../vue/view-collection-widget.js';
import './../vue/edit-collection';

TDAR.main();

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
