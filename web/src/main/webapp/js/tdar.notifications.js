/**
 * notification-specific functionality
 */

const core = require("./tdar.core.js");


function _init() {
    $("[data-dismiss-id]").each(function () {
        var $this = $(this);
        var id = $this.data('dismiss-id');
        $this.click(function() {_dismiss(id)});
    });
}

function _dismiss(id) {
        var url = core.uri( "/api/notification/dismiss"); // the script where you handle the form input.
        $.ajax({
            type : "POST",
            url : url,
            data : {"id":id}, // serializes the form's elements.
            success : function(data) {
                console.log("success, dismissed notification:" + id);
            },
            error : function(data) {
                var json = data.responseJSON;
                $("body").append(JSON.stringify(data));
                console.log(JSON.stringify(data))
                alert(json);
           }
        });
}

module.exports = {
    init : _init,
    dismiss:_dismiss
};
