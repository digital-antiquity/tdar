/**
 * Register a "repeatable container". That is, a table element (or any other block-level element) which
 * contains form fields that the user may duplicate one row at a time.
 *
 * Once registered, this method inserts an "add another" button in the DOM just after the repeatble container.
 * Clicking the add-another button clones a a child element in the table (specified by options.rowSelector), and
 * appends new row to the repeatable container.
 *
 * Events
 * -----
 * Repeat-row tables emit the following custom events:
 *
 * "repeatrowadded": triggered when the add-another handler adds a row to the repeatrow table.
 *      target: the repeatrow container element
 *      additionalParameters: [parentElement, clonedRowElement, indexOfNewRow, penultimateRowElement]
 *
 * "repeatrowbeforedelete": triggered when user clicks a row-delete button but before the handler removes the
 *                              row element from the DOM.
 *      target: the row element to-be deleted
 *      additionalParameters: none
 *
 * "repeatrowdeleted": trigger after the delete-row handler removes the row element from the DOM.
 *      target: parent element of the deleted row
 *      additionalParameters: none
 *
 * About Form Field Attributes:
 * ---------------------------
 * When the module clones a repeat-row,  it renames certain attribute values if the attribute values follow an
 * indexed naming pattern.  For example, if this module clones a row that contains the following element:
 *     <input type="text"  id="username_1_",  name="person.username[1]">
 *
 * The field in the cloned element will be:
 *     <input type="text" id="username_2_", name="person.username[2]>
 *
 * @param selector selector for the repeatable-row container (typically a <table>)
 * @param {{rowSelector: string, addAnother: string}} settings object.
 *
 *          addAnother: label for the "add another" button.  This function places the add-another button
 *                          in the DOM after the repeat-row container. (default: "> div.controls, .repeat-row")
 *
 *          rowSelector: selector that the add-another click handler uses to  identify the element that the
 *                          hanler will clone when the  add-another button.
 */
var registerRepeatable = function (selector, options) {

    var _options = {
        //select the last bootstrap "controls" div,  or the last element with the 'repeat-row' class
        rowSelector: "> div.controls, .repeat-row",
        addAnother: "add another"
    };

    $.extend(_options, options);

    var $parents = $(selector);
    $parents.each(function (index, parentElement) {
        //tag the repeat rows so we know which element to delete if delete button clicked
        $(_options.rowSelector, parentElement).addClass("repeat-row");

        var btnLabel = $(parentElement).data("add-another") || _options.addAnother;
        var $button = _button(btnLabel, parentElement.id + "AddAnotherButton");
        $('button', $button).click(function () {
            var element = $(_options.rowSelector, parentElement).last();
            var $clone = cloneSection(element, parentElement);
            var idx = $(parentElement).find('.repeat-row').length;  //FIXME: shouldn't this be length -1?
            $(parentElement).trigger("repeatrowadded", [parentElement, $clone[0], idx, element]);

            // set focus on the first input field (or designate w/ repeatrow-focus class).
            $("input[type=text], textarea, .repeatrow-focus", $clone).filter(":visible:first").focus();

        });
        $(parentElement).after($button);
        registerDeleteButtons(parentElement);
    });

};

/**
 * add the "delete" button handler to a newly-created repeat-row
 * @param parentElement the repeeat-row button element
 * @private
 */
var registerDeleteButtons = function (parentElement) {
    $(parentElement).on("click", ".repeat-row-delete", function (e) {
        var rowElem = $(this).parents(".repeat-row")[0];
        $(rowElem).trigger("repeatrowbeforedelete");
        deleteRow(rowElem);
        $(parentElement).trigger('repeatrowdeleted');
    });
};

// clone an element, append it to another element.
//  -To prevent attribute renaming for an element in a repeat-row div, apply ".repeat-row-skip" class
//  -To prevent a repeat-row child elemnent from being copied, apply ".repeat-row-remove"

/**
 * Clone an element and append it to another element.  Values for attributes such as ID & NAME are modified
 * if they follow an indexed naming pattern.
 *
 * @param element element to clone
 * @param appendTo element to contain the clone
 * @returns {*} the cloned element.
 * @private
 */
var cloneSection = function (element, appendTo) {

    var $element = $(element);
    var $clone = $element.clone();

    /*
     * we assume that the table row will have an ID that follows the convention _num_, and we will use this same convention for choosing the next ID
     * addribute for the row as well as any element inside the row that uses the same convention for the NAME and ID attributes.
     */
    var rex = /^(.*?_)(\d+)(_.*)$/i;
    var elementIdAttr = $element.attr("id");
    var match = rex.exec(elementIdAttr);

    //if element's id is in right format the rownum will be the 2nd submatch
    var currentId = parseInt(match[2]);

    var nextId = currentId + 1;
    var newRowId = nextId;

    var cloneIdAttr = elementIdAttr.replace(rex, "$1" + nextId + "$3");

    //TODO: remove error/warning labels from $clone (e.g.  form validation fails on last row, then you click 'add new row').
    $clone.find(".repeat-row-remove").remove();
    // update the id for our new row
    $clone.attr('id', cloneIdAttr);

    /*
     * Now that we've cloned the row, certain element attributes may need to be renamed (for example, input tags with name attributes of the form
     * "fieldval[0]" should be renamed "fieldval[1]". Our assumption is that every ID or NAME attribute that contains either "_num_" or "[num]" will
     * renamed.
     * 
     */

    //remove any tags that shouldn't be copied
    $clone;
    // skip any tags that with the repeat-row-skip attribute
    $clone.find('*').not(".repeat-row-skip").each(function () {
        var elem = this;
        $([ "id", "autoVal", "name", "autocompleteIdElement", "autocompleteParentElement" ]).each(function (i, attrName) {
            // replace occurrences of [num]
            _replaceAttribute(elem, attrName, '[' + currentId + ']', '[' + nextId + ']');

            // replace occurrences of _num_
            _replaceAttribute(elem, attrName, '_' + currentId + '_', '_' + nextId + '_');
        });
    });

    $element.after($clone);
    // maybe clear before appending?
    _clearInputs($clone);

    $clone.trigger("heightchange");

    return $clone;
};

/**
 * replace last occurrence of str in attribute with rep
 * @param elem the input element
 * @param attrName name of the attribute to modify
 * @param str string to search for in the attribute value
 * @param rep replacement string
 * @private
 */
var _replaceAttribute = function (elem, attrName, str, rep) {
    var oldval = $(elem).attr(attrName);
    if (!oldval) {
        return;
    }
    if (oldval.indexOf(str) === -1) {
        return;
    }

    var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
    var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length, oldval.length);
    var newval = beginPart + rep + endPart;
    $(elem).attr(attrName, newval);
};

/**
 * Clear (or reset) the values of any input fields contained by the specified element.
 * - for radio elements & checkboxes, uncheck any checked elements
 * - for SELECT elements,  select the first OPTION
 *
 * Opting out: add the .repeatrow-noreset class to any input elements that you want to opt-out of this reset behavior.
 *
 * @param $element container element (a jqselection)
 * @private
 */
var _clearInputs = function ($element) {

    var noresetClass = ".repeatrow-noreset"

    // enable any inputs in the row
    $element.find(":input").not(noresetClass)
        .removeAttr("readonly").removeAttr("disabled").prop("readonly", false).prop("disabled", false);

    // most input elements should have value attribute cleared (but not radiobuttons, checkboxes, or buttons)
    $element.find("input[type!=button],textarea").not(noresetClass).not('input[type=checkbox],input[type=radio]')
        .val("");

    // uncheck any checkboxes/radios
    $element.find("input[type=checkbox],input[type=radio]").not(noresetClass).prop("checked", false);

    // revert all select dropdowns to first option.
    $($element).find("select").not(noresetClass).prop("selectedIndex", 0);

    // allow html5 polyfills for watermarks to be added.  (//fixme: I think this is a bug/copypasta - watermarks shouldn't need to be re-applied)
    //TDAR.common.applyWatermarks($element);
};

/**
 * Attempt to delete the .repeat-row element closest to the specified element.  If the repeatable container only
 * has one row, clear the child inputs instead.
 * @param elem
 * @returns {boolean} true if a the function deleted a row, or false if the function simply cleared the values
 * @private
 */
var deleteRow = function (elem) {
    var $row = $(elem).closest(".repeat-row");
    var bDelete = $row.siblings(".repeat-row").length > 0;
    if (bDelete) {
        $row.remove();
    } else {
        _clearInputs($row);
    }
    return bDelete;
};

/**
 * generate an add-another button
 * @param label
 * @param id
 * @returns {*|jQuery|HTMLElement}
 * @private
 */
var _button = function (label, id) {
    var buttonId = id;
    if (!id) {
        buttonId = "btn" + label.replace(" ", "").toLowerCase();
    }
    var html = "<div class='control-group add-another-control'>" + "<div class='controls offset-3'>" + "<button class=' btn-xs  btn btn-light addanother' id='" + buttonId + "' type='button'><i class=\"fas fa-plus-circle\"></i> " + label + "</button>" + "</div>" + "</div>";
    return $(html);
};

module.exports =  {
    registerRepeatable: registerRepeatable,
    registerDeleteButtons: registerDeleteButtons,
    cloneSection: cloneSection,
    deleteRow: deleteRow,
    clearInputs: _clearInputs
};