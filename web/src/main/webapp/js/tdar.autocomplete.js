/**
 * Autocomplete Support
 */


TDAR.autocomplete = {};
TDAR.autocomplete = (function () {
    "use strict";

    //when a user creates a record manually instead of choosing a menu-item from the autocomplete dropdown, this module
    //stores the record in the object cache.  If the user later fills out similar autocomplete fields,   we add
    //these cached records to the autocomplete dropdown.  This allows the user to save some time in situations where
    //a new value may appear several times on a form (e.g. a new person record  that should be listed as an 'author',
    // 'editor', and 'contact'.
    var _caches = {};

    /**
     * Autocomplete "object cache".  Some autocomplete fields allow for the adhoc creation of new values.  When the user
     * creates new adhoc records they are stored in the object cache.   If the user performs subsequent autocomplete
     * lookups, the results of the server lookup are combined with a search of the records of the object cache and
     * any matching records in the cache are included in the autocomplete results list.  This makes data entry
     * easier for situations where a user intends make repeated references to an adhoc record in a form.
     *
     *
     * @param acOptions
     *  url: the url used by the ajax lookup - used to filter which adhoc records are returned by a search
     *  objectMapper:  function used to map an a set of form fields to record object
     *
     * @constructor
     */
    function ObjectCache(acOptions) {
        this.acOptions = acOptions;
        this.namespace = acOptions.url || "root";
        this.parentMap = {};
        this.objectMapper = acOptions.objectMapper || _objectFromAutocompleteParent;

        //_caches[this.namespace] = this;
    };

    ObjectCache.prototype = {

        //register the fields inside this parent as an 'extra record'. When caller invokes getValues(), this class
        //will generate records based for all the registeredRecords
        register: function (parentElem) {
            var self = this, parentId = parentElem.id;
            //prevent dupe registration
            if ($(parentElem).hasClass("autocomplete-new-record")) {
                return;
            }

            $(parentElem).closest(".repeat-row").bind("repeatrowbeforedelete", function (e) {
                self.unregister(parentId);
                _enable($(parentElem));
            });

            this.parentMap[parentId] = parentElem;

            $(parentElem).addClass("autocomplete-new-record");

            //if user removes the row then unregister the associated record
            $(parentElem).bind("remove", function () {
                self.unregister(parentId);
            });
        },

        unregister: function (parentId) {
            delete this.parentMap[parentId];
        },

        getValues: function () {
            //var keys = Object.keys(this.parentMap).sort();
            var values = [];
            for (var parentId in this.parentMap) {
                var elem = this.parentMap[parentId];
                values.push(this.objectMapper(elem));
            }
            return values;
        },

        //by default search does nothing
        search: function _noop(){return []}
    };

    /**
     * return subset of getValues() for any partial matches of term in object[key].  If key not supplied,  search all fields in each object for a partial match
     * @param term search term
     * @param key name of the property to compare to the search term. if no key supplied, this function
     *      evaluates all fields properties
     * @returns {*} array of objects that have partial matches to the specified term and key
     */
    ObjectCache.basicSearch = function (term, key) {
        var values = this.getValues();
        var ret = $.grep(values, function (obj) {
            var keys = Object.keys(obj);
            if (key) {
                keys = [key];
            }
            for (var i = 0; i < keys.length; i++) {
                var val = obj[keys[i]];
                if (val) {
                    if (val.toLowerCase().indexOf(term) > -1) {
                        return true;
                    }
                }
            }
        });
        return ret;
    };

    /**
     * grab cache for specified url or create one
     * @param options options object used in objectCache constructor
     * @returns {ObjectCache}
     * @private
     */
    function _getCache(options) {
        if (!_caches[options.url]) {
            _caches[options.url] = new ObjectCache(options);
        }
        return _caches[options.url];
    }

    /**
     * construct a jsobject to be used as the 'data' parameter used for the autocomplete ajax request
     * @param element  any element contained by an "autocomplete parent"
     * @returns {{}} jsobject
     * @private
     */
    function _buildRequestData(element) {
        var data = {};

        //    console.log("autocompleteParentElement: " + element.attr("autocompleteParentElement"));
        if (element.attr("autocompleteParentElement")) {
            $("[autocompleteName]", element.attr("autocompleteParentElement")).each(function (index, elem) {
                var $elem = $(elem);
                data[$elem.attr("autocompleteName")] = $.trim($(elem).val());
                //                            console.log("autocompleteName: " + $val.attr("autocompleteName") + "==" + $val.val());
            });
        }
        return data;
    }

    /**
     * translate item property values to form fiends contained in a autocompleteParentElement
     * @param element any .ui-autocomplete-input field contained by the autocompleteParent element
     * @param item the source object. the function copies the item property values to the input fields under the parent
     * @private
     */
    function _applyDataElements(element, item) {
        var $element = $(element);
        if ($element.attr("autocompleteParentElement") != undefined) {
            $("[autocompleteName]", $element.attr("autocompleteParentElement")).each(function (index, val) {
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

    /**
     * Determines if the fields representing an autocomplete control should be interpreted as "empty". For example,
     * if all of the fields in a person autocomplete fieldset have empty value attributes, this function would return true.
     *
     * @param element any child element contained by an autocomplete parent
     * @param ignoredFields property names to exclude when evaluating which properties are non-blank
     * @returns {boolean} true if the function determines the autcomplete control to be "empty"
     */
    function _evaluateAutocompleteRowAsEmpty(element, ignoredFields) {
        var req = _buildRequestData($(element));
        var total = 0;
        var _ignoredFields = [];
        if (ignoredFields != undefined) {
            _ignoredFields = ignoredFields;
        }

        var $idElement = $($(element).attr("autocompleteIdElement"));
        var allowNew = $idElement.attr("allowNew");

        var nonempty = 0;
        // for each item in the request
        for (var p in req) {
            total++;
            if ($.inArray(p, _ignoredFields) == -1 && req[p] != undefined && req[p] != '') {
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

    /**
     * if user tabs away from autocomplete field instead of selecting valid menu item,  register as new record
     * @param objectCache objectCache that will hold the new record
     * @param elem any child element contained by an autocomplete parent element
     * @private
     */
    function _registerOnBlur(objectCache, elem) {
        var parentid = $(elem).attr("autocompleteparentelement");
        var $parentElem = $(parentid);
        var $hidden = $parentElem.find("input[type=hidden]").first();

        $parentElem.find(".ui-autocomplete-input").last().one("blur", function () {
            var hiddenVal = $hidden.val();
            if ((hiddenVal === "" || hiddenVal === "-1") && this.value !== "") {
                objectCache.register($parentElem.get());
            }
        });
    }

    /**
     * disable all the child form elements of the specified element
     * @param $parent
     * @private
     */
    function _disable($parent) {
        //fixme: commenting out for now. disable() must be called on actual autocomplete, not merely a parent of one.
        ////$parent.find(".ui-autocomplete-input").autocomplete("disable");
        //$parent.find(".ui-autocomplete-input").each(function () {
        //    $(this).data("autocomplete").disabled = true;
        //});
    }

    /**
     * enable child elements underneath parent
     * @param $parent
     * @private
     */
    function _enable($parent) {
        //$parent.autocomplete("enable");
    }

    /**
     * Register a selection of elements as autocomplete fields.
     *
     * @param $elements jquery selection of input fields to initialize as autocomplete fields.
     * @param opts initialization options:
     *          addCustomValuesToReturn: function(term){}  callback used to add additional items  to the end of the
     *                                          ajax autocomplete results (default: add objectCach search results),
     *          requestData: {} additional name/val request data to include in ajax request,
     *          url:  url for ajax request,
     *          showCreate: boolean.  if true,  allow user to create a new record by specifying value that cannot be
     *                              found on the server.
     *
     *
     */
    function _applyGenericAutocomplete($elements, opts) {
        var options = $.extend({

            //callback function that returns list of extra items to include in dropdown: function(options, requestData)
            addCustomValuesToReturn: function (term) {
                return cache.search(term);
            }
        }, opts);

        var cache = _getCache(options);

        //set allowNew attribute for each element's corresponding 'id' element
        $elements.each(function () {
            if (options.showCreate) {
                var $idElement = $($(this).attr("autocompleteIdElement"));
                $idElement.attr("allowNew", "true");
            }
        });

        //register the autocomplete for each element
        var autoResult = $elements.autocomplete({
            source: function (request, response) {
                var $elem = $(this.element);

                //is another ajax request in flight?
                var oldResponseHolder = $elem.data('responseHolder');
                if (oldResponseHolder) {
                    //cancel the previous search
                    //                        console.log("cancelling previous search");
                    oldResponseHolder.callback({});

                    //swap out the no-op before the xhrhhtp.success callback calls it
                    oldResponseHolder.callback = function () {
                        //                            console.log("an ajax success callback just called a now-defunct response callback");
                    };
                }

                // add requestData that's passed from the options
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
                $.extend(requestData, _buildRequestData(this.element));

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
                    url: TDAR.uri() + options.url,
                    dataType: "jsonp",
                    data: requestData,
                    success: function (data) {
                        if (!$elem.is(':focus')) {
                            console.debug("input blurred before autocomplete results returned. returning no elements");
                            responseHolder.callback({});
                            return;
                        }

                        // if there's a custom dataMap function, use that, otherwise not
                        if (options.customDisplayMap == undefined) {
                            options.customDisplayMap = function (item) {
                                if (item.name != undefined && options.dataPath != 'person') {
                                    // there is no need to escape this because we're rendering as plain text
                                    item.label = item.name;
                                }
                                return item;
                            };
                        }

                        //tdar lookup returns an object that wraps the results - the property with the results is specified by options.dataPath
                        var dataItems = typeof options.dataPath === "function" ? options.dataPath(data) : data[options.dataPath];
                        var values = $.map(dataItems, options.customDisplayMap);

                        // enable custom data to be pushed onto values
                        if (options.addCustomValuesToReturn) {
                            var extraValues = options.addCustomValuesToReturn(request.term);
                            // could be push, need to test
                            if (extraValues) {
                                values = values.concat(extraValues);
                            }
                        }
                        // console.log(options.dataPath + " autocomplete returned " + values.length);

                        if (options.showCreate) {
                            var createRow = _buildRequestData($elem);
                            createRow.value = request.term;
                            // allow for custom phrasing
                            if (options.showCreatePhrase) {
                                createRow.label = "(" + options.showCreatePhrase + ": " + request.term + ")";
                            }
                            createRow.id = -1;
                            createRow.isNewItem = true;
                            createRow.showCreate = true;
                            values.push(createRow);
                        }
                        responseHolder.callback(values);
                    },
                    complete: function () {
                        $elem.removeData('responseHolder');
                    }
                };
                $.ajax(ajaxRequest);
            },
            minLength: options.minLength || 0,

            change: function(event, ui) {
                var $element = $(this);
                // if the existing autocomplete value stored in the "autoVal" attribute does is not undefined and is not the same as the current
                // evaluate it for being significant (important when trying to figure out if a minimum set of fields have been filled in
                var autovalChanged = $element.attr("autoVal") !== $element.val();
                var rowIsEmpty = _evaluateAutocompleteRowAsEmpty(this, options.ignoreRequestOptionsWhenEvaluatingEmptyRow == undefined ? [] : options.ignoreRequestOptionsWhenEvaluatingEmptyRow);
                if (autovalChanged | rowIsEmpty) {
                    if ($element.attr("autocompleteIdElement")) {
                        var $idElement = $($element.attr("autocompleteIdElement"));
                        $idElement.val("");
                    }
                }
                return true;
            },

            select: function (event, ui) {
                var $elem = $(event.target);
                $elem.data('autocompleteSelectedItem', ui.item.value);
                _applyDataElements(this, ui.item);

                //cancel any pending searches once the user selects an item
                var responseHolder = $elem.data('responseHolder');
                if (responseHolder) {
                    responseHolder.callback();
                    responseHolder.callback = function () {
                    };
                }

                //if user selects 'create new' option, add it to the new item cache and stop trying to find matches.
                if (ui.item.isNewItem) {
                    var $parent = $($elem.attr("autocompleteparentelement"));
                    //$parent.autocomplete("disable");

                }
            },
            open: function () {
                $(this).removeClass("ui-corner-all").addClass("ui-corner-top");
                if (options.customRender != undefined) {
                    $("ul.ui-autocomplete li a").each(function () {
                        var htmlString = $(this).html().replace(/&lt;/g, '<');
                        htmlString = htmlString.replace(/&gt;/g, '>');
                        $(this).html(htmlString);
                    });
                }
                $("ul.ui-autocomplete").css("width", $(this).parent().width());
            },
            close: function () {
                $(this).removeClass("ui-corner-top").addClass("ui-corner-all");
            }
        });
        if (options.customRender != undefined) {
            autoResult.each(function (idx, elem) {
                // handle custom rendering of result
                $(elem).autocomplete("instance")._renderItem = options.customRender;
            });
        }

        $elements.filter("[autocompleteparentelement]").each(function () {
            _registerOnBlur(cache, this);
        });

        //if autocomplete is in a repeatable, register if they clicked 'addnew' after filling out a transient record
        var repeatables = [];
        $elements.closest(".repeatLastRow").filter(function () {
            if (repeatables.indexOf(this) > -1) {
                return false;
            }
            repeatables.push(this);
            return true;
        });
        $(repeatables).on("repeatrowadded", function (e, parentElem, cloneElem, idxOfNewRow, originalElem) {
            $(originalElem).find("[autocompleteparentelement]").filter(":visible").each(function () {
                var elem = this;
                var parentid = $(elem).attr("autocompleteparentelement");
                var $parentElem = $(parentid);
                var id = $parentElem.find("input[type=hidden]").first().val();
                if (id === "-1") {
                    cache.register($parentElem[0]);
                }
            });
        });

    };

    /**
     * initialize selection of elements as autocomplete fields
     *
     * @param selector jquery selection of text inputs to become autocomplete fields
     * @param lookupType type of keywords to be searched (e.g. material keywords, geographic keywords, etc)
     * @param extraData additional name/value data
     * @param newOption boolean true if users are allowed to create new keyword values
     */
    function _applyKeywordAutocomplete(selector, lookupType, extraData, newOption) {
        var options = {};
        options.url = "lookup/" + lookupType;
        options.enhanceRequestData = function (requestData) {
            $.extend(requestData, extraData);
        };

        options.dataPath = "items";
        options.sortField = 'LABEL';
        options.showCreate = newOption;
        options.showCreatePhrase = "Create a new keyword";
        options.minLength = 2;
        _applyGenericAutocomplete($(selector), options);
    }

    /**
     * initialize person autocomplete elements
     *
     * @param $elements jquery selection of elements to initialize as autocomplete fields
     * @param usersOnly if true, limit results to registered tDAR users.
     * @param showCreate if true, allow for the creation of new person records if the specified person
     *              cannot be found on the server
     */
    function _applyPersonAutoComplete($elements, usersOnly, showCreate) {
        var options = {
            url: "lookup/person",
            dataPath: "people",
            retainInputValueOnSelect: true,
            showCreate: showCreate,
            minLength: 3,
            customRender: function (ul, item) {
                var obj = $.extend({}, item);
                obj.addnew = (item.id == -1 && options.showCreate);
                var $snippet = $(tmpl("template-person-autocomplete-li", obj));
                $snippet.data("item.autocomplete", item).appendTo(ul);
                return $snippet;
            },
            requestData: {
                registered: usersOnly
            },
            objectMapper: function (parentElem) {
                var obj = _objectFromAutocompleteParent(parentElem)
                obj.properName = obj.firstName + " " + obj.lastName;
                return obj;
            }
        };

        _applyGenericAutocomplete($elements, options);
        _getCache(options).search = ObjectCache.basicSearch;
    }

    /**
     * Initialize ResourceCollection autocomplete fields.
     *
     * @param $elements jquery selection of text inputs to initialize as autocomplete fields
     * @param options additional initialization optionss (see applyGenericAutocomplete for more info)
     * @param extraData additional name/val request data to include in ajax request
     */
    function _applyCollectionAutocomplete($elements, options, extraData) {
        //FIXME: HACK: this is a bandaid.  need better way to not bind multiple autocompletes
        if ($elements.data("autocompleteApplied")) {
            return;
        }
        $elements.data("autocompleteApplied", true);
        var _options = {};
        if (typeof options === "object") {
            _options = options;
        }
        var defaults = {};
        options.enhanceRequestData = function (requestData) {
            $.extend(requestData, extraData);
        };

        defaults.url = "lookup/collection";
        defaults.dataPath = "collections";
        defaults.sortField = 'TITLE';
        defaults.showCreate = false;
        if (defaults.showCreate) {
            defaults.showCreatePhrase = "Create a new collection";
        }
        defaults.minLength = 2;
        _applyGenericAutocomplete($elements, $.extend({}, defaults, _options));
    }

    /**
     * autocomplete render callback used by applyResourceAutocomplete when displaying search results menu
     * @param item
     * @returns {*}
     * @private
     */
    function _displayResourceAutocomplete(item) {
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

    /**
     * initialize tDAR resource autocomplete fields
     * @param $elements jquery selection of text fields to initialize as autocomplete fields
     * @param type resource type (e.g. "document", "coding-sheet", "image")
     */
    function _applyResourceAutocomplete($elements, type) {
        var options = {};
        options.url = "lookup/resource";
        options.dataPath = "resources";
        options.sortField = 'TITLE';
        options.enhanceRequestData = function (requestData) {
            if (requestData["subCategoryId"] != undefined && requestData["subCategoryId"] != '' && requestData["subCategoryId"] != -1) {
                requestData["sortCategoryId"] = requestData["subCategoryId"];
            }
            requestData.resourceTypes = type;
        };
        options.ignoreRequestOptionsWhenEvaluatingEmptyRow = [ "subCategoryId", "sortCategoryId" ];
        options.minLength = 0;
        options.customDisplayMap = _displayResourceAutocomplete;
        options.customRender = function (ul, item) {
            var description = "";
            if (item.description != undefined) {
                description = item.description;
            }
            var link = "";
            return $("<li></li>").data("item.autocomplete", item).append("<a  title=\"" + TDAR.common.htmlDecode(description) + "\">" + TDAR.common.htmlDoubleEncode(item.value) + link + "</a>").appendTo(ul);
        };

        _applyGenericAutocomplete($elements, options);
        $elements.autocomplete("option", "delay", 600);
    }

    /**
     * initalize institution autocomplete fields
     *
     * @param $elements jquery selection of text inputs to initialize as autocomplete fields
     * @param newOption if true, allow user to create new records if specified value not found on server
     */
    function _applyInstitutionAutocomplete($elements, newOption) {

        var options = {};
        options.url = "lookup/institution";
        options.dataPath = "institutions";
        options.sortField = 'RELEVANCY';
        options.enhanceRequestData = function (requestData) {
            requestData.institution = requestData.term;
        };
        options.showCreate = newOption;
        options.minLength = 2;
        options.showCreatePhrase = "Create new institution";
        _applyGenericAutocomplete($elements, options);
    };

    /**
     * click handler - used to implement an autocomplete that has a "show-all" button, e.g. a combobox
     * @private
     */
    function _autocompleteShowAll() {
        $(this).siblings('input[type=text]').focus().autocomplete("search", "");
    }

    /**
     * initialize "combobox" style autocompletes.  These are similar to autocomplete fields,  however, they include
     * a "show all" button beside the control that displays all possible values.  As a result, this is not ideal
     * for lookups that return relatively small numbers of results (e.g. list of U.S. States).
     *
     * @param $elements jquery selection of elements to convert into comboboxes
     * @param type resource type name (e.g "document", "coding-sheet".
     */
    function _applyComboboxAutocomplete($elements, type) {

        //register autocomplete text box
        _applyResourceAutocomplete($elements, type);

        $elements.each(function () {
            //the autocomplete text field
            var $elem = $(this);

            //register "show-all" click
            var $controls = $(this).closest('.controls');
            var $textInput = $controls.find("input[type=text]");
            var $button = $controls.find("button.show-all");
            $button.click(function () {
                $textInput.focus().autocomplete("search", "");
            });

            //override the default change-event listener
            $elem.autocomplete("option", "change", function(event){
                //the most recent menu item that the user selected
                var item = $elem.data('autocompleteSelectedItem');

                //the hidden input that holds the ID for the associated record in tdar
                var $idElem = $($elem.attr("autocompleteIdElement"));

                //if user deletes the contents of the text field, we also clear the ID field value
                if($elem.val() === "") {
                    $idElem.val("");
                }

                //if the user manually changed the value (as opposed to using the autocomplete menu), clear the ID field value
                if(item && $elem.val() !== item) {
                    $idElem.val("");
                }
            });

            //prime the initial value of the 'previously selected' menu item
            if($elem.val() !== "") {
                $elem.data('autocompleteSelectedItem', $elem.val());
            }
        });
    }

    /**
     * delegate listener that enables autocomplete for creator input fields when a user clicks in a crator field.
     * @param id parent element to receive delegated events
     * @param user if true, use applyPersonAutocomplete, otherwise use applyInstitutionAutocomplete
     * @param showCreate show a "create new" option at the end of the list.
     */
    var _delegateCreator = function (id, user, showCreate) {
        if (user == undefined || user == false) {
            $(id).delegate(".nameAutoComplete", "focusin", function () {
                        // TODO: these calls re-regester every row after a row is
                        // created,
                        // change so that only the new row is registered.
                        _applyPersonAutoComplete($(".nameAutoComplete", id), false, showCreate);
                    });
            $(id).delegate(".institutionAutoComplete", "focusin", function () {
                TDAR.autocomplete.applyInstitutionAutocomplete($(".institution", id), true);
            });
        } else {
            $(id).delegate(".userAutoComplete", "focusin", function () {
                TDAR.autocomplete.applyPersonAutoComplete($(".userAutoComplete", id), true, false);
            });
        }
    };

    /**
     * delegate listener that enables autocomplete for annotationKey input fields when a user clicks in that
     * field.
     * @param id id of parent element to receive delegated events.
     * @param prefix prefix of classname to use in selector when selecting input fields inside of the parent
     * @param delim  lookupType to send in ajax request ot search provider
     * @private
     */
    var _delegateAnnotationKey = function (id, prefix, delim) {
        $(id).delegate("." + prefix + "AutoComplete", "focusin", function () {
            _applyGenericAutocomplete($("." + prefix + "AutoComplete"), {
                url: "lookup/" + delim,
                dataPath: "items",
                sortField: 'LABEL',
                minLength: 2,
                customDisplayMap: function(item)  {
                    item.label = item.key;
                    return item;
                }
            });
        });
    };

    /**
     * delegate listener that enables autocomplete for annotationKey input fields when a user clicks in that
     * @param id id of parent element to receive delegated events.
     * @param prefix prefix of classname to use in selector when selecting input fields inside of the parent
     * @param type keyword type
     * @private
     */
    var _delegateKeyword = function (id, prefix, type) {
        $(id).delegate(".keywordAutocomplete", "focusin", function () {
            // TODO: these calls re-register every row after a row is created,
            // change so that only the new row is registered.
            // console.log('focusin:' + this.id);
            _applyKeywordAutocomplete(id + " .keywordAutocomplete", "keyword", {
                keywordType: type
            }, true);
        });

    };

    /**
     * return an object from any autocomplete input elements inside the specified parentElem elment. This function
     * maps every .autocomplete-ui-input  into a property of the returned object.  The property name is based on the
     * value of the "autocompletename" attribute of the input element (or the value of the 'name' attribute, if no
     * autocompletename attribute specified.
     *
     * @param parentElem
     */
    function _objectFromAutocompleteParent(parentElem) {
        var obj = {};
        $(parentElem).find(".ui-autocomplete-input").each(function () {
            var key = $(this).attr("autocompleteName") || this.name;
            obj[key] = this.value;
        });
        return obj;
    }

    return {
        applyPersonAutoComplete: _applyPersonAutoComplete,
        evaluateAutocompleteRowAsEmpty: _evaluateAutocompleteRowAsEmpty,
        applyKeywordAutocomplete: _applyKeywordAutocomplete,
        applyCollectionAutocomplete: _applyCollectionAutocomplete,
        applyResourceAutocomplete: _applyResourceAutocomplete,
        applyInstitutionAutocomplete: _applyInstitutionAutocomplete,
        applyComboboxAutocomplete: _applyComboboxAutocomplete,
        objectFromAutocompleteParent: _objectFromAutocompleteParent,
        "delegateCreator": _delegateCreator,
        "delegateAnnotationKey": _delegateAnnotationKey,
        "delegateKeyword": _delegateKeyword,
        "buildRequestData": _buildRequestData,
        "ObjectCache": ObjectCache
    };
})();
