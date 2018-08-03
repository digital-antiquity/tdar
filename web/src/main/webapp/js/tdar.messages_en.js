/*
 * Validation message customizations for tDAR
 * Locale: EN
 */


$(document).ready(function(){
    jQuery.extend(jQuery.validator.messages, {
        extension: jQuery.validator.format("Please upload a file with one of the following extensions:{0}.")
    });
});
