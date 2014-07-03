/**
 * email-specific functionality
 */
(function(TDAR, $) {
    "use strict";

    var _register = function (url, versionId) {
        TDAR.common.registerDownload(url, versionId);
    };

    var _autoDownload = function (url, versionId) {
        _register(url, versionId);
        document.location = url;
    };

    var _setup = function(url, versionId) {
        var DOWNLOAD_WAIT_SECONDS = 4;
        var id = setTimeout(_autoDownload(url, versionId), DOWNLOAD_WAIT_SECONDS * 1000);
    
        //cancel auto-download if user beats us to the clock
        $('.manual-download').click(function () {
            clearTimeout(id);
            _register(url, versionId);
            return true;
        });
    }

    TDAR.download = {
        setup : _setup
    };

})(TDAR, jQuery);
