/* ASYNC FILE UPLOAD SUPPORT */

TDAR.namespace("fileupload");

TDAR.fileupload = function() {
    'use strict';
    
    /* how long to display error messages prior to removing a failed file upload from the files table (in millis) */
    var ERROR_TIMEOUT = 5000;

    var _nextRowId = 0;
    var _nextRowVisibility = true;
    var _defaults = {
        //selector to the page's form
        formSelector: "#metadataForm",

        //selector to the actual file input element.
        inputSelector: "#fileAsyncUpload",

        //selector to the element that we will bind the fileupload widget to.  It can be any element so long as it contains the file input element and the files table.
        fileuploadSelector: "#divFileUpload",
        dropZone:  "#divFileUpload",
        pasteZone:  "#divFileUpload input[type=file]"
    }

    //main file upload registration function
    var _registerUpload  = function(options) {

        //FIXME: workaround for ie10 doubleclick bug.  remove this once fixed (https://github.com/blueimp/jQuery-File-Upload/issues/1180)
        var ie_version =  _getIEVersion();
        if(ie_version.major == 10) {
            $('html').addClass("ie10")
        }

        //combine options w/ defaults
        var _options = $.extend(_defaults, options);

        //pass off our options to fileupload options (to allow easy passthrough of page-specific (options e.g. acceptFileTypes)
        var $fileupload = $(_options.fileuploadSelector).fileupload($.extend({
            formData: function(){
                var ticketId, data;
                ticketId = $('#ticketId').val();
                if(!ticketId) {
                    data = [{name:"ticketRequested", value: "true"}]
                } else {
                    data = [{name:"ticketId", value:$('#ticketId').val()}]
                }
                return data;
                
            },
            //send files in a single http request (singleFileUploads==false). See TDAR-2763 for more info      
            singleFileUploads: false,

            url: TDAR.uri('upload/upload'),
            autoUpload: true,
            maxNumberOfFiles: TDAR.maxUploadFiles,
            getNumberOfFiles: function() {
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
        var _updateSequenceNumbers =  function(e, data){
            //console.log("updating sequenceNumbers");
            $('tbody.files').find("tr").not(".replace-target,.deleted-file").each(function(idx, trElem){
                $('.fileSequenceNumber', trElem).val(idx + 1);
            });
        }
        
        $fileupload.bind("fileuploadcompleted fileuploadfailed", _updateSequenceNumbers);

        var helper = $.extend({}, _options, {
                //reference back to fileupload widget's container element
                context: $fileupload[0],
                updateFileAction: _updateFileAction,

                //list of existing and new files that are not deleted or serving as a file replacement
                //FIXME: needs to not include files that were uploaded but failed part way.
                validFiles: function() {
                    var $rows = $filesContainer.find('tr.template-download').not('.replace-target, .deleted-file, .hidden, .fileupload-error');
                    
                    var files = $rows.map(function(){
                        var file = {};
                        $(this).find('[type="hidden"]').each(function(){
                            file[$(this).attr("class")] = $(this).val();
                        });
                        file.context = $(this);
                        return file;
                    }).get();
                    
                    //translate property names and add extension
                    files = $.map(files, function(file){
                        var ext = file.fileReplaceName.substring(file.fileReplaceName.indexOf(".") + 1).toLowerCase();
                        return {
                            id: parseInt(file.fileId),
                            action: file.fileAction,
                            filename: file.fileReplaceName,
                            sequence: parseInt(file.fileSequenceNumber),
                            ext:  ext,
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
        $filesContainer.on("change", "select,textarea,input[type=text],input[type=date]", function(e) {
            _updateFileAction(this);
        });
        
        $(_options.fileuploadSelector).bind("fileuploadcompleted", function(e, data) {
            var $datefields = $(data.context).find(".date");
            _applyDateInputs($datefields);
            $(this).trigger("heightchange");
        });
        
        $(_options.fileuploadSelector).bind("fileuploaddone", function(e, data) {
            if(!data.result) return;
            if(!data.result.ticket) return;
            if(!data.result.ticket.id)  {
                console.log("no ticket in results"); 
            } else {
                var ticket = data.result.ticket;
                console.log("ticket received: %s", JSON.stringify(ticket));
                $("#ticketId").val(ticket.id);
            }
        });

        //When an upload fails, fileupload plugin displays error information in the file row message.  In addition, we need 
        //to remove/disable the fileproxy form fields associated with this upload so they aren't sent in the request upon submit
        $fileupload.bind("fileuploadfailed", function(e, data){
            var $row = data.context;
            //$row.find("input, select").remove();
            //a delete button probably doesn't help us either.
            //$row.find("button").remove();

            //Keep the row around for a few seconds so that the user can see the error information,  then remove the row
            setTimeout(
                function(){
                    $row.fadeOut("slow", function(){$row.remove()})
                }, 
                ERROR_TIMEOUT
            );
        });

        //pre-populate the files table with any previously-uploaded files
        if(TDAR.filesJson) {
            var files = _translateIrFiles(TDAR.filesJson);
            console.log("files.length: %s", files.length);
            // remove all of the pre-loaded proxies ahead of replacing them with their respective proxy versions
            if (files.length) {
                $("#fileProxyUploadBody").empty();
            }
            //fake out the fileupload widget to render our previously uploaded files by invoking it's 'uploadComplete' callback
            $fileupload.fileupload('option', 'done').call($fileupload[0], null, {result: {"files":files}});

            //any dropdown boxes in the new row have no selected value, so we update it here manually.
            $filesContainer.find("select.fileProxyConfidential").each(function(){
                var restriction = $(this).attr("datarestriction");
                if(restriction) $(this).val(restriction);
            });
        }

        //disable submit button during file transfer;
        var $submitButtons = $(".submitButton");
        $fileupload.bind("fileuploadsent", function() {
            $submitButtons.prop("disabled", true)

        //re-enable when complete regardless of outcome
        }).bind("fileuploadfinished", function() {
               $submitButtons.prop("disabled", false);
        });

        return helper;
    };
    
    //update file proxy actionto indicate that the values have changed
    var _updateFileAction = function(elemInRow) {
        console.log("updateFileAction(%s)", elemInRow);
        var $hdnAction = $(elemInRow).closest("tr").find(".fileAction");
        if($hdnAction.val()==="NONE") {
            $hdnAction.val("MODIFY_METADATA");
        }
    }

    //convert json returned from tDAR into something understood by upload-plugin, as well as fileproxy fields to send back to server
    var _translateIrFiles = function(fileProxies) {
        return $.map(fileProxies, function(proxy, i) {
            var file = $.extend({
                name: proxy.filename,
                url: TDAR.uri("filestore/" + proxy.originalFileVersionId),
                thumbnail_url: null, 
                delete_url: null,
                delete_type: "DELETE",
                description: proxy.description,
                fileCreatedDate: ""
            }, proxy);
            if(proxy.fileCreatedDate) {
                file.fileCreatedDate = $.datepicker.formatDate("mm/dd/yy", new Date(proxy.fileCreatedDate))                
            }
            return file;
        });
    };

    //mark a file as deleted/undeleted and properly set appearance and fileproxy state
    var _destroy = function(e, data){
        // data.context: download row,
        // data.url: deletion url,
        // data.type: deletion request type, e.g. "DELETE",
        // data.dataType: deletion response type, e.g. "json"
        
        var $row = data.context;
        //If pre-existing, tell server to delete the file.  If we just sent it, tell server to ignore it. 
        var newUpload = parseInt($row.find(".fileId").val()) <= 0;
        var $btnDelete = $("button.delete-button", data.context);
        var $hdnAction = $(".fileAction", data.context);
        if($btnDelete.data("type") === "DELETE") {
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
        }[$btnDelete.data("type")] );
               
        
        console.log("destroy called. context:%s", data.context[0]);
    };

    //TODO: replace this with a custom event
    var _updateReminder = function(e, data) {
        console.log("_updateReminder")
        var $filesTable = $(data.context).closest("tbody.files");
        console.log($filesTable.length);
        if ($filesTable.length > 0) {
        	$("#reminder").hide();
        } else {
        	$("#reminder").show();
        }
    };

    var _enableRow = function(row) {
        $('button:not(.delete-button), select', row).prop('disabled', false);
        $('.delete-button', row).removeClass('btn-warning').addClass('btn-danger');
    };
    
    var _disableRow = function(row) {
        $('button:not(.delete-button), select', row).prop('disabled', true);
        $('.delete-button', row).addClass('btn-warning').removeClass('btn-danger');
    };
    
    // kludge for dealing w/ fileupload's template system, which doesn't have a good way to pass the row number of the table the template will be rendered to
    var _getRowId = function() {
        return _nextRowId++;
    }

    /**
     * another kludge:  indicate to fileupload-ui template that we don't want uploaded file to appear in files section
     * (e.g. when the user is using the "replace file" feature).
     */
    var _getRowVisibility = function() {
        return _nextRowVisibility;
    }

    var _replaceFile = function($originalRow, $targetRow) {
        var targetFilename = $targetRow.find("input.fileReplaceName").val();
        var originalFilename = $originalRow.find("input.fileReplaceName").val();
        $originalRow.find('.replacement-text').text("will be replaced by " + targetFilename + " upon clicking 'Save')");
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

    //to 'cancel' a file replacement, we need to restore state of the fileproxy,  and then create a new file proxy
    //telling the server to ignore the replacement file.
    var _cancelReplaceFile = function($row) {
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

    var _registerReplaceButton = function(fileuploadSelector) {

        console.log("registering replace button")

        //invoke the fileupload widget's "send" method
        //FIXME: this would be more efficient if we passed the specific div that holds the upload section (instead of entire form)
        $(fileuploadSelector).on("change", ".replace-file" , function (e) {
            console.log("triggering file upload");
            var $elem = $(this);

            //tell filupload-ui to hide this upload from files table
            _nextRowVisibility = false;

            var $replaceTarget =  $(this).closest(".existing-file");

            //temporarily omit the target when calculating file count (so we can replace a file even when we are at max upload cap)
            $replaceTarget.addClass("replace-target");

            $(fileuploadSelector).fileupload('send', {
                files: e.target.files || [{name: this.value}],
                fileInput: $(this),
                $replaceTarget: $replaceTarget,
                ticketId:$('#ticketId').val()
            });

            $replaceTarget.removeClass("replace-target");
            $elem.prop("disabled", true);

        });

        //when browser uploads replacement file uploaded succesfully, update file proxy fields to indicate incoming file is replacement
        $(fileuploadSelector).bind("fileuploadcompleted", function(e, data) {
            if(!data.$replaceTarget) return;
            var file = data.files[0];
            var $originalRow = data.$replaceTarget;
            var $targetRow = $(data.context);
            _replaceFile($originalRow, $targetRow);
        });

        //regardless of success/failure,  perform this cleanup after replacement upload terminates
        $(fileuploadSelector).bind("fileuploadfinished", function() {
            //tell filupload-ui to stop hiding uploads
            _nextRowVisibility = true;
            $(fileuploadSelector).find(".replace-file").prop("disabled", false);
        });

        //
        $(fileuploadSelector).on("click", ".undo-replace-button", function(e) {
            console.log("undo replace click");
            _cancelReplaceFile($(this).closest(".existing-file"));
        });
    }
    
    var _applyDateInputs = function($elements) {
        $elements.datepicker({dateFormat: "mm/dd/yy"});
    }

    function _getIEVersion(){
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
}();