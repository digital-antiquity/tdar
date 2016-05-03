/**
 * Additional jQuery Validation methods used in tDAR forms
 */

//FIXME: Audit these methods; see what needs to be modified/renamed/removed  (TDAR-3498)
(function ($) {

    //FIXME: Not used
    $.validator.addMethod("formatUS", function (value, element) {
        return this.optional(element) || value.match(/^(0[1-9]|1[012]|[1-9])\/(0[1-9]|[12][0-9]|3[01]|[1-9])\/(19|20)\d\d$/);
    }, "The date format mm/dd/yyyy is expected");

    //FIXME: obviated by tdar.maps.js, and anyway I think this is funtionally no different than the "number" rule.
    $.validator.addMethod("latLong", function (value, element) {
        return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
    }, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

    /**
     * Restrict 4-digit year to what we define to be "reasonable" (between 1001 & 2999)
     */
    $.validator.addMethod("reasonableDate", function (value, element) {
        var intVal = parseInt(value);
        // allow -1 for internal management of things that don't have dates
        return (intVal == value && (intVal == -1 || intVal > 1000 && intVal < 3000));
    }, "a date in the last millenia is expected");

    //FIXME: not used
    $.validator.addMethod("currentyearorlater", function (value, element) {
        var intVal = parseInt(value);
        // allow -1 for internal management of things that don't have
        // dates
        return (intVal == value && (intVal >= 1900 + (new Date()).getYear() && intVal < 2050));
    }, "a date in the last millenia is expected");

    //FIXME: not used
    $.validator.addMethod("month", function (value, element) {
        var intVal = parseInt(value);
        // allow -1 for internal management of things that don't have dates
        return (intVal == value && (intVal > 0 && intVal < 13));
    }, "please enter a valid month");

    /**
     * String must adhere to ISBN 10/13 digit format
     */
    $.validator.addMethod("isbn", function (value, element) {
        if ($(element).is(':hidden')) {
            return true;
        } // skip validation if not showing
        return value.match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
    }, "you must include a valid 10/13 Digit ISBN");

    /**
     * String must adhere to 8-digit ISSN format
     */
    $.validator.addMethod("issn", function (value, element) {
        if ($(element).is(':hidden')) {
            return true;
        }// skip validation if not showing
        return value.match(/^((\d{4})-?(\d{3})(\d|X|x)|)$/);
    }, "you must include a valid 8 Digit ISSN");
    
    $.validator.addMethod("couponFilesOrSpace", function(value, element) {
            return !(parseInt($("#create-code_numberOfMb").val())  > 0) && (parseInt($("#create-code_numberOfFiles").val()) > 0);
    });

    $.validator.addMethod("doi", function (value, element) {
    	var $e = $(element);
        if ($e.is(':hidden') || $e.val() == '') {
            return true;
        }// skip validation if not showing
        // from : http://stackoverflow.com/questions/27910/finding-a-doi-in-a-document-or-page
        return value.match(/^\b(10[.][0-9]{4,}(?:[.][0-9]+)*\/(?:(?!["&\'<>])\S)+)\b/);
    }, "you must include a valid DOI e.g.: 10.1000/182");

    //FIXME: already implemented in additional-methods.js
    $.validator.addMethod("phoneUS", function (phone_number, element) {
        phone_number = phone_number.replace(/\s+/g, "");
        return this.optional(element) || phone_number.length > 9 && phone_number.match(/^(1-?)?(\([2-9]\d{2}\)|[2-9]\d{2})(\s?-?)+[2-9]\d{2}(-?\s?)+\d{4}$/);
    }, "Please specify a valid phone number");

    //FIXME: not used
    $.validator.addMethod("ccverify", function (ccverify, element) {
        ccverify = ccverify.replace(/\s+/g, "");
        return this.optional(element) || ccverify.match(/^\d{3,4}$/);
    }, "Please specify a valid verification number");

    /**
     * Disallow short, simplistic titles e.g. "image"
     */
    $.validator.addMethod("descriptiveTitle", function (value, element) {
        return !value.match(/^(\s*)(dataset|collection|project|document|image|coding sheet|ontology|video|scan)(\s*)$/i);
    }, "Please use a more descriptive title");

    //FIXME: not used, redundant
    $.validator.addMethod("float", function (value, element) {
        return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
    }, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

    /**
     * Only allow value in this element if it has a sibling input that stores a peristable ID which is not empty.
     * Sometimes a user types a value in a autocomplete field, but focuses on another
     * element before the autocomplete lookup completes.
     */
    $.validator.addMethod("notValidIfIdEmpty", function (value, element) {
        //no need to continue if the text field is blank
        if ($.trim(value) === "") {
            return true;
        }

        var $id = $($(element).attr("autocompleteIdElement"));
        return parseInt($id.val()) > -1;
    }, "Please select a value from the autocomplete menu");

    /**
     * Only accept if element value is less than  or equal  to value of the specified element.
     * @param param other element to compare to the current element
     */
    $.validator.addMethod('lessThanEqual', function (value, element, param) {
        if (this.optional(element)) {
            return true;
        }
        var i = parseInt(value);
        var j = parseInt($(param[0]).val());
        return i <= j;
    }, "A {1} value must be less than the {2} value");

    /**
     * Only accept if element value is greater than or equal to the value of  the specified element.
     * @param param other element to compare
     */
    $.validator.addMethod('greaterThanEqual', function (value, element, param) {
        if (this.optional(element)) {
            return true;
        }
        var i = parseInt(value);
        var j = parseInt($(param[0]).val());
        return i >= j;
    }, "A {1} value must be greater than the {2} value");

    /**
     * Used with tdar "coverage date" control.  If the coverage date type is "NONE", the start and
     * end values must be blank.
     * @param {*} object.start is the "startYear" input element, object.end is the "endYear" input element
     */
    $.validator.addMethod('blankCoverageDate', function (value, element, param) {
        var concatval = "" + $(param.start).val() + $(param.end).val();
        return concatval.length === 0;
    }, "Choose a valid coverage date type:  'Calendar' or 'Radiocarbon'");

    /**
     * Form must include at least one file upload. Only applies to forms that use the "async" file upload control.
     */
    $.validator.addMethod('asyncFilesRequired', function (value, elem) {
        return $('tr', '#files').not('.noFiles').size() > 0;
    }, "At least one file upload is required.");

    //FIXME: consider renaming this instead of overriding original
    /**
     * A modification of the built-in "number" method.  Unlike the built-in version, this method accepts
     * numbers with a leading decimal point.   Also, our version does not accept scientific notation formats
     */
    $.validator.addMethod('number', function (value, element) {
        return this.optional(element) || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(value);
    }, $.validator.messages.number);

    /**
     * Only accept whole numbers
     */
    $.validator.addMethod('integer', function (value, element) {
        return this.optional(element) || /^-?(?:\d+)$/.test(value);
    }, $.validator.messages.number);

    /**
     * Value is only required when element is visible.
     *
     * @deprecated  use 'ignore' option in validate() method instead. e.g.:
     *
     * $("#registerform").validate({ignore:":not(:visible)"});
     *
     */
    $.validator.addMethod('required-visible', function (value, element) {
        var $element = $(element);
        if ($element.is(':hidden')) {
            return true;
        }
        return $element.val() != '';
    }, "this element is required");

    //FIXME: this needs refactoring.  Just keep reading...
    /**
     * This method applies to "column type" radio buttons on the edit-column-metadata page.  If the column
     * contains invalid metadata for the selected column type,  this method indicates that the column type field
     * is invalid.
     */
    $.validator.addMethod("columnEncoding", function (value, element) {
        //when using this method on radio buttons, validate only calls this function once per radio group (using the first element of the group for value, element)
        //However, we still need to put put the error message in the title attr of the first element in group. Feels hokey but there you are.
        console.log('validating:' + element.id + "\t value:" + value);
        var $element = $(element);
        var $section = $("#" + element.id).parents(".datatablecolumn").first();
        var displayName = "'" + $section.find('h3 > .displayName').text() + "'";
        var $selectedElement = $section.find('input[type=radio]:checked').first();

        //if we came here by way of a form 're-validate', we need to make sure that validation logic in registerCheckboxInfo happens first.
        /* FIXME: (TDAR-4470)this is the wrong way to do this.  Instead of calling out to an external validation routine,  the logic of registerCheckboxInfo needs to be a $.validation method,  and the code that is responsible for coloring the column "status" should be a listener to validation events.  */
        TDAR.datasetMetadata.registerCheckboxInfo.call(element, "tdar.formValidateExtensions.js");

        if ($selectedElement.is(':disabled')) {
            var val = $selectedElement.val().toLowerCase().replace('_', ' ');
            element.title = "The selection '" + val + "' is no longer valid for column " + displayName;
            return false;
        }

        var $target = $element.closest(".datatablecolumn");
        var square = $target.find(".columnSquare.invalid");
        if (square == undefined || square.length == 0) {
            return true;
        } else {
            element.title = "Column " + displayName + " contains errors or invalid selections";
            return false;
        }
    });

    
    /**
     * Only allow confidential file uploads if the user has added at least one  "contact" person/institution.
     */
    $.validator.addMethod("confidential-contact-required", function (value, element) {
        if (value === "PUBLIC") {
            return true;
        }
                var $table = $('#creditTable');
                var institutions = $table.find(".creatorInstitution").not(".hidden").toArray();
                var persons = $table.find(".creatorPerson").not(".hidden").toArray();
                var grepForValidContacts = function (creators) {
                    //first, filter out the rows that don't have the 'contact' role selected
                    var contactRows = $.grep(creators, function (row, idx) {

                        var isContact = $(row).find(".creator-role-select").val() === "CONTACT";
                        return isContact;
                    });

                    //now make sure those contacts aren't blank
                    var validContacts = $.grep(contactRows, function (row, idx) {
                        var isValid;
                        if ($(row).hasClass("creatorPerson")) {
                            //person must have firstname, lastname specified

                            var nonBlanks = $(row).find("[name $= 'lastName'],[name $= 'firstName']").filter(function () {
                                return $(this).val() != "";
                            });
                            isValid = nonBlanks.length == 2;
                        } else {
                            //institution must not be blank
                            isValid = $(row).find("[name $= 'institution.name']").val() != "";
                        }
                        return isValid;
                    });

                    return validContacts;
                };

                var contactCount = grepForValidContacts(institutions).length + grepForValidContacts(persons).length;
                return contactCount > 0;

            }, "You must have at least one person/institution listed as a 'contact' under <b>Individual and Institutional Roles</b> when marking a file 'confidential'");

    /**
     * Case-insensitive version of equalTo builtin.
     */
    $.validator.addMethod("equalToIgnoreCase", function (value, element, param) {
        return this.optional(element) ||
            (value.toLowerCase() == $(param).val().toLowerCase());
    }, "Please enter the same value again. Not case-sensitive.");

})(jQuery);