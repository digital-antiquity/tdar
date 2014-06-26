(function(TDAR, $) {
    'use strict';

    var en_US = {
        "integration.missing_table" : "at least one table does not have any filter values checked",
        "integration.select_variable":"please select at least one variable",
        "integration.no_shared_vars":"no shared integration columns were found.",
        "test.test": "TEST 123"
    };
    
    var defaultLocale = en_US;

    function _getKey(key) {
        return defaultLocale[key];
    };


    TDAR.locale = {
        getKey : _getKey
    };

})(TDAR, jQuery);
