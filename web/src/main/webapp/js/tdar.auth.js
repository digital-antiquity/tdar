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
            alert("Your session has timed out, please reload the page.");
//            location.reload(true);
        }, timeout);
        $('#contributor-id, #tou-id').click(function () {
            _switchContributorReasonDisplay($(this).is(':checked'));
        });
        var contributor = $("#contributor-id").is(':checked');
        var contributor2 = $("#tou-id").is(':checked');
        var c = false;
        if (contributor2 == true || contributor == true) {
            c = true;
        }
        console.log(contributor2, contributor, c);
        _switchContributorReasonDisplay(c);

        $('#firstName').focus();

    };

    var _switchContributorReasonDisplay = function (shouldDisplay) {
        var opt = "hidden";
        if (shouldDisplay) {
            opt = "show";
            $('#contributorReasonTextArea').removeClass("hidden");
        } else {
            $('#contributorReasonTextArea').addClass("hidden");
        }
        $('#contributorReasonId').attr("disabled", !shouldDisplay);
    };

    
    TDAR.auth = {
        "initLogin": _initLogin,
        "initRegister": _initRegister
    };

    
})(TDAR, jQuery);
