/*
 * Validation message customizations for tDAR
 * Locale: EN
 */

require("jquery-validation/dist/jquery.validate");
require("jquery-validation/dist/additional-methods");

$(document).ready(function(){
    jQuery.extend(jQuery.validator.messages, {
        extension: jQuery.validator.format("Please upload a file with one of the following extensions:{0}.")
    });
});
