/**
 * email-specific functionality
 */
(function(TDAR, $) {
    "use strict";

    function _init() {
        $("#emailButton").click(function(e) {
            $("#email-form").modal('show');
            e.preventDefault();
        });

        $("#followup-cancel").click(function(e) {
            $("#messageBody").val('');
            $("#email-form").hide();
            e.preventDefault();
        });
        $("#followup-send").click(function(e) {

            var url = TDAR.uri( "/email/deliver"); // the script where you handle the form input.
            var $modal = $("#emailStatusModal");
            var $success = $(".success", $modal);
            var $error = $(".error", $modal);

            $.ajax({
                type : "POST",
                url : url,
                data : $("#followup").serialize(), // serializes the form's elements.
                success : function(data) {
                    $success.show();
                    $error.hide();
                    $modal.modal('show');
                    $("#messageBody").val('');
                    $("#followup").hide();
                },
                error : function(data) {
                    var json = data.responseJSON;
                    $success.hide();
                    $error.show();
                    var $errorContainer = $("#emailErrorContainer", $error).empty();
                    var msg = "Unknown Error";

                    if (json.errors != undefined && json.errors.actionErrors != undefined) {
                        msg = "";
                        var ae = json.errors.actionErrors;
                        $(ae).each(function(index) {
                            msg += "<li>" + ae[index] + "</li>";
                        });
                        $(msg).appendTo($errorContainer);
                    }
                    $modal.modal('show');
                }
            });
            
            e.preventDefault();
        });
        if (window.location.href.indexOf("showEmail") > -1) {
            $("#email-form").show();
        }
    }

    TDAR.internalEmailForm = {
        init : _init
    };

})(TDAR, jQuery);
