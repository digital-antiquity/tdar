const common = require("./tdar.common");
const contexthelp = require("./tdar.contexthelp");
const autocomplete = require("./tdar.autocomplete");


/**
     * this function manages the display of the checkboxes next to a column field when someone changes one of the values, it changes the color if mapped properly
     * to something
     */
    function _registerCheckboxInfo() {
        console.log("registerCheckboxInfo called", arguments[0], arguments[1]);
        var $target = $($(this).parents(".datatablecolumn").first());
        var val = $('.columnEncoding:checked', $target).val();
        var square = $target.find("span.columnSquare");
        var ontologyInfo = $target.find("div.ontologyInfo");
        var codingInfo = $target.find("div.codingInfo");
        var measurementInfo = $target.find("div.measurementInfo");

        if (val == 'COUNT' || val == 'MEASUREMENT' || val == 'FILENAME') {
            ontologyInfo.hide();
        } else {
            ontologyInfo.show();
        }

        if (val != 'CODED_VALUE') {
            codingInfo.hide();
        } else {
            codingInfo.show();
            ontologyInfo.hide();
        }

        if (val == "MEASUREMENT") {
            measurementInfo.show();
        } else {
            measurementInfo.hide();
        }

        // Decorate the 'column square' element in this section of the form to indicate the type of column and it's
        // level of "completeness". We start out by removing all decorations from the indicator, and (potentially)
        // add them back as we analyze this section.
        //square.removeClass();

        //workaround for jquery-ui removeClass() bug #9015 (http://bugs.jqueryui.com/ticket/9015)
        square.removeAttr("class");

        square.addClass("columnSquare");

        var ontolog = $target.find("input.ontologyfield:visible").first().val();
        var dataType = $target.find("input.dataType").first().val();
        var codig = $target.find("input.codingsheetfield:visible").first();
        var codingSheetId = $('.codingsheetidfield', $target).val();
        var unit = $target.find("select.measurementUnit:visible").first();

        if (dataType == undefined || dataType.indexOf('INT') == -1 && dataType.indexOf('DOUBLE') == -1) {
            $(".columnEncoding[value='MEASUREMENT']", $target).prop('disabled', true);
            $(".columnEncoding[value='COUNT']", $target).prop('disabled', true);
        }

        var valid = false;
        var uncoded = false;
        if (val == 'COUNT') {
            square.addClass("count");
            valid = true;
        } else if (val == 'MEASUREMENT') {
            if (unit != undefined && unit.val() != '') {
                square.addClass("measurement");
                valid = true;
            }
        } else if (val == 'CODED_VALUE') {
            // console.log(codingSheetId + " " + isNaN(parseInt(codingSheetId)));
            if (!isNaN(parseInt(codingSheetId))) {
                square.addClass("coded");
                valid = true;
            }
        } else if (val == 'UNCODED_VALUE' || val == 'FILENAME') {
            square.addClass("uncoded");
            uncoded = true;
            valid = true;
        }

        if (ontolog != undefined && ontolog != '') {
            square.addClass("integration");
        }

        if (!valid) {
            square.addClass("invalid");
        }

        var subcat = $target.find(".categorySelect").first();
        var txt = $target.find("textarea.resizable").first();
        if (subcat != undefined && subcat.val() > 0 && txt != undefined && txt.val().length > 0) {
            square.addClass("complete");
        }

        if (pageInitialized) {
            // ... then it's safe to update the summary table (otherwise, the summary table would be updated n times
            // as the page loads)
            _updateSummaryTable();
        }
    }

    /**
     * used to bind to a select element to jump to a specific part of the page
     */
    function _gotoColumn($el) {
        var idVal = $el.val();
        document.getElementById(idVal).scrollIntoView();
    }

    var pageInitialized = false;

    /**
     * Init the data-table-column metadata page to setup the status table and data validation
     */
    function _init(formId) {

        var $form = $(formId);
        common.suppressKeypressFormSubmissions($form);

        //Use a plugin if browser doesn't support resizeable textareas
        //http://caniuse.com/#feat=css-resize
        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

        // set up ajax calls, no caching
        $.ajaxSetup({
            cache: false
        });

        common.applyWatermarks(document);

        $('#table_select').change(function () {
            window.location = '?dataTableId=' + $(this).val();
        });

        $form.delegate(":input", "blur change", _registerCheckboxInfo);

        contexthelp.initializeTooltipContent("#edit-metadata-form");

        console.debug('binding autocompletes');

        // bugfix: deferred registration didn't properly register expando button. If this is too slow, but delegate inside of _applyComboboxAutocomplete
        autocomplete.applyComboboxAutocomplete($('input.codingsheetfield', $form), "CODING_SHEET");
        autocomplete.applyComboboxAutocomplete($('input.ontologyfield', $form), "ONTOLOGY");

        console.debug('intitializing columns');
        // determine when to show coding-sheet, ontology selection based on column encoding value
        // almost all of the startup time is spent here
        $('input.ontologyfield').change(_registerCheckboxInfo).change();
        pageInitialized = true;
        _updateSummaryTable();
        // clear all hidden ontology/coding sheet hidden fields to avoid polluting the controller
        $form.submit(function () {
            $('input', $('.ontologyInfo:hidden')).val('');
            $('input', $('.codingInfo:hidden')).val('');
        });

        $("#fakeSubmitButton").click(function () {
            $("#submitButton").click();
        });

        var $window = $(window);

        $("#chooseColumn").change(function (e) {
            _gotoColumn($(this));
        });
        
        $form.FormNavigate();
    }

    function _pagination(idPrefix) {
        var $id = $("#recordsPerPage" + idPrefix); 
        $id.change(function () {
            var url = window.location.search.replace(/([?&]+)recordsPerPage=([^&]+)/g, "");
            //are we adding a querystring or merely appending a name/value pair, i.e. do we need a '?' or '&'?
            var prefix = "";
            if (url.indexOf("?") != 0) {
                prefix = "?";
            }
            url = prefix + url + "&recordsPerPage=" + $id.val();
            window.location = url;
        });
    }
    
    /**
     * Updates the SummaryTable based on column validation
     */
    function _updateSummaryTable() {
        var $summary = $("#summaryTable");
        $(".integration_label", $summary).html($("div.datatablecolumn .columnSquare.integration").length);
        $(".coded_label", $summary).html($("div.datatablecolumn .columnSquare.coded").length);
        $(".uncoded_label", $summary).html($("div.datatablecolumn .columnSquare.uncoded").length);
        $(".error_label", $summary).html($("div.datatablecolumn .columnSquare.invalid").length);
        $(".count_label", $summary).html($("div.datatablecolumn .columnSquare.count").length);
        $(".measurement_label", $summary).html($("div.datatablecolumn .columnSquare.measurement").length);
    }

    // expose public elements
    module.exports = {
        "init": _init,
        "gotoColumn": _gotoColumn,
        "updateSummaryTable": _updateSummaryTable,
        "registerCheckboxInfo": _registerCheckboxInfo,
        "initPagination": _pagination
    };

