/*
 * $Id$
 * 
 * Common JS functions used in tDAR (with dependency on JQuery).  
 * Mostly have to do with adding new rows for multi-valued fields, etc.
 */

//Define a dummy console for browsers that don't support logging
if (!window.console) {
    console = {};
}
console.log = console.log || function() {
};
console.warn = console.warn || function() {
};
console.debug = console.debug || function() {
};
console.error = console.error || function() {
};
console.info = console.info || function() {
};
console.trace = function() {
};
// To quickly disable all console messages, uncomment the following line
// console.log = console.debug = console.warn = console.error = console.info =
// function() {};
// or simply turn off the mundane console messages
// console.debug = function(){};
//
if (!window.JSON)
    JSON = {};
JSON.stringify = JSON.stringify || function() {
};

TDAR.ellipsify = function(str, maxlength) {
    if (!str)
        return;
    var newString = str;
    if (str.length > maxlength - 3) {
        newString = str.substring(0, maxlength - 3) + "...";
    }
    return newString;
};

function getQSParameterByName(name) {
    name = name.replace(/[\[]/, "\\\[").replace(/[\]]/, "\\\]");
    var regexS = "[\\?&]" + name + "=([^&#]*)";
    var regex = new RegExp(regexS);
    var results = regex.exec(window.location.href);
    if (results == null)
        return "";
    else
        return decodeURIComponent(results[1].replace(/\+/g, " "));
}

// Compare two arrays. return true if A and B contain same elements (
// http://stackoverflow.com/questions/1773069/using-jquery-to-compare-two-arrays
jQuery.extend({
    compareArray : function(arrayA, arrayB, ignoreOrder) {
        if (arrayA.length !== arrayB.length) {
            return false;
        }
        // ignore order by default
        if (typeof ignoreOrder === 'undefined') {
            ignoreOrder = true;
        }
        var a = arrayA, b = arrayB;
        if (ignoreOrder) {
            a = jQuery.extend(true, [], arrayA);
            b = jQuery.extend(true, [], arrayB);
            a.sort();
            b.sort();
        }
        for ( var i = 0, l = a.length; i < l; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
});


/**
 * trying to move these functions out of global scope and apply strict parsing.
 */


TDAR.namespace("common");
TDAR.common = function() {
    "use strict";
    
    var self = {};
    
    var _defaultValidateOptions = {
        errorLabelContainer : $("#error ul"),
        wrapper: "li",
        highlight: function(element, errorClass, validClass) {
            $(element).addClass("error");
            $(element).closest("div.control-group").addClass("error");
         },
         unhighlight:function(element, errorClass, validClass) {
             $(element).trigger("unhighlight", [errorClass, validClass]);
             $(element).removeClass("error");
             //highlight this div until all visible controls in group are valid
             var $controlGroup = $(element).closest("div.control-group");
             if($controlGroup.find('.error:visible').length === 0) {
                 $controlGroup.removeClass("error");
             }
         },
        showErrors: function(errorMap, errorList) {
            this.defaultShowErrors();
        }
                 
    };
    
    //TODO: remove redundant code -- this is very similar to repeatrow._clearInputs.
    var _clearInputs = function($parents) {
    	
    	//FIXME: can we just set all of these to disabled instead?
    	
        //clear any non-showing creator proxy fields so server knows the actualCreatorType for each
        console.log("clearing unused proxy fields");
        // most input elements should have value attribute cleared (but not radiobuttons, checkboxes, or buttons)
        $parents.find("input[type!=button],textarea").not('input[type=checkbox],input[type=radio]').val("");
        // uncheck any checkboxes/radios
        $parents.find("input[type=checkbox],input[type=radio]").prop("checked", false);
        // remove "selected" from options that were already selected
        $parents.find("option[selected=selected]").removeAttr("selected");
        // revert all select inputs to first option. 
        $parents.find("select").attr("disabled", "disabled");
    }


  //indicate the root context  to use when populateTarget is called. 
    var _setAdhocTarget = function(elem, selector) {
        var _selector = selector;
        if (!_selector) selector = "div";
        var adhocTarget = $(elem).closest(_selector);
        $('body').data("adhocTarget", adhocTarget);
        //expose target for use by child window
        TDAR.common.adhocTarget = adhocTarget;
        //return false; 
    }
    
    

    var _dynamicSort = function(property, caseSensitive) {
        return function(a, b) {
            if (caseSensitive == undefined || caseSensitive == false) {
                return (a[property].toLowerCase() < b[property].toLowerCase()) ? -1
                        : (a[property].toLowerCase() > b[property].toLowerCase()) ? 1
                                : 0;
            } else {
                return (a[property] < b[property]) ? -1
                        : (a[property] > b[property]) ? 1 : 0;
            }
        };
    }

var _sortFilesAlphabetically= function() {
        var rowList = new Array();
        var $table = $("#files tbody");
        $("tr", $table).each(function() {
            var row = {};
            row["id"] = $(this).attr("id");
            row["filename"] = $(".filename", $(this)).text();
            rowList[rowList.length] = row;
        });

        rowList.sort(_dynamicSort("filename"));

        for (var i = 0; i < rowList.length; i++) {
            $("#" + rowList[i]["id"]).appendTo("#files");
        }
    }

  //populate a coding sheet / ontology field from an adhoc add-resource child page. 
  //for now, let's assume there's never more than one adhoc child in play...
  var _populateTarget = function(obj) {
      var $body = $("body");
      var adhocTarget = $body.data("adhocTarget");
      if(typeof(adhocTarget) == 'undefined') return;

      console.log("populateTarget called.   adHocTarget:%s", adhocTarget);
      $('input[type=hidden]', adhocTarget).val(obj.id);
      $('input[type=text]', adhocTarget).val(obj.title);
      $body.removeData("adhocTarget");
      TDAR.common.adhocTarget = null;
  }

    var _toggleLicense = function() {

        // update display of licenses when the radio button selection changes
        $("#license_section input[type='radio']").each(
            function(index) {
                // show or hide the row depending on whether the corresponding radio button is checked
                var $this = $(this);
                var license_type_name = $this.val();
                var license_details_reference = "#license_details_" + license_type_name;
                var license_details = $(license_details_reference);
                var $licenseText = $('#licenseText');
                if ($this.is(":checked")) {
                    license_details.removeClass('hidden');
                } else {
                    license_details.addClass('hidden');
                }    
                if (!$licenseText.is(':hidden')) {
                    $licenseText.addClass("required");
                } else {
                    $licenseText.removeClass("required");
                }
            }
        );
    }


    
     // FIXME: the jquery validate documentation for onfocusout/onkeyup/onclick
     // doesn't jibe w/ what we see in practice. supposedly these take a boolean
     // argument specifying 'true' causes an error. since true is the default for
     // these three options I'm simply removing those lines from the validate
     // call
     // below.
     // see http://docs.jquery.com/Plugins/Validation/validate#options for
     // options and defaults
     // see http://whilefalse.net/2011/01/17/jquery-validation-onkeyup/ for
     // undocumented feature that lets you specify a function instead of a
     // boolean.
    var _setupFormValidate = function(form) {
        var options = {
            onkeyup : function() {
                return;
            },
            onclick : function() {
                return;
            },
            onfocusout : function(element) {
                return;
                // I WORK IN CHROME but FAIL in IE & FF
                // if (!dialogOpen) return;
                // if ( !this.checkable(element) && (element.name in
                // this.submitted ||
                // !this.optional(element)) ) {
                // this.element(element);
                // }
            },
            showErrors: function(errorMap, errorList) {
                this.defaultShowErrors();
                //spawn a modal widget and copy the errorLabelContainer contents (a separate div) into the widget's body section
                //TODO: docs say this is only called when errorList is not empty - can we remove this check?
                if (typeof errorList !== "undefined" && errorList.length > 0) {
                    $('#validationErrorModal .modal-body p').empty().append($("<ul></ul>").append($('#error ul').html()));
                    $('#validationErrorModal').modal();

                }
                $('#error').show();
            },
            submitHandler : function(f) {
                //prevent double submit and dazzle user with animated gif
                _submitButtonStartWait();
                _clearInputs($(f).find(".creatorPerson.hidden, .creatorInstitution.hidden")); 
                $('#error').hide();
                
                $(f).FormNavigate("clean");
                f.submit();
                
            }
        };

         var allValidateOptions = $.extend({}, _defaultValidateOptions, options);
         $(form).validate(allValidateOptions);
     };
     
    var _initRegformValidation = function(form) {
        var $form = $(form);
        var options = {
            errorLabelContainer:
                    $("#error"),
            rules: {
                confirmEmail: {
                    equalTo: "#emailAddress"
                },
                password: {
                    minlength: 3
                },
                username: {
                    minlength: 5
                },
                confirmPassword: {
                    minlength: 3,
                    equalTo: "#password"
                },
                'person.contributorReason': {
                    maxlength: 512
                }
            },
            messages: {
                confirmEmail: {
                    email: "Please enter a valid email address.",
                    equalTo: "Your confirmation email doesn't match."
                },
                password: {
                    required: "Please enter a password.",
                    minlength: jQuery.format("Your password must be at least {0} characters.")
                },
                confirmPassword: {
                    required: "Please confirm your password.",
                    minlength: jQuery.format("Your password must be at least {0} characters."),
                    equalTo: "Please make sure your passwords match."
                }
            }
        };
        $form.validate($.extend({},  _defaultValidateOptions, options));

    };
             
    //setup other form edit controls
    //FIXME: wny is this broken out from  initEditPage?   If anything, break it out even further w/ smaller private functions
    var _setupEditForm = function (form) {
        var $form = $(form);
        //fun fact: because we have a form field named "ID",  form.id actually refers to this DOM element,  not the ID attribute of the form.
        var formid = $form.attr("id");
        
        // prevent "enter" from submitting
        $form.delegate('input,select',"keypress", function(event) {
            return event.keyCode != 13;
        });

        //initialize form validation
        _setupFormValidate(form);
        
        //prepwork prior to form submit (trimming fields)
        $form.submit(function(f) {
            try {
                $.each($('.reasonableDate, .coverageStartYear, .coverageEndYear, .date, .number'), function(idx, elem) {
                    if ($(elem).val() !== undefined)  {
                        $(elem).val($.trim($(elem).val()));
                    }
                });
            } catch (err) {
                console.error("unable to trim:" + err);
            }

            var $button = $('input[type=submit]', f);
            $button.siblings(".waitingSpinner").show();

            //warn user about leaving before saving
            //FIXME: FormNavigate.js has bugs and is not being maintained. need to find/write replacement.
            $("#jserror").val("");
            return true;
        });


        $('.coverageTypeSelect', "#coverageDateRepeatable").each(function(i, elem) {
            _prepareDateFields(elem);
        });

        var $uploaded = $(formid + '_uploadedFiles');
        if ($uploaded.length > 0) {
            var validateUploadedFiles = function() {
                if ($uploaded.val().length > 0) {
                    $("#reminder").hide();
                }
            };
            $uploaded.change(validateUploadedFiles);
            validateUploadedFiles();
        }

        Modernizr.addTest('cssresize', Modernizr.testAllProps('resize'));
        
        if (!Modernizr.cssresize) {
            $('textarea.resizable:not(.processed)').TextAreaResizer();
        }

        $("#coverageDateRepeatable").delegate(".coverageTypeSelect", "change", function() {
            _prepareDateFields(this);
        });
        _showAccessRightsLinkIfNeeded();
        $('.fileProxyConfidential').change(_showAccessRightsLinkIfNeeded);
        
        //FIXME: idea is nice, but default options produce more annoying UI than original browser treatment of 'title' attribute. also, bootstrap docs
        //       tell you how to delegate to selectors but I couldn't figure it out.
        //$(form).find('label[title]').tooltip();
        
        
        
        if ($('#explicitCoordinatesDiv').length > 0) {
            $('#explicitCoordinatesDiv').toggle($('#viewCoordinatesCheckbox')[0].checked);
        
        }
        $(".latLong").each(function(index, value){
            $(this).hide();
            //copy value of hidden original to the visible text input
            var id = $(this).attr('id'); 
            $('#d_' + id).val($('#' + id).val());
        });
        
        $("#jserror").val("SAVE");
        
        // delete/clear .repeat-row element and fire event
        $('#copyrightHolderTable').on("click", ".row-clear", function(e){
            var rowElem = $(this).parents(".repeat-row")[0];
            TDAR.repeatrow.deleteRow(rowElem);
        });
        
        
    };
    
 // called whenever date type changes
  //FIXME: I think we can improve lessThanEqual and greaterThenEqual so that they do not require parameters, and hence can be 
//         used via $.validator.addClassRules.  The benefit would be that we don't need to register these registration rules each time a date
//         gets added to the dom.
  var _prepareDateFields = function(selectElem) {
      var startElem = $(selectElem).siblings('.coverageStartYear');
      var endElem = $(selectElem).siblings('.coverageEndYear');
      $(startElem).rules("remove");
      $(endElem).rules("remove");
      switch ($(selectElem).val()) {
      case "CALENDAR_DATE":
          $(startElem).rules("add", {
              range : [ -99900, 2100 ],
              lessThanEqual : [endElem,"Calender Start", "Calendar End"],
              required : function() {
                  return $(endElem).val() != "";
              }
          });
          $(endElem).rules("add", {
              range : [ -99900, 2100 ],
              required : function() {
                  return $(startElem).val() != "";
              }
          });
          break;
      case "RADIOCARBON_DATE":
          $(startElem).rules("add", {
              range : [ 0, 100000 ],
              greaterThanEqual : [endElem, "Radiocarbon Start", "Radiocarbon End"],
              required : function() {
                  return $(endElem).val() != "";
              }
          });
          $(endElem).rules("add", {
              range : [ 0, 100000 ],
              required : function() {
                  return $(startElem).val() != "";
              }
          });
          break;
      case "NONE":
          $(startElem).rules("add", {
              blankCoverageDate: {"start":startElem, "end":endElem}
          });
          break;
      }
  }

    
    var _applyTreeviews = function() {
        //console.debug("applying tdar-treeviews v3");
        var $treeviews = $(".tdar-treeview");
        $treeviews.treeview({
                collapsed : true
        });
        // expand ancestors if any children are selected
        $treeviews.find("input:checked").parents(".hitarea").trigger("click");
    };
    
   var _submitButtonStartWait = function(){
       var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
       var $buttons = $submitDivs.find(".submitButton");
       $buttons.prop("disabled", true);
       
       //fade in the wait icon
       $submitDivs.find(".waitingSpinner").show();
   };
   
   var _submitButtonStopWait = function() {
       var $submitDivs = $('#editFormActions, #fakeSubmitDiv');
       var $buttons = $submitDivs.find(".submitButton");
       $buttons.prop("disabled", false);
       
       //fade in the wait icon
       $("#possibleJsError").val("");
       $submitDivs.find(".waitingSpinner").hide();
   } 
    
    
    //public: initialize the edit page form
    var _initEditPage = function(form) {
        $("#possibleJsError").val("INIT");

       //Multi-submit prevention disables submit button, so it will be disabled if we get here via back button. So we explicitly enable it. 
        _submitButtonStopWait();
        
        $("#fakeSubmitButton").click(function() {$("#submitButton").click();});

        $("#subnavbar .nav a").click(function() {
            $($(this).attr('href')).animate({
                backgroundColor : "#ffffee"
            }, 200).animate({
                backgroundColor : "transparent"
            }, 400);
            return true;
        });

        //init repeatrows
        TDAR.repeatrow.registerRepeatable(".repeatLastRow");
        
        //init person/institution buttons
        $(".creatorProxyTable").on("click", '.creator-toggle-button', function(event){
            var $this = $(this);
            var $top = $this.closest(".repeat-row");
            if ($top == undefined) {
            	$top = $this.closest(".control-group");
            }
            if ($(event.target).hasClass("personButton")) {
                $(".creatorPerson", $top).removeClass("hidden");
                $(".creatorInstitution",$top).removeClass("hidden").addClass("hidden");
            } else {
                $(".creatorPerson", $top).removeClass("hidden").addClass("hidden");
                $(".creatorInstitution",$top).removeClass("hidden");
            }
        });    
        

        //wire up autocompletes
        _delegateCreator("#authorshipTable", false, true);
        _delegateCreator("#creditTable", false, true);
        _delegateCreator("#divAccessRights", true, false);
        _delegateCreator("#divSubmitter", true, false);
        _delegateCreator("#copyrightHolderTable",false,true);
        _delegateAnnotationKey("#resourceAnnotationsTable", "annotation", "annotationkey");
        _delegateKeyword("#siteNameKeywordsRepeatable", "sitename", "SiteNameKeyword");
        _delegateKeyword("#uncontrolledSiteTypeKeywordsRepeatable", "siteType", "SiteTypeKeyword");
        _delegateKeyword("#uncontrolledCultureKeywordsRepeatable", "culture", "CultureKeyword");
        _delegateKeyword("#temporalKeywordsRepeatable", "temporal", "TemporalKeyword");
        _delegateKeyword("#otherKeywordsRepeatable", "other", "OtherKeyword");
        _delegateKeyword("#geographicKeywordsRepeatable", "geographic", "GeographicKeyword");
        TDAR.autocomplete.applyInstitutionAutocomplete($('#txtResourceProviderInstitution'), true);
        TDAR.autocomplete.applyInstitutionAutocomplete($('#publisher'), true);
        $('#resourceCollectionTable').on(
                "focus",
                ".collectionAutoComplete",
                function() {
                    TDAR.autocomplete.applyCollectionAutocomplete($(this), {showCreate:true}, {permission:"ADMINISTER_GROUP"});
                });

        // prevent "enter" from submitting
        $('input,select').keypress(function(event) {
            return event.keyCode != 13;
        });

        //init sortables
        //FIXME: sortables currently broken 
        $(".alphasort").click(_sortFilesAlphabetically);
        
        //ahad: toggle license
        $(".licenseRadio",$("#license_section")).change(_toggleLicense);
        
//        //ahad: toggle person/institution for copyright holder
//        $("#copyright_holder_type_person").change(toggleCopyrightHolder);
//        $("#copyright_holder_type_institution").change(toggleCopyrightHolder);
    
        //if page has a navbar,  wire it up and refresh it whenever something changes page size (e.g. repeatrow additions)
        
        //fixme: ths scrollspy is being registered twice (remove data-attributes from scrollspy div?)
        $('#subnavbar').each(function() {
            var $scrollspy = $(this).scrollspy();
            
            //monitor document height and fire event when it changes
            $.documentHeightEvents();
            
            $(document).bind("repeatrowadded repeatrowdeleted heightchange", function() {
                //console.log("resizing scrollspy");
                $scrollspy.scrollspy("refresh");
            });
            
        });
        
        
        TDAR.contexthelp.initializeTooltipContent(form);
        _applyWatermarks(form);
        
        //FIXME: other init stuff that is separate function for some reason 
        _setupEditForm(form);

        _applyTreeviews();
        
        //show project preview button when appropriate
        $('#projectId').change(function() {
            var $select = $(this);
            var $row = $select.closest('.controls-row');
            $('.view-project', $row).remove();
            if($select.val().length > 0 && $select.val() !=="-1") {
                var href = getURI('project/' + $select.val());
                var $button = '<a class="view-project btn btn-small" target="_project" href="' + href + '">View project in new window</a>';
                $row.append($button);
            }
        }).change();
        
        
        //display generic wait message with ajax events
        _registerAjaxEvents();

        // I must be "last"
        $("#possibleJsError").val("SAVE");
        $(form).not('.disableFormNavigate').FormNavigate({
            message:"Leaving the page will cause any unsaved data to be lost!",
            customEvents: "repeatrowdeleted fileuploadstarted",
            cleanOnSubmit: false
        });

    };
    
    var _initializeView = function() {
        console.debug('initialize view');
	        var mapdiv = undefined;
	        if($('#large-google-map').length) {
	            mapdiv = $('#large-google-map')[0];
	        };
	        if ($(".google-map").length) {
	            mapdiv = $('.google-map')[0];
	        }
	        if (mapdiv != undefined) {
            var inputContainer = $("#divCoordContainer")[0];
            TDAR.maps.initMapApi();
            TDAR.maps.setupMap(mapdiv, inputContainer);
        }
    };
    
    //display generic wait message for ajax requests
    var _registerAjaxEvents = function() {
        $('body').bind('ajaxSend', function(e, jqXHR, ajaxOptions){
            if(typeof ajaxOptions.waitMessage === "undefined") {
                ajaxOptions.waitMessage = "Loading";
            }
            $('#ajaxIndicator').html("<strong>Waiting</strong>: " + ajaxOptions.waitMessage + "...").fadeIn('fast');
            //TODO: include a timeout to dismiss loading or display warning mesage
        });
        $('body').bind('ajaxComplete', function(e, jqXHR, ajaxOptions) {
            $('#ajaxIndicator').html("<strong>Complete</strong>: " + ajaxOptions.waitMessage + "...").fadeOut(1000);
        });
        
    };
    
    var _index = function(obj, key){
        if(typeof obj === "undefined") return undefined;
        return obj[key];
    };
    
    //public: for a given object, return the value of the field specified using 'dot notation'
    // e.g.:  getObjValue(obj, "foo.bar.baz") will return obj[foo][bar][baz]
    
    var _getObjValue = function(obj, strFieldName) {
        //FIXME: add fallback impl. when  Array.prototype.reduce() not supported (IE8)
        //https://developer.mozilla.org/en-US/docs/JavaScript/Reference/Global_Objects/Array/Reduce#Compatibility
        return strFieldName.split(".").reduce(_index, obj);
    }
    
    //return html-encoded copy of provided string
    var _htmlEncode = function(value) {
        if (value == undefined || value == '')
            return "";
        return $('<div/>').text(value).html();
        // older vesrion
        //      if (typeof value === "undefined" || str === '') return "";
        //            return $('<div></div>').text(str).html();
    }

    //return html-encoded copy of provided string
    var _htmlDoubleEncode = function(value) {
        return _htmlEncode(_htmlEncode(value));
    }

    var _determineResponsiveClass = function(width) {
        return width > 1200 ? 'responsive-large-desktop' :
            width > 979 ? 'responsive-desktop' :
            width > 767 ? 'responsive-tablet' :
        	width > 500 ? 'responsive-phone' :
            width > 1 ? 'responsive-phone-portrait' : '';
    }
    
    //hide the jira button for a week
    function _delayJiraButton() {
        //fixme: add function to hide jira button and store choice in cookie
        //id="" class="atlwdg-trigger atlwdg-TOP"
        $.cookie("hide_jira_button", true, { expires: 7});
        console.log("see you next week");
    
    }
    
    function _elipsify(text, n, useWordBoundary){
        /* from: http://stackoverflow.com/questions/1199352/smart-way-to-shorten-long-strings-with-javascript */
        var toLong = text.length>n,
            s_ = toLong ? text.substr(0,n-1) : text;
        s_ = useWordBoundary && toLong ? s_.substr(0,s_.lastIndexOf(' ')) : s_;
        return  toLong ? s_ + '...' : s_;
    }

    
    function _applyBookmarks() {
        var $this = $(this);
        var resourceId = $this.attr("resource-id");
        var state = $this.attr("bookmark-state");
        var $waitingElem = $("<img src='" + getURI('images/ui-anim_basic_16x16.gif') + "' class='waiting' />");
        $this.prepend($waitingElem);
        var $icon = $(".bookmark-icon",$this);
        $icon.hide();
        console.log(resourceId + ": " + state);
        var oldclass = "tdar-icon-" + state;
        var newtext = "un-bookmark";
        var newstate = "bookmarked";
        var action = "bookmarkAjax";
        var newUrl = "/resource/removeBookmark?resourceId=" + resourceId;
        
        if (state == 'bookmarked') {
            newtext = "bookmark";
            newstate = "bookmark";
            action = "removeBookmarkAjax";
            newUrl = "/resource/bookmark?resourceId=" + resourceId;
        }
        var newclass = "tdar-icon-" + newstate;
        
        $.getJSON(getBaseURI() + "resource/"+action+"?resourceId=" + resourceId,
                function(data) {
                    if (data.success) {
                        $(".bookmark-label",$this).text(newtext);
                        $icon.removeClass(oldclass).addClass(newclass).show();
                        $this.attr("bookmark-state",newstate);
                        $this.attr("href",newUrl);
                        $(".waiting",$this).remove();
                    }
                });
        
        return false;
    }

    //apply watermark input tags in context with watermark attribute.  'context' can be any valid argument to jQuery(selector[, context])
    var _applyWatermarks = function(context) {
        if(!Modernizr.input.placeholder){
            $("input[placeholder]", context).each(function() {
                //todo: see if its any faster to do direct call to attr, e.g. this.attributes["watermark"].value
                $(this).watermark($(this).attr("placeholder"));
            });
        }
    }


    // show the access rights reminder if any files are marked as confidential or if
    // the resource is embargoed
    var _showAccessRightsLinkIfNeeded = function() {
        if ($(".fileProxyConfidential").filter(function(index) {return $(this).val() != "PUBLIC"; }).length > 0) {
            $('#divConfidentialAccessReminder').removeClass("hidden");
        } else {
            $('#divConfidentialAccessReminder').addClass("hidden");
        }
    }




    var _htmlDecode = function(value) {
        if (value == undefined || value == '')
            return "";
        return $('<div/>').html(value).text();
    }

    // http://stackoverflow.com/questions/1038746/equivalent-of-string-format-in-jquery
    var _sprintf = function() {
        var s = arguments[0];
        for ( var i = 0; i < arguments.length - 1; i++) {
            var reg = new RegExp("\\{" + i + "\\}", "gm");
            s = s.replace(reg, arguments[i + 1]);
        }
        return s;
    }

    /**
     * Testing Support
     */

    function initializeView() {
        console.debug('initialize view');
        var maps = $(".google-map, #large-google-map");
        if(maps.length) {
            TDAR.maps.initMapApi();
            maps.each(function() {
                TDAR.maps.setupMap(this, this);
            });
        }
    }

    var _delegateCreator = function(id, user, showCreate) {
        if (user == undefined || user == false) {
            $(id).delegate(
                    ".nameAutoComplete",
                    "focusin",
                    function() {
                        // TODO: these calls re-regester every row after a row is
                        // created,
                        // change so that only the new row is registered.
                        TDAR.autocomplete.applyPersonAutoComplete($(".nameAutoComplete", id), false,
                                showCreate);
                    });
            $(id).delegate(".institutionAutoComplete", "focusin", function() {
                TDAR.autocomplete.applyInstitutionAutocomplete($(".institution", id), true);
            });
        } else {
            $(id).delegate(".userAutoComplete", "focusin", function() {
                TDAR.autocomplete.applyPersonAutoComplete($(".userAutoComplete", id), true, false);
            });
        }
    }

    // fixme: instead of focusin, look into using a customEvent (e.g. 'rowCreated')
    var _delegateAnnotationKey = function(id, prefix, delim) {
        $(id).delegate("." + prefix + "AutoComplete", "focusin", function() {
            TDAR.autocomplete.applyKeywordAutocomplete("." + prefix + "AutoComplete", delim, {}, false);
        });
    }

    var _delegateKeyword = function(id, prefix, type) {
        $(id).delegate(".keywordAutocomplete", "focusin", function() {
            // TODO: these calls re-regester every row after a row is created,
            // change so that only the new row is registered.
            console.log('focusin:' + this.id);
            TDAR.autocomplete.applyKeywordAutocomplete(id + " .keywordAutocomplete", "keyword", {
                keywordType : type
            }, true);
        });

    }

    var _sessionTimeoutWarning = function() {
        // I RUN ONCE A MINUTE
        // sessionTimeout in seconds
        if (parseInt(TDAR.common.currentTime)) {
        TDAR.common.currentTime += 60;
        var remainingTime = TDAR.common.sessionTimeout - TDAR.common.currentTime;
        console.log("remaining time in session:" + remainingTime);
        if (remainingTime == 300) {
            var dialog = $('<div id=timeoutDialog></div>')
                    .html(
                            "<B>Warning!</B><br/>Your session will timeout in 5 minutes, please save the document you're currently working on")
                    .dialog({
                        modal : true,
                        title : "Session Timeout Warning",
                        closeText : "Ok",
                        buttons : {
                            "Ok" : function() {
                                $(this).dialog("close");
                            }
                        }
                    });
        }
        if ($("#timeoutDialog").length != 0 && remainingTime <= 0) {
            $("#timeoutDialog")
                    .html(
                            "<B>WARNING!</B><BR>Your Session has timed out, any pending changes will not be saved");
        } else {
            setTimeout(TDAR.common.sessionTimeoutWarning, 60000);
        }
        }
    }
    /*
    function getBrowserMajorVersion() {
        var browserMajorVersion = 1;
        try {
            browserMajorVersion = parseInt($.browser.version);
        } catch (e) {
        }
        return browserMajorVersion;
    }
    */
    var _setupDocumentEditForm = function() {
        $(".doctype input[type=radio]").click(function() {_switchDocType(this);});
        _switchDocType($(".doctype input[type=radio]:checked"));
    }


    var _switchType = function(radio, container) {
        var type = $(radio).val().toLowerCase();

        console.debug('switchType:start:' + type);
        var $container = $(container);
        $(".typeToggle",$container).hide();
        $($("." + type) ,$container).show();

    }




    var _switchDocType = function(el) {
        var doctype = $(el).val().toLowerCase();

        console.debug('switchType:start:' + doctype);
        var $citeInfo = $("#citationInformation");
        $(".doctypeToggle",$citeInfo).hide();
        $($("." + doctype) ,$citeInfo).show();

        _switchLabel($("#publisher-hints"), doctype);
        _switchLabel($("#publisherLocation-hints"), doctype);

    }

    var _switchLabel = function(field, type) {
        // console.debug('_switchLabel('+field+','+type+')');
        $("label",field).text(field.attr(type));
    }

    var _toggleDiv = function() {
        $(this).next().slideToggle('slow');
        $(this).find("span.ui-icon-triangle-1-e").switchClass(
                "ui-icon-triangle-1-e", "ui-icon-triangle-1-s", 700);
        $(this).find("span.ui-icon-triangle-1-s").switchClass(
                "ui-icon-triangle-1-s", "ui-icon-triangle-1-e", 700);
    }

    var _setupSupportingResourceForm = function(totalNumberOfFiles, rtype) {
        // the ontology textarea or file upload field is required whenever it is
        // visible AND
        // no ontology rules are already present from a previous upload

        $('#fileInputTextArea').rules(
                "add",
                {
                    required : {
                        depends : isFieldRequired
                    },
                    messages : {
                        required : "No " + rtype + " data entered. Please enter "
                                + rtype + " manually or upload a file."
                    }
                });

        $('#fileUploadField').rules(
                "add",
                {
                    required : {
                        depends : isFieldRequired
                    },
                    messages : {
                        required : "No " + rtype
                                + " file selected. Please select a file or enter "
                                + rtype + " data manually."
                    }
                });

        function isFieldRequired(elem) {
            var noRulesExist = !((totalNumberOfFiles > 0)
                    || ($("#fileInputTextArea").val().length > 0) || ($(
                    "#fileUploadField").val().length > 0));
            return noRulesExist && $(elem).is(":visible");
        }

        _refreshInputDisplay();
    }

    /*
    function makeMap(json, mapId, type, value_) {
        var mapString = "";

        if (!json.chartshape) {
            alert("No map elements");
            return;
        }
        mapString = "<map name='" + mapId + "'>";
        var area = false;
        var chart = json.chartshape;
        var values = value_.split("|");
        for ( var i = 0; i < chart.length; i++) {
            area = chart[i];
            mapString += "\n  <area name='" + area.name + "' shape='" + area.type
                    + "' coords='" + area.coords.join(",");
            var val = values[i];

            // FIXME: I don't always consistently work
            // var offset = values.length - 1;
            // if (val == undefined && i >= offset && values[i-offset] != undefined)
            // {
            // val = values[(i-offset)];
            // }
            // console.log(values.length + ' ' + i + "{"+ (i -offset)+ "}" + ' ' +
            // values[(i-offset)]);
            if (val != undefined) {
                mapString += "' href='" + getURI("search/results") + "?" + type
                        + "=" + val + "&useSubmitterContext=true'";
            }
            mapString += " title='" + val + "'>";
            ;
        }
        mapString += "\n</map>";
        $("#" + mapId + "-img").after(mapString);
    }
    */
    var _registerDownload = function(url, tdarId) {
        if (typeof _gaq == 'undefined')
            return;
        var command = [ '_trackEvent', 'Download', url ];
        if (tdarId)
            command.push(tdarId);
        var errcount = _gaq.push(command);
        if (errcount) {
            console.warn("_trackEvent command failed for" + url);
        }
    }

    var _changeSubcategory = function(categoryIdSelect, subCategoryIdSelect) {
        var $categoryIdSelect = $(categoryIdSelect);
        var $subCategoryIdSelect = $(subCategoryIdSelect);
        $categoryIdSelect.siblings(".waitingSpinner").show();
        $.get(getBaseURI() + "resource/ajax/column-metadata-subcategories", {
            "categoryVariableId" : $categoryIdSelect.val()
        }, function(data_, textStatus) {
            var data = jQuery.parseJSON(data_);

            var result = "";
            for ( var i = 0; i < data.length; i++) {
                result += "<option value=\"" + data[i]['value'] + "\">"
                        + data[i]['label'] + "</option>\n";
            }

            $categoryIdSelect.siblings(".waitingSpinner").hide();
            $subCategoryIdSelect.html(result);
        });
    }

    

    var _getFunctionBody = function(func) {
        var m = func.toString().match(/\{([\s\S]*)\}/m)[1];
        return m;
    }

    // replace last occurance of str in attribute with rep
    function _replaceAttribute(elem, attrName, str, rep) {
        if (!$(elem).attr(attrName))
            return;
        var oldval = $(elem).attr(attrName);
        if (typeof oldval == "function") {
            oldval = _getFunctionBody(oldval);
            // console.debug("converting function to string:" + oldval );

        }
        if (oldval.indexOf(str) != -1) {
            var beginPart = oldval.substring(0, oldval.lastIndexOf(str));
            var endPart = oldval.substring(oldval.lastIndexOf(str) + str.length,
                    oldval.length);
            var newval = beginPart + rep + endPart;
            $(elem).attr(attrName, newval);
            // console.debug('attr:' + attrName + ' oldval:' + oldval + ' newval:' +
            // newval);
        }
    }

    var _refreshInputDisplay = function() {
        var selectedInputMethod = $('#inputMethodId').val();
        var showUploadDiv = (selectedInputMethod == 'file');
        $('#uploadFileDiv').toggle(showUploadDiv);
        $('#textInputDiv').toggle(!showUploadDiv);
    }

    /*
     * 
    function personAdded(id) {
//        console.log("person added " + id);
        $(".creatorInstitution", "#" + id).hide();
        $(".creatorPerson", "#" + id).show();
    }

    function institutionAdded(id) {
//        console.log("institution added " + id);
        // hide the person record
        $(".creatorPerson", "#" + id).hide();
        $(".creatorInstitution", "#" + id).show();
    }

     */
    // expand those nodes where children are selected
    function _switchLabel(field, type) {
        var label = "#" + $(field).attr('id') + '-label';
        if ($(field).attr(type) != undefined && $(label) != undefined) {
            $(label).text($(field).attr(type));
        }
    }




    /*
    function showTooltip(x, y, contents) {
        $('<div id="flottooltip">' + contents + '</div>').css({
            position : 'absolute',
            display : 'none',
            top : y + 30,
            left : x + 5
        }).appendTo("body").fadeIn(200);
    }
    */

    function _coordinatesCheckboxClicked(elem) {

        $('#explicitCoordinatesDiv').toggle(elem.checked);
    }
    
    $.extend(self, {
        "initEditPage": _initEditPage,
        "initFormValidation": _setupFormValidate,
        "applyTreeviews": _applyTreeviews,
        "initializeView": _initializeView,
        "getObjValue": _getObjValue,
        "initRegformValidation": _initRegformValidation,
        "determineResponsiveClass": _determineResponsiveClass,
        "elipsify":_elipsify,
        "populateTarget": _populateTarget,
        "prepareDateFields": _prepareDateFields,
        "setAdhocTarget": _setAdhocTarget,
        "changeSubcategory": _changeSubcategory ,
        "registerDownload": _registerDownload,
        "setupSupportingResourceForm": _setupSupportingResourceForm,
        "toggleDiv": _toggleDiv,
        "switchType": _switchType,
        "setupDocumentEditForm": _setupDocumentEditForm,
        "sessionTimeoutWarning": _sessionTimeoutWarning,
        "delegateCreator": _delegateCreator,
        "applyBookmarks":_applyBookmarks,
        "sprintf": _sprintf,
        "htmlDecode": _htmlDecode,
        "htmlEncode":_htmlEncode,
        "htmlDoubleEncode":_htmlDoubleEncode,
        "applyWatermarks": _applyWatermarks,
        "replaceAttribute": _replaceAttribute,
        "delayJiraButton": _delayJiraButton,
        "coordinatesCheckboxClicked": _coordinatesCheckboxClicked
    });
    
    return self;
}();

function checkWindowSize() {
    var width = $(window).width()
    var new_class = TDAR.common.determineResponsiveClass(width);
    $(document.body).removeClass('responsive-large-desktop responsive-desktop responsive-tablet responsive-phone responsive-phone-portrait').addClass(new_class);
}

/*
 * assigns a class to the body tag based on the current width.  These sizes match the bootstrap responsive grid sizes
 */
$(document).ready(function() {
    checkWindowSize();
    $(window).resize(checkWindowSize);
    if($.cookie("hide_jira_button")) {
        setTimeout(function(){$('#atlwdg-trigger').hide()}, 700);
    }

    TDAR.common.sessionTimeoutWarning();

    $(document).delegate(".bookmark-link","click",TDAR.common.applyBookmarks);


});


