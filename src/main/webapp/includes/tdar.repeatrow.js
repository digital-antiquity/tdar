/* Repeat "Row" Support" */


TDAR.namespace("repeatrow");

TDAR.repeatrow = function() {
    "use strict";
    
    /**
     *  public: register a repeatrow element
     *  This has the effect of adding a "add another"  button after each matched element.  Clicking the addnew button clones 
     *  the element specified by options.rowSelector, and places it after that element in the dom.
     *  
     *  events: 
     *      -"repeatrowadded": function(e, parentElement, clonedElement)
     */

    var _registerRepeatable = function(selector, options) {
        var _options = {
                //select the last bootstrap "controls" div,  or the last element with the 'repeat-row' class
                rowSelector: "> div.controls, .repeat-row",
                addAnother: "add another"
        };
        if(options) {
            $.extend(_options, options);
        }
        
        var $parents = $(selector);
        $parents.each(function(index, parentElement){
            //tag the repeat rows so we know which element to delete if delete button clicked
            $(_options.rowSelector, parentElement).addClass("repeat-row");
            
            var btnLabel =$(parentElement).data("add-another") || _options.addAnother;
            var $button = _button(btnLabel);
            $('button', $button).click(function() {
                var element = $(_options.rowSelector, parentElement).last();
                var $clone = _cloneSection(element, parentElement);
                var idx = $(parentElement).find('.repeat-row').length
                $(parentElement).trigger("repeatrowadded", [parentElement, $clone[0], idx]);

                // set focus on the first input field (or designate w/ repeatrow-focus class).
                $("input[type=text], textarea, .repeatrow-focus", $clone).filter(":visible:first").focus();
                
            });
            $(parentElement).after($button);
            $(parentElement).delegate(".repeat-row-delete","click", function(e){
                var rowElem = $(this).parents(".repeat-row")[0];
            	TDAR.repeatrow.deleteRow(rowElem);
            	$(rowElem).trigger('repeatrowdeleted');
            });
        });
        
    };

        
    // public: clone an element, append it to another element
    var _cloneSection = function(element, appendTo) {

        var $element = $(element);
        var $clone = $element.clone();

        /*
         * we assume that the table row will have an ID that follows the convention _num_, and we will use this same convention for choosing the next ID
         * addribute for the row as well as any element inside the row that uses the same convention for the NAME and ID attributes.
         */
        var rex = /_(\d+)_/i;
        var match = rex.exec($element.attr("id"));
        
        //the last occurance _num_ is our current id
        var currentId = parseInt(match[1]); 

        var nextId = currentId + 1;
        var newRowId = nextId;
        if ($element.attr("id") != undefined && $element.attr("id").indexOf("_") != -1) {
            while ("a" != "b") {
                newRowId = $element.attr("id").substring(0, $element.attr("id").lastIndexOf('_' + currentId + '_')) + "_" + nextId + '_';
                if ($(newRowId).length == 0)
                    break;
            }
        }
        
        //TODO: remove error/warning labels from $clone (e.g.  form validation fails on last row, then you click 'add new row').

        // update the id for our new row
        $clone.attr('id', newRowId);

        /*
         * Now that we've cloned the row, certain element attributes may need to be renamed (for example, input tags with name attributes of the form
         * "fieldval[0]" should be renamed "fieldval[1]". Our assumption is that every ID or NAME attribute that contains either "_num_" or "[num]" will
         * renamed.
         * 
         * However, we do not modify any tags that that have the css class"repeatRowSkip".
         */
        // console.debug("about to find each elment in $clone:" + currentId);
        $clone.find('*').each(function() {
            var elem = this;
            // skip any tags that with the repeatRowSkip attribute
            if (!$(elem).hasClass('repeatRowSkip')) {
                $([ "id", "autoVal", "name", "autocompleteIdElement", "autocompleteParentElement" ]).each(function(i, attrName) {
                    // replace occurances of [num]
                    _replaceAttribute(elem, attrName, '[' + currentId + ']', '[' + nextId + ']');

                    // replace occurances of _num_
                    _replaceAttribute(elem, attrName, '_' + currentId + '_', '_' + nextId + '_');
                });
            }
        });

        $element.after($clone);
        // maybe clear before appending?
        _clearInputs($clone);

        return $clone;
    }
    
    
    
    // private: replace last occurance of str in attribute with rep
    var _replaceAttribute = function(elem, attrName, str, rep) {
        var oldval = $(elem).attr(attrName);
        if (!oldval) return;
        if (oldval.indexOf(str) === -1) return;
        
        var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
        var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length,
                oldval.length);
        var newval = beginPart + rep + endPart;
        $(elem).attr(attrName, newval);
    }
    

    // private: clear input elements in a cloned element
    var _clearInputs = function($element) {

        // enable any inputs in the row
        $(":input", $element).removeAttr("readonly").removeAttr("disabled").prop("readonly", false).prop("disabled", false);

        // most input elements should have value attribute cleared (but not radiobuttons, checkboxes, or buttons)
        $("input[type!=button],textarea", $element).not('input[type=checkbox],input[type=radio]').val("");

        // uncheck any checkboxes/radios
        $("input[type=checkbox],input[type=radio]", $element).prop("checked", false);

        // remove "selected" from options that were already selected
        $("option[selected=selected]", $element).removeAttr("selected");

        // revert all select inputs to first option. 
        $("select", $element).find('option:first').attr("selected", "selected");
    };
    
    
    var _deleteRow = function(elem) {
        var $row = $(elem).closest(".repeat-row");
        if($row.siblings(".repeat-row").length > 0) {
            $row.remove();
        } else {
            _clearInputs($row);
        }
    }
    
    
    // private: return a dom button
    var _button = function(label) {
        var html = "<div class='control-group'>" +
        		"<div class='controls'>" +
        		"<button class='btn' type='button'><i class='icon-plus-sign'></i>" + label + "</button>" +
        		"</div>" +
        		"</div>"
       return $(html);
    };
    
    
    
    //return public members
    console.log("repeatrow loaded");
    return {
        registerRepeatable: _registerRepeatable,
        cloneSection: _cloneSection,
        deleteRow: _deleteRow
    };
    
}();


