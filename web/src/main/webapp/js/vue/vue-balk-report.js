TDAR.vuejs.balkreport = (function(console, $, ctx, Vue) {
    "use strict";


    var _init = function(appId) {
        var app2 = new Vue({
            el : appId,
            data : {
                summary: undefined,
                 recent: undefined,
                 dateSearch: undefined,
                 accountId: undefined,
                 selectedAccount: undefined,             
                 accounts: []
            },
            watch: {
                accountId: function(after, before) {
                    this.loadData();
                    this.loadRecentData();
                },
                dateSearch: function(after, before) {
                    this.loadData();
                    this.loadRecentData();
                }
            },
            methods: {
                yesNo: function(date) {
                    if (date == undefined || this.dateSearch == undefined) {
                        return '';
                    }
                    if (new Date(this.dateSearch) < new Date(date)) {
                        return true;
                    }
                    return '';
                },
                loadData: function() {
                    var _app = this;
                    $.get("/api/file/reports/summary", {accountId: this.accountId, dateSearch: this.dateSearch}).done(function(sum) {
                        Vue.set(_app,"summary",sum);
                    });
                },
                loadRecentData: function(id) {
                    var _app = this;
                    $.get("/api/file/reports/recentFiles", {accountId: this.accountId, dateSearch: this.dateSearch}).done(function(sum) {
                        Vue.set(_app,"recent",sum);
                    });
                }
            },
            mounted: function() {
                Vue.set(this,"accountId", 220);
                Vue.set(this,"accounts", JSON.parse($("#accountJson").text()));
                var date = new Date();
                date.setDate(date.getDate() - 7);
                Vue.set(this, "dateSearch", (date.getMonth() + 1) + '/' + date.getDate() + '/' +  date.getFullYear().toString().substring(2));
                var _app = this;
                var picker = $("#dateSearch").datepicker({autoclose:true, format: "mm/dd/yyyy"}).on('changeDate', function(ev){
                    var $target = $(ev.target);
                    console.log('target', $target, $target.val());
                    Vue.set(_app,'dateSearch',$target.val());
                    $target.datepicker('hide');
                });

                
            }
       });

    return app2;
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
