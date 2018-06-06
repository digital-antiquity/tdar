TDAR.vuejs.balkreport = (function(console, $, ctx, Vue) {
    "use strict";

    var _init = function(appId) {
        var app2 = Vue.component("reports",{
            template : '#reports-template',
            data : function() {
                return {
                    summary : undefined,
                    recent : undefined,
                    dateStart : undefined,
                    dateEnd : undefined,
                    accountId : undefined,
                    selectedAccount : undefined,
                    userId : undefined,
                    accounts : []
                }
            },
            watch : {
                accountId : function(after, before) {
                    this.loadData();
                    this.loadRecentData();
                    Vue.set(this, "userId", undefined);
                    router.push({ path: '/' + this.accountId })
                },
                dateStart : function(after, before) {
                    this.loadRecentData();
                },
                dateEnd : function(after, before) {
                    this.loadRecentData();
                },
                userId : function(after, before) {
                    this.loadRecentData();
                },
                '$route': function(to, from) {
                    console.log(to.params);
                    Vue.set(this, "accountId", to.params.accountId);
                    this.loadData();
                    this.loadRecentData();
                    Vue.set(this, "userId", undefined);
                }
            },
            methods : {
                loadData : function() {
                    if (this.accountId == undefined) {
                        return;
                    }
                    var _app = this;
                    $.get("/api/file/reports/summary", {
                        accountId : this.accountId
                    }).done(function(sum) {
                        Vue.set(_app, "summary", sum);
                    });
                },
                loadRecentData : function() {
                    if (this.accountId == undefined) {
                        return;
                    }
                    var _app = this;
                    $.get("/api/file/reports/recentFiles", {
                        accountId : this.accountId,
                        dateStart : this.dateStart,
                        dateEnd : this.dateEnd,
                        userId : this.userId
                    }).done(function(sum) {
                        Vue.set(_app, "recent", sum);
                    });
                }
            },
            mounted : function() {
                Vue.set(this, "accounts", JSON.parse($("#accountJson").text()));
                var date = new Date();
                date.setDate(date.getDate() - 7);
                Vue.set(this, "dateStart", (date.getMonth() + 1) + '/' + date.getDate() + '/' + date.getFullYear().toString());
                date = new Date();
                Vue.set(this, "dateEnd", (date.getMonth() + 1) + '/' + date.getDate() + '/' + date.getFullYear().toString());
                var _app = this;
                $('#reports a[data-toggle="tab"]').on('shown', function(e) {
                    var $picker = $(".placeholdered").datepicker({
                        autoclose : true,
                        format : "mm/dd/yyyy"
                    }).on('changeDate', function(ev) {
                        var $target = $(ev.target);
                        Vue.set(_app, $target.attr('id'), $target.val());
                        $target.datepicker('hide');
                    });
                    $picker.removeClass("placeholderd");
                });
            }
        });

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
