/**
 * Autocomplete Support
 */
//FIXME: put this in TDAR.autocomplete namespace, "hide" functions that don't need to be public.
function buildRequestData(element) {
    var data = {};
    //    console.log("autocompleteParentElement: " + element.attr("autocompleteParentElement"));
    if (element.attr("autocompleteParentElement")) {
        $("[autocompleteName]", element.attr("autocompleteParentElement")).each(function(index, elem) {
            var $elem = $(elem);
            data[$elem.attr("autocompleteName")] = $.trim($(elem).val());
            //                            console.log("autocompleteName: " + $val.attr("autocompleteName") + "==" + $val.val());
        });
    }
    //    console.log(data);
    return data;
}

function applyDataElements(element, item) {
    var $element = $(element);
    if ($element.attr("autocompleteParentElement") != undefined) {
        $("[autocompleteName]", $element.attr("autocompleteParentElement")).each(function(index, val) {
            var $val = $(val);
            var newvalue = item[$val.attr("autocompleteName")];
            if (newvalue != undefined) {
                var valueToSet = newvalue;
                if (newvalue.constructor === String) {

                } else {
                    if (newvalue['name'] != undefined) {
                        valueToSet = newvalue['name'];
                    }
                    if (newvalue['label'] != undefined) {
                        valueToSet = newvalue['label'];
                    }
                }

                $val.val(valueToSet);
                //                         console.log("setting: " + val.name +  "["+$val.attr("autocompleteName")+"]" + ":" + valueToSet);
                $val.attr("autoVal", valueToSet);
            }
        });
        if ($element.attr("autocompleteName") != undefined) {
            item.value = $element.attr("autoVal");
        }
    }

    //if id element defined,  set it's value
    if ($element.attr("autocompleteIdElement")) {
        var $idElement = $($element.attr("autocompleteIdElement"));

        if (item["id"] != undefined) {
            $idElement.val(item["id"]);
        }
    } else {
        //TODO:  confirm  $element.closest('.autocomplete-id-element') will work for all use cases. 
    }

}

function applyPersonAutoComplete($elements, usersOnly, showCreate) {
    var options = {};
    options.url = "lookup/person";
    options.dataPath = "data.people";
    options.retainInputValueOnSelect = true;
    options.sortField = 'CREATOR_NAME';
    options.showCreate = showCreate;
    options.minLength = 3;

    options.enhanceRequestData = function(requestData) {
        if (usersOnly) {
            requestData.registered = true;
        }
        // var lemail = (usersOnly) ? '' : getEmail(
        // this.element).val();
    };

    options.customRender = function(ul, item) {
        //        console.log(item);
        var institution = "";
        if (item.institution != undefined && item.institution.name != undefined) {
            institution = item.institution.name;
        }
        //double-encode on custom render
        var htmlSnippet = "<p style='min-height:4em'><img class='silhouette' src=\"" + getBaseURI() +
                "images/man_silhouette_clip_art_9510.jpg\" width=\"40\"/>" + "<span class='name'>" + htmlEncode(htmlEncode(item.properName)) + "(" +
                htmlEncode(htmlEncode(item.email)) + ")</span><br/><span class='institution'>" + htmlEncode(htmlEncode(institution)) + "</span></p>";
        if (item.id == -1 && options.showCreate) {
            htmlSnippet = "<p style='min-height:4em'><img class='silhouette' src=\"" + getURI("images/man_silhouette_clip_art_9510.jpg") + "\" width=\"40\"/>" +
                    "<span class='name'><em>Create a new person record</em></span> </p>";
        }
        return $("<li></li>").data("item.autocomplete", item).append("<a>" + htmlSnippet + "</a>").appendTo(ul);
    };
    applyGenericAutocomplete($elements, options);
}

function evaluateAutocompleteRowAsEmpty(element, minCount) {
    var req = buildRequestData($(element));
    var total = 0;
    //FIXME:  I think 'ignored' is irrelevant as defined here.  Can we remove this?
    var ignored = new Array();
    if (minCount != undefined) {
        ignored = minCount;
    }

    var $idElement = $($(element).attr("autocompleteIdElement"));
    var allowNew = $idElement.attr("allowNew");

    var nonempty = 0;
    // for each item in the request
    for ( var p in req) {
        total++;
        if ($.inArray(p, ignored) == -1 && req[p] != undefined && req[p] != '') {
            nonempty++;
        }
        if (p == "id") {
            nonempty++;
        }
    }
    //    console.log("req size:" + total + " nonEmpty:" + nonempty + " ignored:" + ignored);
    if (nonempty == 0) {
        return true;
    }

    if (allowNew != undefined && allowNew == "true" && ($idElement.val() == "" || $idElement.val() == -1)) {
        return true;
    }

    return false;
}

function applyGenericAutocomplete($elements, options) {

    // if there's a change in the autocomplete, reset the ID to ""
    $elements.change(function() {
        var $element = $(this);
        //TODO: add comment describing what on earth is the mission of this gargantuan IF statement??????????x10^5000
        if (($element.attr("autoVal") != undefined && $element.attr("autoVal") != $element.val()) ||
                evaluateAutocompleteRowAsEmpty(this, options.ignoreRequestOptionsWhenEvaluatingEmptyRow == undefined ? []
                        : options.ignoreRequestOptionsWhenEvaluatingEmptyRow)) {
            if ($element.attr("autocompleteIdElement")) {
                var $idElement = $($element.attr("autocompleteIdElement"));
                $idElement.val("");

            } else {
                //TODO:  confirm  $element.closest('.autocomplete-id-element') will work for all use cases. 
            }
        }
        return true;
    });

    //set allowNew attribute for each element's corresponding 'id' element
    $elements.each(function() {

        if (options.showCreate) {
            var $idElement = $($(this).attr("autocompleteIdElement"));
            $idElement.attr("allowNew", "true");
        }
    });

    //register the autocomplete for each element
    var autoResult = $elements.autocomplete({
        source : function(request, response) {
            var $elem = $(this.element);

            //is another ajax request in flight?
            var oldResponseHolder = $elem.data('responseHolder');
            if (oldResponseHolder) {
                //cancel the previous search
                //                        console.log("cancelling previous search");
                oldResponseHolder.callback({});

                //swap out the no-op before the xhrhhtp.success callback calls it
                oldResponseHolder.callback = function() {
                    //                            console.log("an ajax success callback just called a now-defunct response callback");
                };
            }

            var requestData = {};
            // add requestData that's passed from the
            // options
            if (options.requestData != undefined) {
                $.extend(requestData, options.requestData);
            }

            // add the sortField
            if (options.sortField != undefined) {
                requestData.sortField = options.sortField;
            }

            // hard-code map for term
            if (request.term != undefined) {
                requestData.term = request.term;
            }
            // more generic map for any form based
            // autocomplete elements
            $.extend(requestData, buildRequestData(this.element));

            // final callback for using custom method
            if (options.enhanceRequestData != undefined) {
                options.enhanceRequestData(requestData, request);
            }

            //add a closure to ajax request that wraps the response callback. This way we can swap it out for a no-op if a new source() request
            //happens before the existing is complete.
            var responseHolder = {};
            responseHolder.callback = response;
            $elem.data('responseHolder', responseHolder);

            var ajaxRequest = {
                url : getBaseURI() + options.url,
                dataType : "jsonp",
                data : requestData,
                success : function(data) {
                    if (!$elem.is(':focus')) {
                        console.debug("input blurred before autocomplete results returned. returning no elements");
                        responseHolder.callback({});
                        return;
                    }
                    // if there's a custom dataMap function, use that, otherwise not
                    if (options.customDisplayMap == undefined) {
                        options.customDisplayMap = function(item) {
                            if (item.name != undefined && options.dataPath != 'person') {
                                // there is no need to escape this because we're rendering as plain text
                                item.label = item.name;
                            }

                            return item;
                        };
                    }
                    var values = $.map(eval(options.dataPath), options.customDisplayMap);
                    // show create function

                    // enable custom data to be pushed onto values
                    if (options.addCustomValuesToReturn != undefined) {
                        var extraValues = options.addCustomValuesToReturn(options, requestData);
                        // could be push, need to test
                        values = values.concat(extraValues);
                    }
                    console.log(options.dataPath + " autocomplete returned " + values.length);

                    if (options.showCreate != undefined && options.showCreate == true) {
                        var createRow = buildRequestData($elem);
                        createRow.value = request.term;
                        // allow for custom phrasing
                        if (options.showCreatePhrase != undefined) {
                            createRow.label = "(" + options.showCreatePhrase + ": " + request.term + ")";
                        }
                        createRow.id = -1;
                        values.push(createRow);
                    }
                    responseHolder.callback(values);
                },
                complete : function() {

                    $elem.removeData('responseHolder');
                    //                            console.log("clearing responseholder. this should be blank -->" + $elem.data('reponseHolder') );
                }
            };
            $.ajax(ajaxRequest);
        },
        minLength : options.minLength || 0,
        select : function(event, ui) {
            // 'this' is the input box element.
            $elem = $(this);
            applyDataElements(this, ui.item);

            //cancel any pending searches once the user selects an item
            var responseHolder = $elem.data('responseHolder');
            if (responseHolder) {
                responseHolder.callback();
                responseHolder.callback = function() {
                };
            }
        },
        open : function() {
            $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
            if (options.customRender != undefined) {
                $("ul.ui-autocomplete li a").each(function() {
                    var htmlString = $(this).html().replace(/&lt;/g, '<');
                    htmlString = htmlString.replace(/&gt;/g, '>');
                    $(this).html(htmlString);
                });
            }
            $("ul.ui-autocomplete").css("width", $(this).parent().width());
        },
        close : function() {
            $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
        }
    });
    if (options.customRender != undefined) {
        autoResult.each(function(idx, elem) {
            // handle custom rendering of result
            $(elem).data("autocomplete")._renderItem = options.customRender;
        });
    }
};

function applyKeywordAutocomplete(selector, lookupType, extraData, newOption) {
    var options = {};
    options.url = "lookup/" + lookupType;
    options.enhanceRequestData = function(requestData) {
        $.extend(requestData, extraData);
    };

    options.dataPath = "data.items";
    options.sortField = 'LABEL';
    options.showCreate = newOption;
    options.showCreatePhrase = "Create a new keyword";
    options.minLength = 2;
    applyGenericAutocomplete($(selector), options);
}

function applyCollectionAutocomplete($elements, options, extraData) {
    var _options = {};
    if (typeof options === "object") {
        _options = options;
    }
    var defaults = {};
    options.enhanceRequestData = function(requestData) {
        $.extend(requestData, extraData);
    };

    defaults.url = "lookup/collection";
    defaults.dataPath = "data.collections";
    defaults.sortField = 'TITLE';
    defaults.showCreate = false;
    if (defaults.showCreate) {
        defaults.showCreatePhrase = "Create a new collection";
    }
    defaults.minLength = 2;
    applyGenericAutocomplete($elements, $.extend({}, defaults, _options));
}

function displayResourceAutocomplete(item) {
    var label = "";
    if (item.name) {
        label = item.name;
    }
    if (item.title) {
        label = item.title;
    }
    item.value = label + " (" + item.id + ") ";
    return item;
}

function applyResourceAutocomplete($elements, type) {
    var options = {};
    options.url = "lookup/resource";
    options.dataPath = "data.resources";
    options.sortField = 'TITLE';
    options.enhanceRequestData = function(requestData) {
        if (requestData["subCategoryId"] != undefined && requestData["subCategoryId"] != '' && requestData["subCategoryId"] != -1) {
            requestData["sortCategoryId"] = requestData["subCategoryId"];
        }
        requestData.resourceTypes = type;
    };
    options.ignoreRequestOptionsWhenEvaluatingEmptyRow = [ "subCategoryId", "sortCategoryId" ];
    options.minLength = 0;
    options.customDisplayMap = displayResourceAutocomplete;
    options.customRender = function(ul, item) {
        var description = "";
        //            console.log(item);
        if (item.description != undefined) {
            description = item.description;
        }
        var link = "";
        if (item.urlNamespace) {
            // link = "<b onClick=\"openWindow('"+ getBaseURI() +
            // item.urlNamespace + "/view/" + item.id +"\')\">view</b>";
        }
        //double-encode on custom render
        return $("<li></li>").data("item.autocomplete", item).append(
                "<a  title=\"" + htmlDecode(description) + "\">" + htmlEncode(htmlEncode(item.value)) + link + "</a>").appendTo(ul);
    };

    applyGenericAutocomplete($elements, options);
    //    var lookupUrl = getURI("lookup/resource");

}

function applyInstitutionAutocomplete($elements, newOption) {

    var options = {};
    options.url = "lookup/institution";
    options.dataPath = "data.institutions";
    options.sortField = 'CREATOR_NAME';
    options.enhanceRequestData = function(requestData) {
        requestData.institution = requestData.term;
    };
    options.showCreate = newOption;
    options.minLength = 2;
    options.showCreatePhrase = "Create new institution";
    applyGenericAutocomplete($elements, options);
};

function autocompleteShowAll() {
    $(this).siblings('input[type=text]').focus().autocomplete("search", "");
}
