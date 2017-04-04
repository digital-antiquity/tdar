
TDAR.manage = (function($, console, ctx) {
    'use strict';

    /**
     * Return a new DIV element representing a bootstrap 'button group' based on the provided list of buttons
     * @param btnList list of object containing a 'label' and 'value' field.
     * @returns {*|HTMLElement}  DIV element.
     * @private
     */
    function _buttonGroup(btnList) {
        var $div = $('<div class="btn-group" data-toggle="buttons-radio"></div>');
        btnList.forEach(function(item){
            $div.append('<button type="button" class="btn" value="' + item.value + '">' + item.label + '</button>')
        });
        return $div;

    };

    function _serializeRadioGroup($ctx) {
        var items = $ctx.find('input[type=radio],input[type=checkbox],option')
            .map(function(i, elem ) {
                var obj = {};
                obj.value = elem.value;
                obj.label = obj.value;
                if($(elem).parent().is('label')) {
                    obj.label = obj.parent().text();
                } else if($(elem).is('option') && $(elem).text().length > 0) {
                    obj.label = $(elem).text();
                }
                return obj;
            })
            .get();
        return items;
    }

    function _convertRadiosToButtons($container) {
        var items = _serializeRadioGroup($container);
        var $btnGroup = _buttonGroup(items);
        $container.find('input[type=checkbox],input[type=radio],select').remove();
        $container.append($btnGroup);
    }




    return {

        'init': function() {
            TDAR.notifications.init();
            TDAR.common.collectionTreeview();
            TDAR.autocomplete.applyCollectionAutocomplete($('#txtShareCollectionName'),
                {showCreate: false}, {permission: 'ADMINISTER_GROUP',collectionType:'LIST'});
            TDAR.autocomplete.applyResourceAutocomplete($('#txtShareResourceName'), '');
            TDAR.autocomplete.applyPersonAutoComplete($("#accessRightsRecords"), true, false);

            //_convertRadiosToButtons($('#divWhat'))

            $('#divWhat').find('select').on('change', function(evt) {
                var $option = $(evt.target.options[evt.target.selectedIndex]);
                $('#toggleControls').find('>div').hide();
                $($option.data('target')).show();

            }).change();



        },

        'convertRadiosToButtons': _convertRadiosToButtons




    }
})(jQuery, console, window);
