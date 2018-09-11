/***
 * tdar.moreinfo.js
 */
    var _init = function() {
        $(".moreInfo").each(function() {
            var $t = $(this);
            var type = $t.data("type");
            if (type == 'collection') {
                var description = $t.data("description");
                var size = $t.data("size");
                var submitter = $t.data("submitter");
                var submitterLink = $t.data("submitterLink");
                var hidden = $t.data("hidden");

                var html_ = "<p><b>Description:</b> "+description+"<br>";
                html_ += "<b># of Resources:</b> "+size+"<br>";
                    if (hidden) {html_ += "<b>Hidden:</b> "+hidden+"<br>";}
                    html_ += "<b>Submitter:</b> "+submitter+"</p>";

                $t.popover({html: true, content:html_,trigger:'hover'});
            }
        });
    }
    
    //expose public elements
    module.exports = {
        "init": _init,
        main : function() {
            TDAR.moreInfo.init();
        }
    };
