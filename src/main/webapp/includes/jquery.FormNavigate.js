/**
 * jQuery.FormNavigate.js
 * jQuery Form onChange Navigate Confirmation plugin
 *
 * Copyright (c) 2009 Law Ding Yong
 * 
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/mit-license.php
 *
 * See the file license.txt for copying permission.
 */
 
 /**
  * Documentation :
  * ==================
  *
  * How to Use:
  * $("YourForm").FormNavigate(options);
  *  -- "YourForm" as Your Form ID $("#form") or any method to grab your form
  *  -- options: can be either a string (representing the confirmation message the plugin will display), or an object with the following fields
  *         message:    confirmation messaged displayed to the user
  *         customEvents:     A space-separated list of additional events that, when triggered within the form, will cause the plugin to consider the form "dirty".
  *                     The plugin will always monitor keyup and change events.  
  *     
  *
  *
  * This plugin handles onchange of input type of text, textarea, password, radio, checkbox, select and file to toggle on 

and off of window.onbeforeunload event.
  * Users are able to configure the custom onBeforeUnload message.
  */
(function($){
    $.fn.FormNavigate = function(options) {
        "use strict";
        var _options = {customEvents:null, message:"Your changes have not been saved."};
        if(typeof options === "string") {
            _options.message = options;
        }else if(typeof options === "object") {
            $.extend(_options, options);
        }
        
        this.each(function() {
            var $this = $(this);
            $this.data("formNavigate", true);
        	$(window).bind("beforeunload", function (event) {
                    if ($this.data("formNavigate")) {  event.cancelBubble = true;  }  else  { return _options.message;}
        	});
        	
        	var _eventHandler = function(evt) {
                console.log("Form #%s has become dirty. event:%s\t target:%s\t ", $this.attr("id"), evt.type, evt.target);
                $this.data("formNavigate", false);
        	};
        	
        	$this.one("keyup change", _eventHandler);
        	if(_options.customEvents) {
        	    $this.one(_options.customEvents, _eventHandler);
        	}
        	
        	$this.find("input:submit, .submitButton").click(function(){
        	    $this.data("formNavigate", true);
        	});
        });
    }
})(jQuery);