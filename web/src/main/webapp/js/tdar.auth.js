(function (TDAR, $) {
    'use strict';

    var _initLogin = function () {
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
        _switchContributorReasonDisplay(contributor);

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
