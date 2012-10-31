/**
 * jQuery.FormNavigate.js
 * jQuery Form onChange Navigate Confirmation plugin
 * Browser Compatibility : IE 6.0, 7.0, 8.0; Firefox 2.0+;  Safari 3+; Opera 9+; Chrome 1+;
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
var global_formNavigate = true;		// Js Global Variable for onChange Flag
(function($){
    $.fn.FormNavigate = function(message) {
    	$(window).bind("beforeunload", function (event) {
                if (global_formNavigate == true) {  event.cancelBubble = true;  }  else  { return message;              }
    	});
    	var $this = $(this);
//    	$this.delegate("input, select, textarea","keyup change", function() {
    	$this.one("keyup change", function() {
    		global_formNavigate = false;
    	});
//        $(this+ ":input[type='text'], :input[type='button'], :input[type='textarea'], :input[type='password'], :input[type='radio'], :input[type='checkbox'], :input[type='file'], select").change(function(){
//            global_formNavigate = false;
//        });
//		//to handle back button
//		$(this+ ":input[type='textarea']").keyup(function(){ 
//			global_formNavigate = false; 
//		}); 
        $("input:submit",$this).click(function(){
            global_formNavigate = true;
        });
    }
})(jQuery);