const path = require('path');

import '/components/bootstrap-datepicker-eyecon/css/datepicker.css';
import '/components/c3/c3.min.css';
import '/components/jquery-file-upload/css/jquery.fileupload-ui.css';
import '/components/jquery.treeview/jquery.treeview.css';
import '/components/leaflet-cluster/dist/MarkerCluster.css';
import '/components/leaflet-cluster/dist/MarkerCluster.Default.css';
import '/components/leaflet-control-geocoder/dist/Control.Geocoder.css';
import '/components/leaflet-draw/dist/leaflet.draw.css';
import '/components/leaflet/dist/leaflet.css';
import '/components/selectize/dist/css/selectize.css';
import '/css/famfamfam.css';
import '/css/tdar-bootstrap.css';
import '/css/tdar-svg.css';
import '/css/tdar.c3graph.css';
import '/css/tdar.d3tree.css';
import '/css/tdar.dashboard.css';
import '/css/tdar.datatablemapping.css';
import '/css/tdar.datatables.css';
import '/css/tdar.homepage.css';
import '/css/tdar.invoice.css';
import '/css/tdar.leaflet.css';
import '/css/tdar.searchresults.css';
import '/css/tdar.sprites.css';
import '/css/tdar.worldmap.css';
import '/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css';
import '/includes/jquery.datatables.plugins-1.9.4/integration/bootstrap/2/dataTables.bootstrap.css';


module.exports = {
  entry: './src/main/webapp/js/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, './src/main/webapp/components')
  },
  module: {
      rules: [
        {
          test: /\.css$/,
          use: ['style-loader', 'css-loader']
        }
      ]
    }
};