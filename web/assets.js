//This file specifies which resources should be copied. It gets read from the 
//WebpackCopyModule 

const CSS = [
     './src/main/webapp/includes/jquery.datatables-1.9.4/media/css/jquery.dataTables.css',
     './src/main/webapp/css/famfamfam.css',
     './src/main/webapp/css/tdar-bootstrap.css',
     './src/main/webapp/css/tdar-svg.css',
     './src/main/webapp/css/tdar.c3graph.css',
     './src/main/webapp/css/tdar.d3tree.css',
     './src/main/webapp/css/tdar.dashboard.css',
     './src/main/webapp/css/tdar.datatablemapping.css',
     './src/main/webapp/css/tdar.datatables.css',
     './src/main/webapp/css/tdar.homepage.css',
     './src/main/webapp/css/tdar.invoice.css',
     './src/main/webapp/css/tdar.leaflet.css',
     './src/main/webapp/css/tdar.searchresults.css',
     './src/main/webapp/css/tdar.sprites.css',
     './src/main/webapp/css/tdar.worldmap.css'
];

//I created these mappings because the copier would only create the last directory. 
//This can be parsed out better later, but 🤷‍♂️

const MODULES  = [
{name:'bootstrap-datepicker-eyecon',path:'./node_modules/bootstrap-datepicker-eyecon/'},
{name:'c3',path:'./node_modules/c3'},
{name:'jquery/dist',path:'./node_modules/jquery/dist'},
{name:'jquery-file-upload',path:'./node_modules/jquery-file-upload/'},
{name:'jquery.treeview',path:'./node_modules/jquery.treeview/'},
{name:'leaflet-cluster/dist',path:'./node_modules/leaflet-cluster/dist'},
{name:'leaflet-control-geocoder/dist',path:'./node_modules/leaflet-control-geocoder/dist'},
{name:'leaflet-control-geocoder/dist',path:'./node_modules/leaflet-control-geocoder/dist'},
{name:'leaflet/dist',path:'./node_modules/leaflet/dist'},
{name:'selectize/dist',path:'./node_modules/selectize/dist'},
{name:'blueimp-tmpl',path:'./node_modules/blueimp-tmpl'},
{name:'bootstrap-datepicker-eyecon',path:'./node_modules/bootstrap-datepicker-eyecon'},
{name:'d3',path:'./node_modules/d3'},
{name:'es6-promise',path:'./node_modules/es6-promise'},
{name:'jquery-file-upload/js',path:'./node_modules/jquery-file-upload/js'},
{name:'jquery.treeview',path:'./node_modules/jquery.treeview'},
{name:'leaflet-choropleth/dist',path:'./node_modules/leaflet-choropleth/dist'},
{name:'leaflet-cluster/dist',path:'./node_modules/leaflet-cluster/dist'},
{name:'leaflet-control-geocoder/dist',path:'./node_modules/leaflet-control-geocoder/dist'},
{name:'leaflet-draw/dist',path:'./node_modules/leaflet-draw/dist'},
{name:'leaflet/dist',path:'./node_modules/leaflet/dist'},
{name:'qs/dist',path:'./node_modules/qs/dist'},
{name:'selectize/dist',path:'./node_modules/selectize/dist'},
{name:'svgxuse',path:'./node_modules/svgxuse'},
{name:'vue/dist',path:'./node_modules/vue/dist'},
{name:'axios/dist',path:'./node_modules/axios/dist'},
{name:'angular',path:'./node_modules/angular'}
{name:'angular-modal-service/dst',path:'./node_modules/angular-modal-service/dst'}
]


module.exports = [CSS, MODULES];