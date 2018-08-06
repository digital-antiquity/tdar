const TDAR = require("JS/tdar.master.js");
require('script-loader!blueimp-tmpl/js/tmpl.js');
require('script-loader!./../../includes/bootstrap-datepicker-eyecon/js/bootstrap-datepicker.js');

//Used this fix from https://stackoverflow.com/questions/44187714/import-blueimp-jquery-file-upload-in-webpack/48236429
/**These weren't originally in the WRO file**/
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-image.js');
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-audio.js');
//require('script-loader!blueimp-file-upload/js/jquery.fileupload-video.js');
//require('script-loader!blueimp-load-image/js/load-image.all.min.js');
//require('script-loader!blueimp-canvas-to-blob/js/canvas-to-blob.js');


require('script-loader!blueimp-file-upload/js/vendor/jquery.ui.widget.js');
require('script-loader!blueimp-file-upload/js/jquery.iframe-transport.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-process.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-validate.js');
require('script-loader!blueimp-file-upload/js/jquery.fileupload-ui.js');

TDAR.main();

