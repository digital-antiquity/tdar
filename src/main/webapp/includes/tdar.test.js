/** 
 * tDAR testing utilities.
 * 
 * This is only intended to be accessed via the console and bookmarklets - do not reference this file in any other scripts.
 */


//bootlint plugin - hightights any occurances of elements that violate bootstrap grid-layout semantics.
// - direct children of .row must have grid-size (span1 - span12)
// - TODO: total colcount of .row's  direct children must not exceed either 1) colcount of row's parent element, or 2) span12
// - TODO: misc semantic errors; element cannot have .row and .span,  .span element cannot contain direct child .span  
(function( $ ) {
     "use strict";
     
     var _highlight = function (el) {
         $(el).css({"background-color":"red", "opacity":0.5});
     }
     
     
     $.fn.bootlint = function() {
     
         var $this = this;  //"this" is a jquery object
         
         console.log("you selected %s", $this);
    
         var _verifyRow = function($row) {
             if(!$row.is(".row, .controls-row, .row-fluid"))  { 
                 console.error("element isn't a bootstap row. terminating");
                 return $this;
             }
             //assert row's direct children all specify grid-size
             var $badKids = $row.children().not("[class*=span]");
             
             //direct block-elements of a row should have  grid-size
             $badKids.each(function() {
                 console.error(this);
                 $(this).css("background-color", "red");
             });
         }
         
         //return jquery selection when finished
         return $this.find(".row, .controls-row, .row-fluid").each(function() {
             _verifyRow($(this));
         });
         
     };
     
     
})( jQuery );