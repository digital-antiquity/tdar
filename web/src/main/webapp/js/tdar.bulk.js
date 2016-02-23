(function (TDAR, $) {
    'use strict';
    var asyncUrl;
    var gPercentDone = 0;
    var TIMEOUT = 1000 / 2; //2fps is all we need.

    var _init = function($div) {
        asyncUrl = $div.data('asyncUrl');
        //stop pinging for info when the process is done
        gPercentDone = 0;

        $("#progressbar").progressbar({value: 0});

        //fixme: for testing purposes, call fqn of updateProgress instead of _updateProgress
        setTimeout(TDAR.bulk.updateProgress, TIMEOUT);

        return {asyncUrl: asyncUrl, percentDone: gPercentDone, timeout: TIMEOUT};
    };

    var _updateProgress = function () {
        //console.log("updating progress");
        if (gPercentDone >= 100) {
            //console.log("progress at 100. no need to continue");
            return;
        }
        ;

        $.ajax({
            url: asyncUrl,
            dataType: 'json',
            type: 'POST',
            success: function (data) {
                gPercentDone = data.percentDone;
                console.log("percent complete: %s", data.percentDone);
                if (data.percentDone != 100) {
                    $("#progressbar").progressbar("option", "value", data.percentDone);
                    $("#buildStatus").empty().append(data.phase);
                    setTimeout(_updateProgress, TIMEOUT);
                } else {
                    $("#progressbar").progressbar("option", "value", 100);
                    $('#divUploadComplete').show();
                    //$("#progressbar").progressbar("destroy");
                    $("#buildStatus").empty().append("Upload complete.");
                    $("#btnDashboard").button();
                }
                if (data.errors != undefined && data.errors != "") {
                    $("#asyncErrors").show().find("#errorDetails").html("<div class=''><ul>" + data.errors + "</ul></div>");
                    $("#progressbar").progressbar("disable");
                }
            },
            error: function (xhr, txtStatus, errorThrown) {
                gPercentDone = 101;
                console.error("error: %s, %s", txtStatus, errorThrown);
                $('#unspecifiedError, #asyncErrors').show();
                $("#progressbar").progressbar("disable");
            }
        });

        //console.log("registered ajax callback");
    };
    //expose public elements
    TDAR.bulk = {
        "updateProgress": _updateProgress,
        "init": _init
    };

})(TDAR, jQuery);