//This file replaces what was in WRO. 
//Its used for Karma tests and in webpack.

const files = [
    /**
     './src/main/webapp/css/tdar-bootstrap.css',
     './src/main/webapp/css/tdar.dashboard.css',
     './src/main/webapp/components/jquery.treeview/jquery.treeview.css',
     './src/main/webapp/css/famfamfam.css',
     './src/main/webapp/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css',
     './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css',
     './src/main/webapp/css/tdar-svg.css',
     './src/main/webapp/css/tdar.c3graph.css',
     './src/main/webapp/css/tdar.d3tree.css',
     './src/main/webapp/css/tdar.datatablemapping.css',
     './src/main/webapp/css/tdar.datatables.css',
     './src/main/webapp/css/tdar.homepage.css',
     './src/main/webapp/css/tdar.invoice.css',
     './src/main/webapp/css/tdar.leaflet.css',
     './src/main/webapp/css/tdar.searchresults.css',
     './src/main/webapp/css/tdar.sprites.css',
     './src/main/webapp/css/tdar.worldmap.css',**/
          
     './src/main/webapp/css/tdar-bootstrap.css',
     './src/main/webapp/css/tdar.dashboard.css',
     './src/main/webapp/css/famfamfam.css',
     './src/main/webapp/components/jquery.treeview/jquery.treeview.css',
     './src/main/webapp/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css',
     './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css',
     /*  used by jquery.treeview */
     './src/main/webapp/includes/jquery.cookie.js',
     './src/main/webapp/css/tdar-svg.css',
     './src/main/webapp/components/svgxuse/svgxuse.min.js',
     
     /* LICENSE: MIT /GPL 2.0 */
     /*  not sure who uses this */
     /*'./src/main/webapp/includes/jquery.metadata.2.1/jquery.metadata.js', */
     /* LICENSE: MIT */
     './src/main/webapp/includes/jquery.textarearesizer.js',
     
     /* LICENSE: MIT */
     './src/main/webapp/js/jquery.FormNavigate.js',
     './src/main/webapp/includes/jquery.watermark-3.1.3.min.js',
     
     /* LICENSE: MIT */
     './src/main/webapp/includes/jquery.datatables-1.9.4/media/js/jquery.dataTables.js',
     
     /* LICENSE: BSD /GPL 2.0 */
     './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.js',
     
     /* LICENSE: BSD /GPL 2.0 */
     './src/main/webapp/components/jquery.treeview/jquery.treeview.js',
     
     /* LICENSE: MIT /GPL 2.0 */
     './src/main/webapp/components/jquery-file-upload/css/jquery.fileupload-ui.css',
     './src/main/webapp/components/blueimp-tmpl/js/tmpl.js',
     './src/main/webapp/components/bootstrap-datepicker-eyecon/css/datepicker.css',
     './src/main/webapp/components/bootstrap-datepicker-eyecon/js/bootstrap-datepicker.js',
     
     './src/main/webapp/components/jquery-file-upload/js/vendor/jquery.ui.widget.js',
     './src/main/webapp/components/jquery-file-upload/js/jquery.iframe-transport.js',
     './src/main/webapp/components/jquery-file-upload/js/jquery.fileupload.js',
     './src/main/webapp/components/jquery-file-upload/js/jquery.fileupload-process.js',
     './src/main/webapp/components/jquery-file-upload/js/jquery.fileupload-validate.js',
     './src/main/webapp/components/jquery-file-upload/js/jquery.fileupload-ui.js',
     
     /* LICENSE: MIT */
     './src/main/webapp/includes/jquery.populate.js',
     
     /* LICENSE: MIT */
     './src/main/webapp/includes/jquery.tabby-0.12.js',
     
     /* LICENSE:CC Attribution 3.0 Unported */
     './src/main/webapp/js/latLongUtil-1.0.js',
     './src/main/webapp/components/leaflet/dist/leaflet.js',
     './src/main/webapp/components/leaflet-choropleth/dist/choropleth.js',
     
     './src/main/webapp/components/es6-promise/es6-promise.js',
     './src/main/webapp/components/es6-promise/es6-promise.auto.js',
     
     /* './src/main/webapp/components/axios/dist/axios.js', */
     './src/main/webapp/components/axios/dist/axios.min.js',
     './src/main/webapp/components/qs/dist/qs.js',
     
     /*  https://github.com/CliffCloud/Leaflet.Sleep */
     './src/main/webapp/includes/Leaflet.Sleep.js',
     './src/main/webapp/components/leaflet/dist/leaflet.css',
     './src/main/webapp/components/leaflet-draw/dist/leaflet.draw.js',
     './src/main/webapp/components/leaflet-draw/dist/leaflet.draw.css',
     './src/main/webapp/components/leaflet-cluster/dist/leaflet.markercluster.js',
     './src/main/webapp/components/leaflet-cluster/dist/MarkerCluster.css',
     './src/main/webapp/components/leaflet-cluster/dist/MarkerCluster.Default.css',
     './src/main/webapp/components/d3/d3.min.js',
     './src/main/webapp/components/c3/c3.min.js',
     './src/main/webapp/components/c3/c3.min.css',
     './src/main/webapp/components/leaflet-control-geocoder/dist/Control.Geocoder.js',
     './src/main/webapp/components/leaflet-control-geocoder/dist/Control.Geocoder.css',
     
     './src/main/webapp/css/tdar.homepage.css',
     './src/main/webapp/css/tdar.d3tree.css',
     './src/main/webapp/css/tdar.c3graph.css',
     './src/main/webapp/css/tdar.leaflet.css',
     './src/main/webapp/css/tdar.worldmap.css',
     './src/main/webapp/css/tdar.datatablemapping.css',
     './src/main/webapp/css/tdar.sprites.css',
     './src/main/webapp/css/tdar.datatables.css',
     './src/main/webapp/css/tdar.searchresults.css',
     './src/main/webapp/css/tdar.invoice.css',
     
     /* LICENSE: MIT */
     './src/main/webapp/includes/js-emca-5.1-polyfill.js',

     /*Selectize  */        
     './src/main/webapp/components/selectize/dist/js/standalone/selectize.js',
     './src/main/webapp/components/selectize/dist/css/selectize.css',

     './src/main/webapp/components/vue/dist/vue.min.js'
     //'./src/main/webapp/components/bundle.js'     
];


module.exports = files;