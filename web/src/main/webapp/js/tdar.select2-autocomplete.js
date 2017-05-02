TDAR.select2autocomplete = (function ($, ctx) {
    "use strict";


    var _getDefaults = function () {
        return  {
            tokenSeparators: [";", "|"],
            minimumInputLength: 0,
            ajax: {
                delay: 250,
                cache: true,
                dataType: "jsonp"
            }
        };
    };


    /**
     * if there's a data attribute to associate with valdiateMethod, then see if it's a function. if it's not a function, then call validate() plain.
     */
    var _init = function () {

        /** NOTE!!! select2 can set defaults via the above arrays or via data-attributes we're using them to set the URL and a few other common settings **/
        $(".keyword-autocomplete").select2($.extend(true, _getDefaults(), {
            tags: true,
            templateResult: _highlightKeyword,
            templateSelection: _highlightKeyword,
            ajax: {
                data: function (params) {
                    return {
                        term: params.term, // search term
                        page: params.page
                    };
                },

                processResults: function (data, params) {
                    if (data && data.items && data.items.length > 0) {
                        for (var item in data.items) {
                            data.items[item].text = data.items[item].label;
                            data.items[item]._id = data.items[item].id;
                            data.items[item].id = data.items[item].label;
                        }
                    }

                    // parse the results into the ; format expected by Select2
                    // since we are using custom formatting functions we do not need to
                    // alter the remote JSON data, except to indicate that infinite
                    // scrolling can be used
                    params.page = params.page || 1;

                    return {
                        results: data.items,
                        pagination: {
                            more: (params.page * 30) < data.total_count
                        }
                    };
                },
            },
            // hide input too short
        }));

        $(".resource-autocomplete").select2($.extend(true, _getDefaults(), {
            templateResult: _highlightResource,
            templateSelection: _highlightResource,
            ajax: {
                data: function (params) {
                    return {
                        term: params.term, // search term
                        page: params.page
                    };
                },

                processResults: function (data, params) {
                    console.log(data);
                    if (data && data.resources) {
                        data.resources.unshift({id:"-1",title:"No parent project"});
                        data.resources.unshift({id:"", title:"" });
                        for (var item in data.resources) {
                            data.resources[item].text = data.resources[item].title;
                        }
                    }

                    // parse the results into the ; format expected by Select2
                    // since we are using custom formatting functions we do not need to
                    // alter the remote JSON data, except to indicate that infinite
                    // scrolling can be used
                    params.page = params.page || 1;

                    return {
                        results: data.resources,
                        pagination: {
                            more: (params.page * 30) < data.total_count
                        }
                    };
                },
            },

        }));

        // Register clear button for resource autocompletes
        $('.btn-clear-select').on("click", function() {
            var $elem = $(this);
            //clear any inputs that share the same parent as the select2 component
            $elem.parent().find(".resource-autocomplete").val(null).trigger("change");
        });


        $(".losenge").click(function (e) {
            e.stopPropagation();
            return false;
        });

//    $(".js-example-tags").bind("paste", function(e){
//        // access the clipboard using the api
//        var pastedData = e.originalEvent.clipboardData.getData('text');
//        e.originalEvent.clipboardData.setData(
//    } );

        // $(".select2-selection__choice").click(function(e) {
        // console.log(e);
        // e.stopPropagation();
        // return false;
        // });
    };

    var _highlightResource = function (resource) {
        if (resource.text) {
            resource.title = resource.text;
        }
        var $elem = $('<span data-id="' + resource.id + '" style="z-index:1000;"><span>' + resource.title + ' </span></span>')
        if(resource.id > 0) {
            $elem.append('<span> (tDAR ID #' + resource.id + ')');
        }
        return $elem;
    };

    var _highlightKeyword = function (keyword) {
        var label = keyword.text;
        if (!keyword.id || !keyword.text) {
            label = keyword.text;
        }
        return $("<span data-id='" + keyword.id + "' style='z-index:1000' class='losenge'>" + label + "</span>");
    }

    return {
        "init": _init,
        "main": _init
    };
})(jQuery, window);

