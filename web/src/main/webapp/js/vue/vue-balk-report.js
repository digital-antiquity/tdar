TDAR.vuejs.balkreport = (function(console, $, ctx, Vue) {
    "use strict";

    var _init = function(appId) {

        var router = new VueRouter({
            routes : [ {
                path : '/:accountId(\\d+)',
                component : app2
            }, ]
        });

        var pp = new Vue({
            router : router
        }).$mount(appId);

        // we return everything for testing
        return {
            'router' : router,
            'app' : pp,
            'reports' : app2
        }
    }

    return {
        init : _init,
        main : function() {
            var appId = "#filesReports";
            if ($(appId).length == 1) {
                TDAR.vuejs.balkreport.init(appId);
            }
        }
    }

})(console, jQuery, window, Vue);
