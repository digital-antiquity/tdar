TDAR.select2autocomplete = (function($, ctx) {
    "use strict";


    var _setDefaults = function() {
	    $.fn.select2.defaults.set("tokenSeparators", [";","|"]);
	    $.fn.select2.defaults.set("minimumInputLength", 3);
	    $.fn.select2.defaults.set("ajax--delay", 250);
	    $.fn.select2.defaults.set("ajax--cache", true);
	    $.fn.select2.defaults.set("ajax--dataType", "jsonp");
    }

    /**
     * if there's a data attribute to associate with valdiateMethod, then see if it's a function. if it's not a function, then call validate() plain.
     */
    var _init = function() {

    	_setDefaults();
            $(".keyword-autocomplete").select2({
            	tags:true,
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
            })

            $(".losenge").click(function(e) {
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

    var _highlightKeyword = function(keyword) {
    	var label = keyword.text;
    	if (!keyword.id || !keyword.text) {
    		label = keyword.text; 
    	}
    	return $("<span data-id='"+keyword.id+"' style='z-index:1000' class='losenge'>"+label+"</span>");
    }

    return {
        "init" : _init,
    }
})(jQuery, window);

//FIXME: inline onload binding complicates testing and makes it harder to discover the total number of initializers (and their sequence)
$(function() {
    TDAR.select2autocomplete.init();
});
