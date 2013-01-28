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
  * $("YourForm").FormNavigate("YourMessage");
  *  -- "YourForm" as Your Form ID $("#form") or any method to grab your form
  *  -- "YourMessage" as Your onBeforeUnload Prompt Message Here
  *
  * This plugin handles onchange of input type of text, textarea, password, radio, checkbox, select and file to toggle on 

and off of window.onbeforeunload event.
  * Users are able to configure the custom onBeforeUnload message.
  */
(function($){
    $.fn.FormNavigate = function(message) {
        this.each(function() {
            var $this = $(this);
            $this.data("formNavigate", true);
        	$(window).bind("beforeunload", function (event) {
                    if ($this.data("formNavigate")) {  event.cancelBubble = true;  }  else  { return message;}
        	});
        	
        	$this.one("keyup change", function(evt) {
        	    console.log("Form #%s has become dirty. event:%s\t target:%s\t ", $this.attr("id"), evt.type, evt.target);
        	    $this.data("formNavigate", false);
        	});
        	
        	$this.find("input:submit, .submitButton").click(function(){
        	    $this.data("formNavigate", true);
        	});
        });
    }
})(jQuery);