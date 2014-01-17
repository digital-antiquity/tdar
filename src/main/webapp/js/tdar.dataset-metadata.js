(function(TDAR, $) {
    'use strict';

    /**
     * this function manages the display of the checkboxes next to a column field when someone changes one of the values, it changesthe color if mapped properly
     * to something
     */
    function _registerCheckboxInfo() {
        var $target = $($(this).parents(".datatablecolumn").first());
        var val = $('.columnEncoding:checked', $target).val();
        var square = $target.find("span.columnSquare");
        var mapping = $target.find("div.mappingInfo");
        var ontologyInfo = $target.find("div.ontologyInfo");
        var codingInfo = $target.find("div.codingInfo");
        var measurementInfo = $target.find("div.measurementInfo");

        if (val == 'CODED_VALUE' || val == 'UNCODED_VALUE') {
            mapping.show();
        } else {
            mapping.hide();
        }

        if (val == 'COUNT' || val == 'MEASUREMENT' || val == 'MAPPED_VALUE') {
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

        square.removeClass();

        square.addClass("columnSquare");

        var ontolog = $target.find("input.ontologyfield:visible").first().val();
        var dataType = $target.find("input.dataType").first().val();
        var codig = $target.find("input.codingsheetfield:visible").first();
        var codingSheetId = $('.codingsheetidfield', $target).val();
        var unit = $target.find("select.measurementUnit:visible").first();
        var map = $target.find(':input.mappingValue:visible:checked').first();
        var mapDetail = $target.find('.mappingDetail').first();
        mapDetail.hide();

        if (dataType == undefined || dataType.indexOf('INT') == -1 && dataType.indexOf('DOUBLE') == -1) {
            // TODO: confirm test coverage, then delete this comment.
            $(".columnEncoding[value='MEASUREMENT']", $target).prop('disabled', true);
            $(".columnEncoding[value='COUNT']", $target).prop('disabled', true);
        }

        var valid = false;
        var uncoded = false;
        if (val == 'COUNT') {
            square.addClass("count");
        } else if (val == 'MEASUREMENT') {
            if (unit != undefined && unit.val() != '') {
                square.addClass("measurement");
                valid = true;
            }
        } else if (ontolog != undefined && ontolog != '') {
            square.addClass("integration");
        } else if (val == 'CODED_VALUE') {
            // console.log(codingSheetId + " " + isNaN(parseInt(codingSheetId)));
            if (!isNaN(parseInt(codingSheetId))) {
                square.addClass("coded");
                valid = true;
            }
        } else if (val == 'UNCODED_VALUE') {
            square.addClass("uncoded");
            uncoded = true;
            valid = true;
        }

        if (map != undefined && map.val() == "true") {
            square.addClass("mapped");
            mapDetail.show();
        }

        if (valid) {
            square.removeClass('invalid');
            if (!uncoded) {
                square.removeClass('uncoded');
            }
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

    function _gotoColumn($el) {
        var idVal = $el.val();
        document.getElementById(idVal).scrollIntoView();
    }

    var pageInitialized = false;

    function _init(formId) {

        var $form = $(formId);

        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

        // set up ajax calls, no caching
        $.ajaxSetup({
            cache : false
        });

        TDAR.common.applyWatermarks(document);

        $('#table_select').change(function() {
            window.location = '?dataTableId=' + $(this).val();
        });

        $form.delegate(":input", "blur change", TDAR.datasetMetadata.registerCheckboxInfo);

        TDAR.contexthelp.initializeTooltipContent("#edit-metadata-form");

        console.debug('binding autocompletes');

        // bugfix: deferred registration didn't properly register expando button. If this is too slow, but delegate inside of _applyComboboxAutocomplete
        TDAR.autocomplete.applyComboboxAutocomplete($('input.codingsheetfield', $form), "CODING_SHEET");
        TDAR.autocomplete.applyComboboxAutocomplete($('input.ontologyfield', $form), "ONTOLOGY");

        console.debug('intitializing columns');
        // determine when to show coding-sheet, ontology selection based on column encoding value
        // almost all of the startup time is spent here
        $('input.ontologyfield').change(TDAR.datasetMetadata.registerCheckboxInfo).change();
        pageInitialized = true;
        TDAR.datasetMetadata.updateSummaryTable();
        // clear all hidden ontology/coding sheet hidden fields to avoid polluting the controller
        $form.submit(function() {
            $('input', $('.ontologyInfo:hidden')).val('');
            $('input', $('.codingInfo:hidden')).val('');
        });

        $("#fakeSubmitButton").click(function() {
            $("#submitButton").click();
        });

        var $window = $(window);

        $("#chooseColumn").change(function(e) {
            TDAR.datasetMetadata.gotoColumn($(this));
        });

        TDAR.common.initFormValidation($("#edit-metadata-form")[0]);
    }

    function _updateSummaryTable() {
        var $summary = $("#summaryTable");
        $(".integration_label", $summary).html($("div.datatablecolumn .columnSquare.integration").length);
        $(".coded_label", $summary).html($("div.datatablecolumn .columnSquare.coded").length);
        $(".uncoded_label", $summary).html($("div.datatablecolumn .columnSquare.uncoded").length);
        $(".error_label", $summary).html($("div.datatablecolumn .columnSquare.invalid").length);
        $(".count_label", $summary).html($("div.datatablecolumn .columnSquare.count").length);
        $(".mapped_label", $summary).html($("div.datatablecolumn .columnSquare.mapped").length);
        $(".measurement_label", $summary).html($("div.datatablecolumn .columnSquare.measurement").length);
    }
    // expose public elements
    TDAR.datasetMetadata = {
        "init" : _init,
        "gotoColumn" : _gotoColumn,
        "updateSummaryTable" : _updateSummaryTable,
        "registerCheckboxInfo" : _registerCheckboxInfo
    };

})(TDAR, jQuery);