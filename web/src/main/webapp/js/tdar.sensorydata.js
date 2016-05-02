(function (TDAR, $) {
    'use strict';


    //show relevant fields based on scan type value.

    function showRelevantSurveyFields(scanType) {

        var $conditionalElems = $('.conditional-scantype').hide();
        if (scanType) {
            //show the relevent fields, e.g. if scantype was TOF,  the element
            var cssClass = "." + scanType.toLowerCase();
            $conditionalElems.filter(cssClass).show()
        }
    }

    function showLegacyScannerTechFields(elemScannerTech) {
        // get the parent element of the scanner tech field
        console.debug("showing scanner tech fields for:");
        console.debug(elemScannerTech);
        // determine which div to show based on the value of the scanner tech
        var divmap = {
            'TIME_OF_FLIGHT': '.scantech-fields-tof',
            'PHASE_BASED': '.scantech-fields-phase',
            'TRIANGULATION': '.scantech-fields-tri'
        };
        var parent = $(elemScannerTech).parents('.scantech-fields');
        parent.find('.scantech-field').addClass('hide');
        var scannerTechnologyValue = $(elemScannerTech).val();
        if (scannerTechnologyValue) {
            var targetClass = divmap[scannerTechnologyValue];
            console.log("showing all elements of class: " + targetClass);
            parent.find(targetClass).removeClass('hide');
            //        $(elemScannerTech).siblings(targetClass).removeClass('hide');
            // $(elemScannerTech).parent().find('.scantech-fields-tof');
        }

    }

    
    function scanAdded(rowElem) {
        // get the select element
        var scannerTechElem = $('.scannerTechnology', rowElem);
        // the scanner type changed to blank, so we hide the scanner-tech-specific
        // fields, and bind to the select change
//        showScannerTechFields(scannerTechElem);
//        $(scannerTechElem).change(function() {
//            var elem = this;
//            showScannerTechFields(elem);
//        });
    }

     //return true if any form field in this div are populated

    function hasContent(div) {
        var found = false;
        var $div = $(div);
        $div.find("input[type=text],textarea").each(function(idx, elem) {
            if ($.trim(elem.value).length > 0) {
                found = true;
                return false;
            }
        });
        return found;
    }

    /**
     * Sensory Data Support
     */
    // display the proper fields that correspond to the current value of the
    // supplend scanner technology dropdown element.
    //legacy -- not used anymore?
    function showScannerTechFields(elemScannerTech) {
        // get the parent element of the scanner tech field
        console.debug("showing scanner tech fields for:");
        console.debug(elemScannerTech);
        // determine which div to show based on the value of the scanner tech
        var divmap = {
            'TIME_OF_FLIGHT' : '.scantech-fields-tof',
            'PHASE_BASED' : '.scantech-fields-phase',
            'TRIANGULATION' : '.scantech-fields-tri'
        };
        var parent = $(elemScannerTech).parents('.scantech-fields');
        parent.find('.scantech-field').addClass('hide');
        var scannerTechnologyValue = $(elemScannerTech).val();
        if (scannerTechnologyValue) {
            var targetClass = divmap[scannerTechnologyValue];
            console.log("showing all elements of class: " + targetClass);
            parent.find(targetClass).removeClass('hide');
//            $(elemScannerTech).siblings(targetClass).removeClass('hide');
            // $(elemScannerTech).parent().find('.scantech-fields-tof');
        }

    }
    
    function _initEdit() {
        //show legacy edit fields if they have content
        $('#registeredDatasetDiv, #polygonalMeshDatasetDiv, #divScanInfo').each(function(idx, div) {
            if (hasContent(div)) {
                $(div).show();

                $('.scannerTechnology').each(
                    function(i, elem) {
                        var scannerTechElem = elem;
//                        showScannerTechFields(scannerTechElem);
                        $(scannerTechElem).change(function() {
                            showLegacyScannerTechFields(scannerTechElem);
                        });
                    }
                );

            }
        });


        $('#sensoryDataScans').bind('repeatrowadded', function(e, parent, newRow) {
            scanAdded(newRow);
        });


         //show/hide content that is conditional on the value of the Scanner Technology field
        $('#divScannerTechnologyOptions').click(function() {
            var val = $('#divScannerTechnologyOptions input[type=radio]:checked').val();
            if (val) {
                $('#scantypeFileReminder').show();
                var $ul = $('#ulTemplateList');
                var highlightItem = "." + val.toLowerCase();
                $ul.find("span").removeClass('highlighted').addClass("muted");
                var $highlighted = $ul.find(highlightItem).find('span');
                $highlighted.removeClass("muted").addClass("highlighted");
            } else {
                $('#scantypeFileReminder').hide();
            }
            showRelevantSurveyFields(val);
        }).click();


    }

    //expose public elements
    TDAR.sensoryData = {
        "initEdit": _initEdit
    };

})(TDAR, jQuery);