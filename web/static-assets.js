//This file replaces what was in WRO. 
//Its used for Karma tests and in webpack.


    /***********************************************************************************************************************************************************
     * './src/main/webapp/css/tdar-bootstrap.css'; './src/main/webapp/css/tdar.dashboard.css';
     * 'jquery.treeview/jquery.treeview.css'; './src/main/webapp/css/famfamfam.css';
     * './src/main/webapp/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css';
     * './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css'; './src/main/webapp/css/tdar-svg.css';
     * './src/main/webapp/css/tdar.c3graph.css'; './src/main/webapp/css/tdar.d3tree.css'; './src/main/webapp/css/tdar.datatablemapping.css';
     * './src/main/webapp/css/tdar.datatables.css'; './src/main/webapp/css/tdar.homepage.css'; './src/main/webapp/css/tdar.invoice.css';
     * './src/main/webapp/css/tdar.leaflet.css'; './src/main/webapp/css/tdar.searchresults.css'; './src/main/webapp/css/tdar.sprites.css';
     * './src/main/webapp/css/tdar.worldmap.css';
     **********************************************************************************************************************************************************/
          
    import './src/main/webapp/css/tdar-bootstrap.css';
    import './src/main/webapp/css/tdar.dashboard.css';
    import './src/main/webapp/css/famfamfam.css';
    import 'jquery.treeview/jquery.treeview.css';
    import './src/main/webapp/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css';
    import './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css';
     /* used by jquery.treeview */
    import './src/main/webapp/includes/jquery.cookie.js';
    import './src/main/webapp/css/tdar-svg.css';
    import 'svgxuse/svgxuse.min.js';
     
     /* LICENSE: MIT /GPL 2.0 */
     /* not sure who uses this */
     /* './src/main/webapp/includes/jquery.metadata.2.1/jquery.metadata.js'; */
     /* LICENSE: MIT */
    import './src/main/webapp/includes/jquery.textarearesizer.js';
     
     /* LICENSE: MIT */
    import './src/main/webapp/js/jquery.FormNavigate.js';
    import './src/main/webapp/includes/jquery.watermark-3.1.3.min.js';
     
     /* LICENSE: MIT */
    import './src/main/webapp/includes/jquery.datatables-1.9.4/media/js/jquery.dataTables.js';
     
     /* LICENSE: BSD /GPL 2.0 */
    import './src/main/webapp/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.js';
     
     /* LICENSE: BSD /GPL 2.0 */
    import 'jquery.treeview/jquery.treeview.js';
     
     /* LICENSE: MIT /GPL 2.0 */
     import 'blueimp-tmpl/js/tmpl.js';
     import 'bootstrap-datepicker-eyecon/css/datepicker.css';
     import 'bootstrap-datepicker-eyecon/js/bootstrap-datepicker.js';
     
    
     //Used this fix from https://stackoverflow.com/questions/44187714/import-blueimp-jquery-file-upload-in-webpack/48236429
     require('script-loader!blueimp-file-upload/js/vendor/jquery.ui.widget.js');
     require('script-loader!blueimp-tmpl/js/tmpl.js');
     require('script-loader!blueimp-load-image/js/load-image.all.min.js');
     require('script-loader!blueimp-canvas-to-blob/js/canvas-to-blob.js');
     require('script-loader!blueimp-file-upload/js/jquery.iframe-transport.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-process.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-image.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-audio.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-video.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-validate.js');
     require('script-loader!blueimp-file-upload/js/jquery.fileupload-ui.js');
     
     /* LICENSE: MIT */
     import './src/main/webapp/includes/jquery.populate.js';
     
     /* LICENSE: MIT */
     import './src/main/webapp/includes/jquery.tabby-0.12.js';
     
     /* LICENSE:CC Attribution 3.0 Unported */
     import './src/main/webapp/js/latLongUtil-1.0.js';
     import 'leaflet/dist/leaflet.js';
     import 'leaflet-choropleth/dist/choropleth.js';
     
     import 'es6-promise/es6-promise.js';
     import 'es6-promise/es6-promise.auto.js';
     
     /* 'axios/dist/axios.js'; */
     import 'axios/dist/axios.min.js';
     import 'qs/dist/qs.js';
     
     /* https://github.com/CliffCloud/Leaflet.Sleep */
     import './src/main/webapp/includes/Leaflet.Sleep.js';
     import 'leaflet/dist/leaflet.css';
     import 'leaflet-draw/dist/leaflet.draw.js';
     import 'leaflet-draw/dist/leaflet.draw.css';
     import 'leaflet-cluster/dist/leaflet.markercluster.js';
     import 'leaflet-cluster/dist/MarkerCluster.css';
     import 'leaflet-cluster/dist/MarkerCluster.Default.css';
     import 'd3/d3.min.js';
     import 'c3/c3.min.js';
     import 'c3/c3.min.css';
     import 'leaflet-control-geocoder/dist/Control.Geocoder.js';
     import 'leaflet-control-geocoder/dist/Control.Geocoder.css';
     import './src/main/webapp/css/tdar.homepage.css';
     import './src/main/webapp/css/tdar.d3tree.css';
     import './src/main/webapp/css/tdar.c3graph.css';
     import './src/main/webapp/css/tdar.leaflet.css';
     import './src/main/webapp/css/tdar.worldmap.css';
     import './src/main/webapp/css/tdar.datatablemapping.css';
     import './src/main/webapp/css/tdar.sprites.css';
     import './src/main/webapp/css/tdar.datatables.css';
     import './src/main/webapp/css/tdar.searchresults.css';
     import './src/main/webapp/css/tdar.invoice.css';
     
     /* LICENSE: MIT */
     import './src/main/webapp/includes/js-emca-5.1-polyfill.js';

     /* Selectize */        
     import 'selectize/dist/js/standalone/selectize.js';
     import 'selectize/dist/css/selectize.css';

     import 'vue/dist/vue.min.js'
     // 'bundle.js'
