
TDAR.manage = (function($, console) {
    'use strict';

    return {
        'main': function() {
            console.log('hello from tdar.manage:main');
        },

        'init': function() {
            TDAR.notifications.init();
            TDAR.common.collectionTreeview();
            TDAR.autocomplete.applyCollectionAutocomplete($('#txtShareCollectionName'),
                {showCreate: false}, {permission: 'ADMINISTER_GROUP',collectionType:'LIST'});
            TDAR.autocomplete.applyResourceAutocomplete($('#txtShareResourceName'), '');
            TDAR.autocomplete.applyPersonAutoComplete($("#txtShareEmail"), true, false);


        }

    }
})(jQuery, console, TDAR);
