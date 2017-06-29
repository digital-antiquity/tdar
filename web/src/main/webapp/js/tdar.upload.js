/* ASYNC FILE UPLOAD SUPPORT */

TDAR.fileupload = (function (TDAR, $) {
    "use strict";
    /* how long to display error messages prior to removing a failed file upload from the files table (in millis) */
    var ERROR_TIMEOUT = 5000;

    var _nextRowId = 0;
    var _nextRowVisibility = true;
    /**
     *
     * @type {{formSelector: string, inputSelector: string, fileuploadSelector: string, dropZone: string, pasteZone: string}}
     * @private
     */
    var _defaults = {
        //selector to the page's form
        formSelector: "#metadataForm",

        //selector to the actual file input element.
        inputSelector: "#fileAsyncUpload",

        //selector to the element that we will bind the fileupload widget to.  It can be any element so long as it contains the file input element and the files table.
        fileuploadSelector: "#divFileUpload",
        dropZone: "#divFileUpload",
        pasteZone: "#divFileUpload input[type=file]"
    }

    /**
     * Initialize a jQuery File Upload control on the form, using specified options.
     * @param options
     *      formSelector: selector to the form element
     *
     *      fileuploadSelector: selector to the element that we will bind the fileupload widget to.  It can be any
     *          element so long as it contains the file input element and the files table. (default:
     *                      #fileAsyncUpload)
     *      inputSelector: selector to the file input element (default:#fileAsyncUpload)
     *
     *      dropZone:  selector to a div that will receive file-drag-drop events (default: #divFileUpload)
     *
     *      pasteZone: selector that identifies the recipient of "paste" events
     *          (default: '#divFileUpload input[type=file]')
     *
     *
     * @returns {*} the element associated with the new file upload control
     * @private
     */
    var _registerUpload = function (options) {

        //FIXME: workaround for ie10 doubleclick bug.  remove this once fixed (https://github.com/blueimp/jQuery-File-Upload/issues/1180)
        var ie_version = _getIEVersion();
        if (ie_version.major == 10) {
            $('html').addClass("ie10")
        }

        //combine options w/ defaults
        var _options = $.extend(_defaults, options);

        //pass off our options to fileupload options (to allow easy passthrough of page-specific (options e.g. acceptFileTypes)
        var $fileupload = $(_options.fileuploadSelector).fileupload($.extend({
            formData: function () {
                var ticketId, data;
                ticketId = $('#ticketId').val();
                if (!ticketId) {
                    data = [
                        {name: "ticketRequested", value: "true"}
                    ]
                } else {
                    data = [
                        {name: "ticketId", value: $('#ticketId').val()}
                    ]
                }
                return data;

            },
            //send files in a single http request (singleFileUploads==false). See TDAR-2763 for more info      
            singleFileUploads: false,

            url: TDAR.uri('upload/upload'),
            autoUpload: true,
            maxNumberOfFiles: $(document).data("maxUploadFiles"),
            getNumberOfFiles: function () {
                return this.filesContainer.children().not(".deleted-file, .hidden, .replace-target").length;
            },
            destroy: _destroy

        }, _options));

        var $filesContainer = $fileupload.fileupload('option', 'filesContainer');

        $fileupload.bind('fileuploadpaste', function (e, data) {
            if (data.files.length == 0) {
                console.log("interpreting text paste instead of file");
                return true;
            }
            $.each(data.files, function (index, file) {
                console.log('Pasted file type: ' + file.type);
            });
        });
        $fileupload.bind("fileuploadcompleted", _updateReminder);
        //make sure the sequenceNumber field is correct after files are added (or the operation fails)
        var _updateSequenceNumbers = function (e, data) {
            //console.log("updating sequenceNumbers");
            $('tbody.files').find("tr").not(".replace-target,.deleted-file").each(function (idx, trElem) {
                $('.fileSequenceNumber', trElem).val(idx + 1);
            });
        }

        $fileupload.bind("fileuploadcompleted fileuploadfailed", _updateSequenceNumbers);

        //FIXME: break this out as a separate class so that it's easier to be seen by jsdoc
        /**
         * Helper object for the Fileupload control. We add the helper as 'data' property to both the form
         * of the current page as well as the fileupload input element.  The property is named 'fileuploadHelper'.
         * (e.g. "var helper = $("form").data("fileuploadHelper")
         * @type {*}
         */
        var helper = $.extend({}, _options, {
            /** reference back to fileupload widget's container element */
            context: $fileupload[0],

            updateFileAction: _updateFileAction,

            //list of existing and new files that are not deleted, serving as a file replacement, or that failed to upload properly
            validFiles: function () {
                /** {id:string, action:string, filename:string, sequence:number, ext:string, base:string, context:string}[] array of file objects */
                var $rows = $filesContainer.find('tr.template-download').not('.replace-target, .deleted-file, .hidden, .fileupload-error');
                var files = $rows.map(function () {
                    var file = {};
                    $(this).find('[type="hidden"]').each(function () {
                        file[$(this).attr("class")] = $(this).val();
                    });
                    file.context = $(this);
                    return file;
                }).get();
                TDAR.datepicker.applyHidden($(".new-file input.datepicker"));
                $(".new-file input.datepicker").change(function(){
                    _updateFileAction(this);
                });
                //translate property names and add extension
                files = $.map(files, function (file) {
                    var ext = file.fileReplaceName.substring(file.fileReplaceName.indexOf(".") + 1).toLowerCase();
                    return {
                        id: parseInt(file.fileId),
                        action: file.fileAction,
                        filename: file.fileReplaceName,
                        sequence: parseInt(file.fileSequenceNumber),
                        ext: ext,
                        base: file.fileReplaceName.substr(0, file.fileReplaceName.length - ext.length - 1),
                        context: file.context
                    }
                });
                return files;
            }
        });

        //add reference to helper object  to form and fileupload widget container
        $(_options.formSelector).add(_options.fileuploadSelector).data('fileuploadHelper', helper);

        _registerReplaceButton(_options.fileuploadSelector);

        //update the proxy action if user updates fileproxy metadata
        $filesContainer.on("change", "select,textarea,input[type=text],.datepicker", function (e) {
            console.log("update file action fired");
            _updateFileAction(this);
        });
        
        TDAR.datepicker.bind($("input.datepicker",$filesContainer));


        
        $(_options.fileuploadSelector).bind("fileuploadcompleted", function (e, data) {
            var $datefields = $(data.context).find(".date");
            _applyDateInputs($datefields);
            $(this).trigger("heightchange");
        });

        $(_options.fileuploadSelector).bind("fileuploaddone", function (e, data) {
            if (!data.result) {
                return;
            }
            if (!data.result.ticket) {
                return;
            }
            if (!data.result.ticket.id) {
                console.log("no ticket in results");
            } else {
                var ticket = data.result.ticket;
                console.log("ticket received: %s", JSON.stringify(ticket));
                $("#ticketId").val(ticket.id);
            }
        });

        //When an upload fails, fileupload plugin displays error information in the file row message.  In addition, we need
        //to remove/disable the fileproxy form fields associated with this upload so they aren't sent in the request upon submit
        $fileupload.bind("fileuploadfailed", function (e, data) {
            var $row = data.context;
            $row.find("input, select, button").prop("disabled", true).addClass("disabled");

            //Keep the row around for a few seconds so that the user can see the error information,  then remove the row
            setTimeout(function () {
                        $row.fadeOut("slow", function () {
                            $row.remove();
                        })
                    }, ERROR_TIMEOUT);

            //clear the previous input value, otherwise repeated attempt to upload same file will not trigger change event
            $(this).find('input[type=file]')
                .val('') //resets IE file input
                .val(null); //resets file input for other browsers
        });

        //pre-populate the files table with any previously-uploaded files
        if (TDAR.filesJson) {
            var files = _translateIrFiles(TDAR.filesJson);
            // console.debug("files.length: %s", files.length);
            // remove all of the pre-loaded proxies ahead of replacing them with their respective proxy versions
            if (files.length) {
                $("#fileProxyUploadBody").empty();
            }
            //fake out the fileupload widget to render our previously uploaded files by invoking it's 'uploadComplete' callback
            $fileupload.fileupload('option', 'done').call($fileupload[0], null, {result: {"files": files}});

            //any dropdown boxes in the new row have no selected value, so we update it here manually.
            $filesContainer.find("select.fileProxyConfidential").each(function () {
                var restriction = $(this).attr("datarestriction");
                if (restriction) {
                    $(this).val(restriction);
                }
            });
        }

        // disable submit/upload buttons during file transfer (TDAR-3884);
        $fileupload.bind("fileuploadsent", function () {
            console.log("disabling stuff");
            $(".submitButton, #fileAsyncUpload").prop("disabled", true);
            $(".fileinput-button").addClass("disabled");

            //re-enable when complete regardless of outcome
        }).bind("fileuploadfinished", function () {
            $(".submitButton, #fileAsyncUpload").prop("disabled", false);
            $(".fileinput-button").removeClass("disabled");
            console.log("enabling stuff");
        });

        return helper;
    };

    /**
     * update file proxy action to indicate to the server that we have modified metadata for this file proxy
     *
     * @param elemInRow element contained by the row
     * @private
     */
    var _updateFileAction = function (elemInRow) {
        console.log("updateFileAction(%s)", elemInRow);
        var $hdnAction = $(elemInRow).closest("tr").find(".fileAction");
        if ($hdnAction.val() === "NONE") {
            $hdnAction.val("MODIFY_METADATA");
        }
    }

    /**
     * convert json returned from tDAR into something understood by upload-plugin, as well as fileproxy fields to send back to server
     *
     * @param fileProxies elements that contain file-proxy  input fields
     * @returns {*} list of "file" objects
     * @private
     */
    var _translateIrFiles = function (fileProxies) {
        return $.map(fileProxies, function (proxy, i) {
            var file = $.extend({
                name: proxy.filename,
                url: TDAR.uri("filestore/" + proxy.originalFileVersionId),
                thumbnail_url: null,
                delete_url: null,
                delete_type: "DELETE",
                description: proxy.description,
                fileCreatedDate: ""
            }, proxy);
            if (proxy.fileCreatedDate) {
                var date = new Date(proxy.fileCreatedDate);
                var year = date.getYear() - 100;
                var month = date.getMonth() + 1;
                var day = date.getDate();                
                file.fileCreatedDate = month + "/" + day + "/" + year;
            }
            return file;
        });
    };

    /**
     * custom _destroy function that overrides the fileupload plugin's "destroy" handler.  Instead of removing the row,  we
     * this handler toggles the "deleted" state of the file-proxy and updates.  When "deleted", we change the file
     * proxy action to "delete" and replace the delete button in the row with an "undelete" button. conversely, if the
     * row is already in the 'deleted' state, we replace the undelete button with a delete button and revert the
     * file proxy action to the value it had prior to being "deleted"
     * @param e
     * @param data
     * @private
     */
    var _destroy = function (e, data) {
        // data.context: download row,
        // data.url: deletion url,
        // data.type: deletion request type, e.g. "DELETE",
        // data.dataType: deletion response type, e.g. "json"

        var $row = data.context;
        //If pre-existing, tell server to delete the file.  If we just sent it, tell server to ignore it. 
        var newUpload = parseInt($row.find(".fileId").val()) <= 0;
        var $btnDelete = $("button.delete-button", data.context);
        var $hdnAction = $(".fileAction", data.context);
        if ($btnDelete.data("type") === "DELETE") {
            $hdnAction.data("prev", $hdnAction.val());
            $hdnAction.val(newUpload ? "NONE" : "DELETE");
            $(data.context).addClass("deleted-file");
            $btnDelete.data("type", "UNDELETE");
            //TODO: make row look "deleted", disable everything except 'undelete' button
            _disableRow($row);
        } else {
            //re-enable row appearance and change label back to previous state
            $hdnAction.val($hdnAction.data("prev"));
            $(data.context).removeClass("deleted-file");
            $btnDelete.data("type", "DELETE");
            _enableRow($row);
        }
        //show the correct button label
        $("span", $btnDelete).html({
            "DELETE": "Delete",
            "UNDELETE": "Undelete"
        }[$btnDelete.data("type")]);

        //console.log("destroy called. context:%s", data.context[0]);
    };

    //FIXME: this could/should be replaced with a "suggestion" validation rule
    /**
     * If the the user has not uploaded any files,  show the "Don't forget to add some files" reminder.
     * @param e
     * @param data
     * @private
     */
    var _updateReminder = function (e, data) {
        var $filesTable = $(data.context).closest("tbody.files");
        // console.debug("$filesTable.length: %s", $filesTable.length);
        if ($filesTable.length > 0) {
            $("#reminder").hide();
        } else {
            $("#reminder").show();
        }
    };

    /**
     * Enable form elements in the specified row
     * @param row
     * @private
     */
    var _enableRow = function (row) {
        $('button:not(.delete-button), select', row).prop('disabled', false);
        $('.delete-button', row).removeClass('btn-warning').addClass('btn-danger');
    };

    /**
     * Disable the form elements in  the specified table row
     * @param row
     * @private
     */
    var _disableRow = function (row) {
        $('button:not(.delete-button), select', row).prop('disabled', true);
        $('.delete-button', row).addClass('btn-warning').removeClass('btn-danger');
    };

    /**
     * kludge for dealing w/ fileupload's template system, which doesn't have a good way to pass the row number of the table the template will be rendered to
     * @private
     */
    var _getRowId = function () {
        return _nextRowId++;
    }

    /**
     * another kludge:  indicate to fileupload-ui template that we don't want uploaded file to appear in files section (e.g. when the user is using the "replace file" feature).
     * @private
     */
    var _getRowVisibility = function () {
        return _nextRowVisibility;
    }

    /**
     * Update the file proxy  fields in the specified rows to indicate that one file (the original) is to be replaced
     * by another file (the target)
     *
     * @param $originalRow row containing the file user wishes to replace
     * @param $targetRow row containing the replacement file
     * @private
     */
    var _replaceFile = function ($originalRow, $targetRow) {
        var targetFilename = $targetRow.find("input.fileReplaceName").val();
        var originalFilename = $originalRow.find("input.fileReplaceName").val();
        $originalRow.find('.replacement-text').text("Will be replaced by " + targetFilename + " upon clicking 'Save'");
        $originalRow.find('.fileReplaceName').val(targetFilename);
        $originalRow.find('.fileReplaceName').data("original-filename", originalFilename);

        //Change action to "REPLACE", and store original in event of cancel
        var $fileAction = $originalRow.find('.fileAction');
        $fileAction.data('original-fileAction', $fileAction.val());
        $fileAction.val("REPLACE");

        //store the replacement row in case we need to cancel this operation later
        $targetRow.detach();
        $originalRow.data("$targetRow", $targetRow);
        $originalRow.find(".replace-file-button, .undo-replace-button").toggle();
    }

    /**
     * Revert the state file proxy fields that were modified from a previous call to _replaceFile()
     * @param $row row that served as the originalRow argument in the prior call to _repaceFile()
     * @private
     */
    var _cancelReplaceFile = function ($row) {
        var $fileReplaceName = $row.find(".fileReplaceName");
        var $fileAction = $row.find(".fileAction");
        var $replacementText = $row.find(".replacement-text");

        //restore the old name and action
        $fileReplaceName.val($fileReplaceName.data("original-filename"));
        $fileAction.val($fileAction.data("original-fileAction"));
        $replacementText.text("");

        //create proxy for the former replacefile
        var $targetRow = $row.data("$targetRow");
        $targetRow.find(".fileAction").val("NONE");
        $("#cancelledProxies").append($targetRow);

        $row.find(".replace-file-button, .undo-replace-button").toggle();
    }

    /**
     * Register all the necessary listeners/handlers for the "Replace" button in a file upload row.
     * @param fileuploadSelector
     * @private
     */
    var _registerReplaceButton = function (fileuploadSelector) {

        //console.debug("registering replace button")

        //invoke the fileupload widget's "send" method
        $(fileuploadSelector).on("change", ".replace-file", function (e) {
            console.debug("triggering file upload");
            var $elem = $(this);

            //tell filupload-ui to hide this upload from files table
            _nextRowVisibility = false;

            var $replaceTarget = $(this).closest(".existing-file");

            //temporarily omit the target when calculating file count (so we can replace a file even when we are at max upload cap)
            $replaceTarget.addClass("replace-target");

            $(fileuploadSelector).fileupload('send', {
                files: e.target.files || [
                    {name: this.value}
                ],
                fileInput: $(this),
                $replaceTarget: $replaceTarget,
                ticketId: $('#ticketId').val()
            });

            $replaceTarget.removeClass("replace-target");

            //disable replace 'button' during transfer (except if we are doing iframe-based transport, which will not
            // work with disabled elements)
            if (e.target.files) {
                $elem.prop("disabled", true);
            }

        });

        //when browser uploads replacement file uploaded succesfully, update file proxy fields to indicate incoming file is replacement
        $(fileuploadSelector).bind("fileuploadcompleted", function (e, data) {
            if (!data.$replaceTarget) {
                return;
            }
            var file = data.files[0];
            var $originalRow = data.$replaceTarget;
            var $targetRow = $(data.context);
            _replaceFile($originalRow, $targetRow);
        });

        //regardless of success/failure,  perform this cleanup after replacement upload terminates
        $(fileuploadSelector).bind("fileuploadfinished", function () {
            //tell filupload-ui to stop hiding uploads
            _nextRowVisibility = true;
            $(fileuploadSelector).find(".replace-file").prop("disabled", false);
        });

        //
        $(fileuploadSelector).on("click", ".undo-replace-button", function (e) {
            console.log("undo replace click");
            _cancelReplaceFile($(this).closest(".existing-file"));
        });
    }

    var _applyDateInputs = function ($elements) {
        TDAR.datepicker.apply($elements);
    }

    /**
     * kludge that gets us the IE version (if user-agent is IE).  This is used in conjunction with the
     * double-click button label workarond  (in IE11, the upload button must be double-clicked, so we change the
     * button label from "Upload a file" to  "Double-click to Upload"
     * @returns {*}  object with major: and minor: version numbers
     * @private
     */
    function _getIEVersion() {
        var agent = navigator.userAgent;
        var reg = /MSIE\s?(\d+)(?:\.(\d+))?/i;
        var matches = agent.match(reg);
        if (matches != null) {
            return { major: matches[1], minor: matches[2] };
        }
        return { major: "-1", minor: "-1" };
    }

    //expose public elements
    return {
        "registerUpload": _registerUpload,
        "updateFileAction": _updateFileAction,
        "getRowId": _getRowId,
        "getRowVisibility": _getRowVisibility
    };
})(TDAR, jQuery);
