(function(TDAR, console) {
    "use strict";
    console.debug("tdar.pagination.js");

    TDAR.pagination = {};

    function PaginationHelper(recordsPerPage, idxFirst, totalRecords, resultsWindow) {
        this.recordsPerPage = recordsPerPage;
        this.idxFirst = idxFirst;
        this.totalRecords = totalRecords;
        this.resultsWindow = resultsWindow;
    }

    //helper functions related to pagination. indexes are 1-based.
    PaginationHelper.prototype = {
        currentPage: function() {
            if(!this.totalRecords) {return 1;}
            return Math.floor(this.idxFirst / this.recordsPerPage) + 1;
        },

        //fixme: not fully implemented
        hasNext: function() {
            if(!this.resultsWindow  && !this.totalRecords) return true;

            return false;

        },

        hasPrevious: function() {
            return this.currentPage() > 1;
        },

        advancePage: function() {
            this.idxFirst += this.recordsPerPage;
        }
    }

})(TDAR, console);

