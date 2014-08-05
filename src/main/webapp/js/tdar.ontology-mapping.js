(function (TDAR, $, ctx) {
    'use strict';

    var _initMapping = function () {
        $("#autosuggest").click(_autosuggest);
        $("#clearAll").click(_clearall);
        $("#mapontologyform").FormNavigate({message: "Leaving the page will cause any unsaved data to be lost!"});
        $("#selectColumn").unbind("change");
        $('button.ui-button').hover(function () {
            $(this).addClass("ui-state-hover");
        }, function () {
            $(this).removeClass("ui-state-hover");
        });

        $(".show-all").click(function () {
            var $button = $(this);
            var $div = $button.closest('.input-append');
            var $textfield = $div.find("input[type=text]");
            var $widget = $textfield.autocomplete("widget");
            $textfield.focus().autocomplete("search", "");
        });

        for (var i = 0; i < $(".mappingPair").length; i++) {
            _applyLocalAutoComplete($("#autocomp_" + i), ctx["autocomp_" + i + "Suggestions"]);
        }
    };

    function _applyLocalAutoComplete(selector, db) {

        $(selector).autocomplete({
            source: function (request, response) {
                //var timer = new Timer();
                var matcher = new RegExp($.ui.autocomplete.escapeRegex(request.term), "i");
                var allMatchedItems = $.merge($.map(db, function (item) {
                    if (matcher.test(item.name)) {
                        return {
                            value: item.name,
                            label: item.name,
                            id: item.id
                        };
                    }
                }), $.map(ontology, function (item) {
                    if (matcher.test(item.name)) {
                        return {
                            value: item.name.replace(/^([\|\-\s])*/ig, ""),
                            label: item.name,
                            id: item.id
                        };
                    }
                }));
                //console.log("%s\t autocomplete.source::\t suggestiondb.size:%s\t ontology.size:%s", timer.current(), db.length, ontology.length);
                response(allMatchedItems);
                //timer.stop();
            },
            minLength: 0,
            select: function (event, ui) {
                var $input = $(this); //'this' points to the target element 
                //get the hidden input next to the textbox and set the id field
                var $idElement = $($input.attr("autocompleteIdElement"));
                $idElement.val(ui.item.id);
                $input.removeClass("error");
            },
            open: function (event, ui) {
                $("ul.ui-autocomplete").css("width", $(this).parent().width());
            },
            change: function (event, ui) {
            }
        });

        //not to be confused with autocomplete 'change' option,  which is actually a custom 'autocompletechange' event
        //we assume this fires only when you change the textbox and not when via selecting an item from the autocomplete list
        $(selector).change(function () {

            var input = this; //'this' is the text input element
            var $input = $(input);

            //if the textbox is blank,  clear the hidden id field
            if ($.trim(input.value) == '') {
                var $idElement = $($input.attr("autocompleteIdElement"));
                $idElement.val("");
                return;
            }

            //don't allow invalid selection
            if (true) {
                //did they type in an exact match to an elment?
                var matcher = new RegExp("^([\\|\\-\\s]|&nbsp;)*" + $.ui.autocomplete.escapeRegex(input.value) + "$", "i");
                var valid = false;

                //troll through onto ontologies until we find one that matches the value of the input element
                $.each(ontology, function (k, v) {
                    if (v.name.match(matcher)) {
                        valid = true;
                        var $idElement = $($input.attr("autocompleteIdElement"));
                        $idElement.val(v.id);
                        return false;
                    }
                });

                if (!valid) {
                    console.debug("invalid entry - clearing input box and hidden id value");
                    var $idElement = $($input.attr("autocompleteIdElement"));
                    $idElement.val("");
                    $input.addClass("error");
                }

            }
        });

    }

    function _autosuggest() {
        $(".manualAutocomplete").each(function () {
            var $element = $(this);
            var json = eval($element.attr("id") + "Suggestions");
            if (json.length == 3 && $element.val() == "") {
                var $idElement = $($element.attr("autocompleteIdElement"));
                $element.val(json[2].name);
                $idElement.val(json[2].id);
            }

        });
    }

    function _clearall() {
        $(".manualAutocomplete").each(function () {
            var $element = $(this);
            var $idElement = $($element.attr("autocompleteIdElement"));
            $element.val("");
            $idElement.val("");
        });
    }

    //expose public elements
    TDAR.ontologyMapping = {
        "initMapping": _initMapping,
        "clearAll": _clearall,
        "autoSuggest": _autosuggest
    };

})(TDAR, jQuery, window);