(function (TDAR, $) {
    'use strict';

    var _initLogin = function () {
        //hack for autofill
        setTimeout(function () {
            $("#loginUsername").focus();
        }, 1000);
        $('#loginForm').validate({
            messages: {
                loginUsername: {
                    required: "Please enter your username."
                },
                loginPassword: {
                    required: "Please enter your password."
                }
            },
            errorClass: 'help-inline',
            highlight: function (label) {
                $(label).closest('.control-group').addClass('error');
            },
            success: function ($label) {
                $label.closest('.control-group').removeClass('error').addClass('success');
            }

        });
        $('#loginUsername').focus();
        $('#loginUsername').bind("focusout", function () {
            var fld = $('#loginUsername');
            fld.val($.trim(fld.val()))
        });
    };

    var _initRegister = function (timeout) {
        setTimeout(function () {
            alert("Your session has timed out, click ok to refresh the page.");
            location.reload(true);
        }, timeout);
        $('#contributor-id').click(function () {
            _switchContributorReasonDisplay($(this).is(':checked'));
        });
        var contributor = $("#contributor-id").is(':checked');
        switchContributorReasonDisplay(contributor);

        TDAR.common.initRegformValidation("#accountForm");

        $('#firstName').focus();

    };

    var _switchContributorReasonDisplay = function (shouldDisplay) {
        var opt = shouldDisplay ? 'show' : 'hide';
        $('#contributorReasonTextArea').collapse(opt);
        $('#contributorReasonId').attr("disabled", !shouldDisplay);
    };

    
    TDAR.auth = {
        "initLogin": _initLogin,
        "initRegister": _initRegister
    };

    
})(TDAR, jQuery);
