/* ASYNC FILE UPLOAD SUPPORT */

TDAR.namespace("fileupload");

TDAR.fileupload = function() {
    'use strict';
    

    var _nextRowId = 0;
    var _nextRowVisibility = true;


    //main file upload registration function
    var _registerUpload  = function(options) {

        //FIXME: workaround for ie10 doubleclick bug.  remove this once fixed (https://github.com/blueimp/jQuery-File-Upload/issues/1180)
        var ie_version =  _getIEVersion();
        if(ie_version.major == 10) {
            $('html').addClass("ie10")
        }

        //combine options w/ defaults
        var _options = $.extend({formSelector: "#resourceMetadataForm"}, options);

        //pass off our options to fileupload options (to allow easy passthrough of page-specific (options e.g. acceptFileTypes)
        var $fileupload = $(_options.inputSelector).fileupload($.extend({
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
                return this.filesContainer.children().not(".deleted-file, .hidden").length;
            },
            destroy: _destroy

        }, _options));

        var $filesContainer = $fileupload.fileupload('option', 'filesContainer');
        
        $fileupload.bind("fileuploadcompleted", _updateReminder);
        //make sure the sequenceNumber field is correct after files are added (or the operation fails)
        var _updateSequenceNumbers =  function(e, data){
            //console.log("updating sequenceNumbers");
            $('tbody.files').find("tr").not(".replace-target,.deleted-file").each(function(idx, trElem){
                //console.log("updating sequencenumber::   row.id:%s   sequenceNumber:%s", trElem.id, idx+1);
                $('.fileSequenceNumber', trElem).val(idx + 1);
            });
        }
        
        //note: unlike in jquery.bind(), you cant use space-separated event names here.
        $fileupload.bind("fileuploadcompleted fileuploadfailed", _updateSequenceNumbers);

        var helper = {
                //reference back to fileupload widget's container element
                context: $fileupload[0],
                updateFileAction: _updateFileAction,
                inputSelector: _options.inputSelector,
                
                //list of existing and new files that are not deleted or serving as a file replacement
                //FIXME: needs to not include files that were uploaded but failed part way.
                validFiles: function() {
                    var $rows = $filesContainer.find('tr.template-download').not('.replace-target, .deleted-file, .hidden');
                    
                    var files = $rows.map(function(){
                        var file = {};
                        $(this).find('[type=hidden]').each(function(){
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
        };
        
        //add reference to helper object  to form and inputFile
        $(_options.formSelector).add(_options.inputSelector).data('fileuploadHelper', helper);

        _registerReplaceButton(_options.formSelector);

        //update the proxy action if user updates fileproxy metadata
        $filesContainer.on("change", "select,textarea,input[type=text],input[type=date]", function(e) {
            _updateFileAction(this);
        });
        
        $(_options.formSelector).bind("fileuploadcompleted", function(e, data) {
            var $datefields = $(data.context).find(".date");
            _applyDateInputs($datefields);
            
        });
        
        $(_options.formSelector).bind("fileuploaddone", function(e, data) {
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
        $originalRow.find('.replacement-text').text("will be replaced by " + targetFilename + ")");
        $originalRow.find('.fileReplaceName').val(targetFilename);
        $originalRow.find('.fileReplaceName').data("original-filename", originalFilename);

        //Change action to "REPLACE", and store original in event of cancel
        var $fileAction = $originalRow.find('.fileAction');
        $fileAction.data('original-fileAction', $fileAction.val());
        $fileAction.val("REPLACE");

        //store the replacement row in case we need to cancel this operation later
        $targetRow.fadeOut(function() {
            $targetRow.detach();
            $originalRow.data("$targetRow", $targetRow);
            $originalRow.find(".replace-file-button, .undo-replace-button").toggle();
        });
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

    var _registerReplaceButton = function(formSelector) {

        console.log("registering replace button")

        //invoke the fileupload widget's "send" method
        //FIXME: this would be more efficient if we passed the specific div that holds the upload section (instead of entire form)
        $(formSelector).on('change', '.replace-file' , function (e) {
            console.log("triggering file upload");

            //tell filupload-ui to hide this upload from files table
            _nextRowVisibility = false;

            $(formSelector).fileupload('send', {
                files: e.target.files || [{name: this.value}],
                fileInput: $(this),
                $replaceTarget: $(this).closest(".existing-file")
            });
        });

        //when browser uploads replacement file uploaded succesfully, update file proxy fields to indicate incoming file is replacement
        $(formSelector).bind("fileuploadcompleted", function(e, data) {
            if(!data.$replaceTarget) return;
            var file = data.files[0];
            var $originalRow = data.$replaceTarget;
            var $targetRow = $(data.context);
            _replaceFile($originalRow, $targetRow);
        });

        //regardless of success/failure,  perform this cleanup after replacement upload terminates
        $(formSelector).bind("fileuploadfinished", function() {
            //tell filupload-ui to stop hiding uploads
            _nextRowVisibility = true;
        });

        //
        $(formSelector).on("click", ".undo-replace-button", function(e) {
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