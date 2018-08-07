window.$ = window.jQuery = require('jquery'); // or import $ from 'jquery';
require('./../../includes/jquery-ui-1.11.4.custom/jquery-ui');

const TDAR = require("JS/tdar.master.js");
require('script-loader!blueimp-tmpl/js/tmpl.js');
require('script-loader!./../../includes/bootstrap-datepicker-eyecon/js/bootstrap-datepicker.js');

TDAR.main();

