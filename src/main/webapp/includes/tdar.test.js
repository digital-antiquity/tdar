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
     
     
     var _addCss = function() {
         var stylesheet = $("<style id='bootlint-style' type='text/css'>" +
                 //".bootlint-error {background-color: rgba(255,0,0, 0.2)}" +
                 ".bootlint-badkid {outline: 3px dotted red}" +
                 ".bootlint-gridfit-too-big {outline: 3px dotted purple}" +
                 ".bootlint-gridfit-too-small {outline: 3px dotted orange}" +
         		"</style>");
         $('#bootlint-style').remove();
         $('head').append(stylesheet);
     };
     
     var _highlight = function (el) {
         $(el).css({"background-color":"red", "opacity":0.5});
     }
     
     $.fn.bootlint = function() {
         _addCss();
         var _cssAnySpan = ".span1,.span2,.span3,.span4,.span5,.span6,.span6,.span7,.span8,.span9,.span10,.span11,.span12";
         var _$rows = this.find('.row, .controls-row, .row-fluid'); 
         var _$spans = this.find(_cssAnySpan);
         var _this = this;
         var $this = this;  //"this" is a jquery object
     
         var _idcounter = 0;
         var _getid = function(el) {
             if(!$(el).attr("id")) {
                 $(el).attr("id", "bootlint-elem-" + _idcounter++);
             }
             return $(el).attr("id");
         }
         
         console.log("you selected %s", $this);
    
         var _verifyRow = function($row, info) {
             //assert row's direct children all specify grid-size
             var $badKids = $row.children().not("[class*=span]").not(":hidden");
             
             //direct block-elements of a row should have  grid-size
             $badKids.each(function() {
                 var id = _getid(this);
                 console.warn("badkid: child element #%s should have grid size. class is '%s'", id, $(this).attr("class"));
             }).addClass("bootlint-error " + info.css);
         };
         
         var _verifyRows = function(info) {
             _$rows.each(function(){
                 _verifyRow($(this), info);
             });
         };
         
         var _parseSpan = function(classAttr) {
             if(!classAttr) return 0;
             var rex = /.*?\bspan(\d|1[012]?)\b.*/;
             return rex.test(classAttr) ? parseInt(classAttr.replace(rex, "$1")) : 0;
         };
         
         var _calcSpanTotal = function($elems) {
             if($elems.length === 0 ) return 0;
             var total = 0;
             $.each($elems, function(){
                 total += _parseSpan($(this).attr("class"));
             });
             return total;
         };
         
         var _verifyRowSize = function($row, info) {
             var $parent = $row.closest(_cssAnySpan);
             var parentSize = $parent.length ? _parseSpan($parent.attr("class")) : 12;
             //account for rows that are first descendent of wells
             if($row.parent().closest(".row, .row-fluid, .controls-row, .well").is(".well")) {
                 parentSize--;
             }
             //account for .controls-row margin ( about 2)
             if ($row.is(".controls-row") && $row.closest(".form-horizontal").length > 0) {
                 parentSize -= 2;
             }
             var childSize = _calcSpanTotal($row.children());
             if(parentSize !== childSize ) {
                 var id = _getid($row[0]);
                 console.warn("gridfit: row #%s colcount is %s but colcount of children is %s ",
                         id, parentSize, childSize);
                 $row.addClass("bootlint-error");
                 $row.addClass("bootlint-gridfit-too-" + (parentSize > childSize ? "big" : "small"));
             }
             
         };
         
         var _verifySpanSizes = function(info) {
             _$rows.each(function(){
                 _verifyRowSize($(this), info);
             });
         };
         
         
         var _testinfo = {
                 //direct children of .rows should have grid size (.span[1-12])
                 "badkid": {
                     test: _verifyRows,
                     css: "bootlint-badkid"
                 },
                 
                 //all grid-size elements should have .row parent
                 "orphans": {
                     test: undefined
                 },
                 
                 //related to orphan test -- the orphanage is a container that has no .row class
                 "orphanage": {},
                 
                 //combined colcount of child elements should equal colcount of row's inherited colcount
                 "gridfit": {
                     test: _verifySpanSizes,
                     css: "bootlint-gridfit"
                 },
                 
                 //ambiguous name test: classes that have "span" in their name will get matched by bootstrap.css
                 "ambiguity": {} 
         };
         
         //TODO: allow caller to override which tests to run.
         var _tests = ["badkid", "gridfit"];
         
         //return jquery selection when finished
         $.each(_tests, function() {
             var info =_testinfo[this];
             if(info) info.test(info);
         });
         
         
         return $this;
     };
     
})( jQuery );