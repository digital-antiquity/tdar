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
 *         message:    confirmation messaged displayed to the user (default: 'your changes have not been saved')
 *
 *         customEvents:     A space-separated list of additional events that, when triggered within the form, will cause the plugin to consider the form "dirty".
 *                     The plugin will always monitor keyup and change events. (no default)
 *
 *         cleanOnSubmit:   set state to 'clean' on form submit, allowing the navigation. Note that the state remains clean even if the submit
 *                            event is cancelled (e.g. by form validation proces).  (default: true)
 *
 *
 *  -- FormNavigate methods:  methods available after you've initialized formNavigate
 *
 *         $("YourForm").FormNavigate("status"):  returns "clean", "dirty", or "not initialized"
 *         $("YourForm").FormNavigate("clean"):  forces the status to "clean"
 *
 *
 *
 * This plugin handles onchange of input type of text, textarea, password, radio, checkbox, select and file to toggle on

 and off of window.onbeforeunload event.
 * Users are able to configure the custom onBeforeUnload message.
 */
(function ($) {
    var DATA_FIELD = "formNavigate";

    $.fn.FormNavigate = function (options) {
        "use strict";

        //return state of first item in selection
        if (options === "status") {
            if (typeof this.data(DATA_FIELD) === "undefined") {
                return "not initialized";
            }
            return this.data(DATA_FIELD) ? "clean" : "dirty";

            //force the form clean
        } else if (options === "clean") {
            return this.each(function () {
                $(this).data(DATA_FIELD, true);
            });
        }  else if (options === "dirty") {
            return this.each(function () {
                $(this).data(DATA_FIELD, false);
            });
        }

        var _options = {customEvents: null, message: "Your changes have not been saved.", cleanOnSubmit: true};
        if (typeof options === "object") {
            $.extend(_options, options);
        }

        this.each(function () {
            var $this = $(this);
            $this.data(DATA_FIELD, true);
            $(window).bind("beforeunload", function (event) {
                if ($this.data(DATA_FIELD)) {
                    event.cancelBubble = true;
                } else {
                    return _options.message;
                }
            });

            var _eventHandler = function (evt) {
                $this.data(DATA_FIELD, false);
            };

            $this.one("keyup change", _eventHandler);
            if (_options.customEvents) {
                $this.one(_options.customEvents, _eventHandler);
            }

            if (_options.cleanOnSubmit) {
                $this.find(' input[type="submit"], button[type="submit"], .submitButton').click(function () {
                    $this.data(DATA_FIELD, true);
                });
            }

        });
        return this;
    };
    $.fn.FormNavigate.DATA_FIELD = DATA_FIELD;
})(jQuery);
