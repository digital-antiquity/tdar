/**
 * TDAR.bulk.js
 */
    //const TDAR = require("./tdar.master");

    var asyncUrl;
    var gPercentDone = 0;
    var TIMEOUT = 1000 / 2; //2fps is all we need.

    var _init = function($div) {
        asyncUrl = $div.data('asyncUrl');
        //stop pinging for info when the process is done
        gPercentDone = 0;

        _updateProgressBar(0);
        //fixme: for testing purposes, call fqn of updateProgress instead of _updateProgress
        setTimeout(window.TDAR.bulk.updateProgress, TIMEOUT);
        return {asyncUrl: asyncUrl, percentDone: gPercentDone, timeout: TIMEOUT};
    };

    var _updateProgressBar = function(valeur) {
        $(".progress-bar").attr('aria-valuenow', valeur);
        $(".progress-bar").html(valeur + "%");
        $(".progress-bar").css("width",valeur+'%');
    }
    
    var _updateProgress = function () {
//        console.log("updating progress...");
        if (gPercentDone >= 100) {
            //console.log("progress at 100. no need to continue");
            return;
        }

        $.ajax({
            url: asyncUrl,
            dataType: 'json',
            type: 'POST',
            success: function (data) {
                gPercentDone = data.percentComplete;
                console.log("percent complete: %s", data.percentComplete);
                if (data.percentDone != 100) {
                    _updateProgressBar(data.percentComplete);
                    $("#buildStatus").empty().append(data.message);
                    setTimeout(window.TDAR.bulk.updateProgress, TIMEOUT);
                } else {
                    _updateProgressBar(100);
                    $('#divUploadComplete').show();
                    $("#buildStatus").empty().append("Upload complete.");
                    $("#btnDashboard").button();
                }
                if (data.errors != undefined && data.errors != "") {
                    $("#asyncErrors").show().find("#errorDetails").html("<div class=''><ul>" + data.errors + "</ul></div>");
                }
            },
            error: function (xhr, txtStatus, errorThrown) {
                gPercentDone = 101;
                console.error("error: %s, %s", txtStatus, errorThrown);
                $('#unspecifiedError, #asyncErrors').show();
            }
        });

        //console.log("registered ajax callback");
    };
    //expose public elements
    module.exports = {
        "updateProgress": _updateProgress,
        "init": _init
    };
