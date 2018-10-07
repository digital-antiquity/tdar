window.$ = window.jQuery = require('jquery'); // or import $ from 'jquery';
const TDAR          = require("./tdar.core");
TDAR.common         = require("./tdar.common");
TDAR.c3graph        = require("./tdar.c3graph");
TDAR.c3graphsupport = require("./tdar.c3graphsupport");
TDAR.leaflet         = require("./maps/tdar.leaflet");
TDAR.worldmap        = require("./maps/tdar.worldmap");
require("select2/dist/css/select2.min.css");
require("select2/dist/js/select2.full.min.js")

window.TDAR = TDAR;
if (TDAR['vuejs'] == undefined) {
    TDAR['vuejs'] = {};
}



module.exports = TDAR;

require("../../css/sass/style.scss")

TDAR.main();