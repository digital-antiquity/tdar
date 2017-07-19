/**
 * notification-specific functionality
 */
(function(TDAR, $) {
    "use strict";

    function _init() {
        $("[data-dismiss-id]").each(function () {
            var $this = $(this);
            var id = $this.data('dismiss-id');
            $this.click(function() {TDAR.notifications.dismiss(id)});
        });
    }
    
    function _dismiss(id) {
            var url = TDAR.uri( "/notification/dismiss"); // the script where you handle the form input.
            $.ajax({
                type : "POST",
                url : url,
                data : {"id":id}, // serializes the form's elements.
                success : function(data) {
                    console.log("success, dismissed notification:" + id);
                },
                error : function(data) {
                    var json = data.responseJSON;
                    alert(json);
               }
            });
    }

    TDAR.notifications = {
        init : _init,
        dismiss:_dismiss
    };

})(TDAR, jQuery);
