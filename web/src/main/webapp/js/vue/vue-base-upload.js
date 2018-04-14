TDAR.vuejs.upload = (function(console, $, ctx, Vue) {
    "use strict";
    
    var __matching = function(arr1, arr2, name) {
        var ret = new Array();
        arr1.forEach(function(p1) {
            arr2.forEach(function(p2) {
                // console.log(p1[name], p2[name]);
                if (p1[name] == p2[name]) {
                    ret.push([ p1, p2 ]);
                }
            });
        });
        return ret;
    }

    var _setProgress = function(progress) {
        $('#progress .progress-bar').css('width', progress + '%');
        if (progress == 100) {
            $("#uploadstatus").html("Complete");
        } else if (progress < 1) {
            $("#uploadstatus").html("");
        } else {
            $("#uploadstatus").html("Uploading...");            
        }


    }

    
    return {
        _matching : __matching,
        setProgress: _setProgress
    }

})(console, jQuery, window, Vue);
