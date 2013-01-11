$.validator.addMethod("formatUS", function(value, element) {
    return this.optional(element) || value.match(/^(0[1-9]|1[012]|[1-9])\/(0[1-9]|[12][0-9]|3[01]|[1-9])\/(19|20)\d\d$/);
}, "The date format mm/dd/yyyy is expected");

$.validator.addMethod("latLong", function(value, element) {
    return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator.addMethod("reasonableDate",
        function(value, element) {
            var intVal = parseInt(value);
            // allow -1 for internal management of things that don't have dates
            return (intVal == value && (intVal == -1 || intVal > 1000
                    && intVal < 3000));
        }, "a date in the last millenia is expected");

$.validator.addMethod("currentyearorlater",
        function(value, element) {
            var intVal = parseInt(value);
            // allow -1 for internal management of things that don't have dates
            return (intVal == value && (intVal >= 1900 + (new Date()).getYear()
                    && intVal < 2050));
        }, "a date in the last millenia is expected");


$.validator.addMethod("month",
        function(value, element) {
            var intVal = parseInt(value);
            // allow -1 for internal management of things that don't have dates
            return (intVal == value && (intVal > 0 && intVal < 13));
        }, "please enter a valid month");

$.validator
        .addMethod(
                "isbn",
                function(value, element) {
                    if ($(element).is(':hidden'))
                        return true; // skip validation if not showing
                    return value
                            .match(/^(((\d+)-?(\d+)-?(\d+)-?([\dX]))|((978|979)-?(\d{9}[\dXx]))|)$/);
                }, "you must include a valid 10/13 Digit ISBN");

$.validator.addMethod("issn", function(value, element) {
    if ($(element).is(':hidden'))
        return true;// skip validation if not showing
    return value.match(/^((\d{4})-?(\d{3})(\d|X|x)|)$/);
}, "you must include a valid 8 Digit ISSN");


$.validator.addMethod("phoneUS", function(phone_number, element) {
    phone_number = phone_number.replace(/\s+/g, ""); 
    return this.optional(element) || phone_number.length > 9 &&
        phone_number.match(/^(1-?)?(\([2-9]\d{2}\)|[2-9]\d{2})(\s?-?)+[2-9]\d{2}(-?\s?)+\d{4}$/);
}, "Please specify a valid phone number");


$.validator.addMethod("ccverify", function(ccverify, element) {
    ccverify = ccverify.replace(/\s+/g, ""); 
    return this.optional(element) || ccverify.match(/^\d{3,4}$/);
}, "Please specify a valid verification number");

$.validator
        .addMethod(
                "descriptiveTitle",
                function(value, element) {
                    return !value
                            .match(/^(\s*)(dataset|collection|project|document|image|coding sheet|ontology)(\s*)$/i);
                }, "please select a more descriptive title");

$.validator.addMethod("float", function(value, element) {
    return value.match(/^(((\-?)(\d+)(\.?)(\d*))|)$/);
}, "a valid lat/long in the format DEG.Min/Sec (eg. -67.892068) required");

$.validator
        .addMethod(
                "validIdRequired",
                function(value, element) {
                    if (parseInt(value) != undefined && parseInt(value) > 0) {
                        return true;
                    } else if (evaluateAutocompleteRowAsEmpty(element, 0)) {
                        return true;
                    }
                    return false;
                },
                function(value, element) {
                    var msg = "";
                    $("input[type=text]:visible",
                            $($(element).attr("autocompleteParentElement")))
                            .each(
                                    function() {
                                        if ($(this).val() != '') {
                                            msg += " "
                                                    + $(this).attr("placeholder")
                                                    + ":" + $(this).val();
                                        }
                                    });
                    msg += "  is not a valid, registered user.  If you do not wish to add or specify a user, leave all fields in this section blank.";
                    return msg;
                });

// http://stackoverflow.com/questions/1260984/jquery-validate-less-than
$.validator.addMethod('lessThanEqual', function(value, element, param) {
    if (this.optional(element))
        return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i <= j;
}, "This value must be less than the maximum value");

$.validator.addMethod('greaterThanEqual', function(value, element, param) {
    if (this.optional(element))
        return true;
    var i = parseInt(value);
    var j = parseInt($(param).val());
    return i >= j;
}, "This value must be greater than the minimum value");

$.validator.addMethod('asyncFilesRequired', function(value, elem) {
    return $('tr', '#files').not('.noFiles').size() > 0;
}, "At least one file upload is required.");

// $.validator's built-in number rule does not accept leading decimal points
// (e.g.'.12' vs. '0.12'), so we replace with our own
$.validator.addMethod('number', function(value, element) {
    return this.optional(element)
            || /^-?(?:\d+|\d{1,3}(?:,\d{3})+)?(?:\.\d+)?$/.test(value);
}, $.validator.messages.number);

$.validator.addMethod('integer', function(value, element) {
    return this.optional(element)
            || /^-?(?:\d+)$/.test(value);
}, $.validator.messages.number);

$.validator.addMethod('required-visible', function(value, element) {
    var $element = $(element);
    if ($element.is(':hidden')) return true; 
    return $element.val() != '';
}, "this element is required");
