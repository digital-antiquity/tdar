/* ASYNC FILE UPLOAD SUPPORT */

TDAR.namespace("fileupload");

TDAR.fileupload = function() {
    'use strict';
    
    var _informationResource;
    var _informationResourceId = -1;
    var _nextRowId = 0;
    
    //main file upload registration function
    var _registerUpload  = function(options) {
        
        //combine options w/ defaults
        var _options = $.extend({formSelector: "#resourceMetadataForm"}, options);
        
        
        //pass off our options to fileupload options (to allow easy passthrough of page-specific (options e.g. acceptFileTypes)
        var $fileupload = $(_options.formSelector).fileupload($.extend({
//            add: function(e, data) {
//                data.submit(); //upload after file selected
//            },
            formData: function(){
                return [{name:"ticketId", value:$('#ticketId').val()}]
            },
            singleFileUploads: false,
            url: TDAR.uri('upload/upload'),
            autoUpload: true,
            maxNumberOfFiles: 50,
            destroy: _destroy
        }, _options));
        
        
        $fileupload.bind("fileuploadcompleted", _updateReplaceButtons);
        
        //make sure the sequenceNumber field is correct after files are added (or the operation fails)
        var _updateSequenceNumbers =  function(e, data){
            console.log("updating sequenceNumbers");
            $('tbody.files').find("tr").not(".replace-target,.deleted-file").each(function(idx, trElem){
                console.log("updating sequencenumber::   row.id:%s   sequenceNumber:%s", trElem.id, idx+1);
                $('.fileSequenceNumber', trElem).val(idx + 1);
            });
        }
        
        //note: unlike in jquery.bind(), you cant use space-separated event names here.
        $fileupload.bind("fileuploadcompleted fileuploadfailed", _updateSequenceNumbers);
        
        //hack: disable until we have a valid ticket
        //$fileupload.fileupload("disable");
        
        $.post(TDAR.uri("upload/grab-ticket"), function(data) {
            $('#ticketId').val(data.id);
            //$fileupload.fileupload('enable');
        }, 'json');
        
        //populate list of peviously uploaded files,  if available.
        if(_options.informationResourceId) {
            _informationResourceId = _options.informationResourceId;
            $.ajax(
                    {
                        url: TDAR.uri("upload/list-resource-files"), 
                        data: {informationResourceId: _options.informationResourceId},
                        success: function(data){
                            //FIXME: if there's an exception in this method, it gets eaten
                            var files = _translateIrFiles(data);
                            console.log("files.length: %s", files.length);
                            // remove all of the pre-loaded proxies ahead of replacing them with their respective proxy versions
                            if (files.length) {
                                $("#fileProxyUploadBody").empty();
                            }
                            $fileupload.fileupload('option', 'done').call($fileupload[0], null, {result: files});
                        },
                        error: function(jqxhr, textStatus, errorThrown) {
                            console.error("textStatus:%s    error:%s", textStatus, errorThrown);
                        },
                        dataType: 'json'
                    });
        }
        
        //dynamically generate the replace-with dropdown list items with the the candidate replacement files
        $fileupload.on("click", "button.replace-button", _buildReplaceDropdown);
        
        console.log("register() done")
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
            return {
                name: proxy.filename,
                size: proxy.size,
                url: TDAR.uri("filestore/" + proxy.originalFileVersionId),
                thumbnail_url: null, //TODO
                delete_url: null,
                delete_type: "DELETE", //required, no purpose (future use?)
                action: proxy.action,
                fileId: proxy.fileId
            };
        });
    };

    //mark a file as deleted/undeleted and properly set appearance and fileproxy state
    var _destroy = function(e, data){
        // data.context: download row,
        // data.url: deletion url,
        // data.type: deletion request type, e.g. "DELETE",
        // data.dataType: deletion response type, e.g. "json"
        
        var $row = data.context;
        var newUpload = TDAR.fileupload.informationResourceId === -1;
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
            "DELETE": locale.fileupload.destroy,
            "UNDELETE": "Undelete"
        }[$btnDelete.data("type")] );
               
        
        _updateReplaceButtons(e, data);
        console.log("destroy called. context:%s", data.context[0]);
    };
    
    
    //if there are any newfile rows,  enable all the replace buttons
    var _updateReplaceButtons = function(e, data) {
        console.log("_updateReplaceButtons")
        var $filesTable = $(data.context).closest("tbody.files");
        var $newfileRows = $('.new-file:not(.replace-target,.deleted-file)', $filesTable);
        
        //if there are new files in the uploaded queue that arent already replace targets we can  enable all the replace buttons
        if($newfileRows.length) {
            //TODO: create simple jquery plugin .enable() and .disable()  that takes care of class + property in one shot.
            $('button.replace-button', $filesTable).removeClass("disabled").prop("disabled", false);
        } else {
            $('button.replace-button', $filesTable).addClass("disabled").prop("disabled", true);
        }
    };
    
    //dynamically generate the replace-with dropdown list items with the the candidate replacement files
    var _buildReplaceDropdown = function(e) {
        var button = this;
        var $ul = $(button).next(); //in a button dropdown, the ul follows the button
        var $tr = $(button).parents("tr.existing-file");
        var $tbody = $(button).closest("tbody.files");
        var $newfiles = $('.new-file:not(.replace-target,.deleted-file)');
        var data = {
                jqnewfiles: $newfiles,
                //TODO: figure  out if this existing file has already chosen a replacement, if so,  render a "cancel" option.
                bReplacePending: $tr.hasClass('replacement-selected')};
        
        var $listItems = $(tmpl("template-replace-menu", data));
        $listItems.find('a').bind("click", _replacementFileItemClicked);
        $ul.empty();
        $ul.append($listItems);
    };

    //"replacement file chosen" event handler.  update the hidden replacement filename field of the replacement file row, and mark target as selected
    var _replacementFileItemClicked = function(e) {
        //FIXME: I don't think preventDefault should be necessary, but browser follows href ("#", e.g. scrolls to top of page) unless I do.
        e.preventDefault();  
        
        var $anchor = $(this);
        var $tr = $anchor.parents(".existing-file");
        var $hidden = $tr.find('.fileReplaceName');
        var $target =  $($anchor.data("target"));
        if(!$anchor.hasClass("cancel")) {
            if($target.data('jqOriginalRow')) {
                //if this replace operation overwrites a pending replace, cancel the pending replace first.
                _cancelFileReplace($target.data('jqOriginalRow'), $target);
            }
            _replaceFile($tr, $target);
        } else {
            //the 'cancel' link doesn't have a data-target attr; but we did add a reference to the target in the original
            _cancelFileReplace($tr, $tr.data("jqTargetRow"));
        }
    };
    
    var _replaceFile = function($originalRow, $targetRow) {
        var targetFilename = $targetRow.find("input.fileReplaceName").val();
        var originalFilename = $originalRow.find("input.fileReplaceName").val();
        
        
        $originalRow.find('.replacement-text').text("will be replaced by " + targetFilename + ")");
        $originalRow.find('.fileReplaceName').val(targetFilename);
        $originalRow.find('.fileReplaceName').data("original-filename", originalFilename)
        
        //effectively 'remove' the target file proxy fields from the form by removing the name attribute.
        $targetRow.find("input,select").each(function(){
            var $hidden = $(this);
            $hidden.data("original-name", $hidden.attr("name"));
            $hidden.removeAttr("name");
        });
        //have original row point to target,  in the event we need to cancel later and set everything back to normal
        $originalRow.data("jqTargetRow", $targetRow).addClass("replacement-selected");

        //implicitly cancel a pending replace if user initiates a another replace operate on the same targetRow
        $targetRow.data("jqOriginalRow", $originalRow);
        
        //Change action to "REPLACE", and store original in event of cancel 
        var $fileAction = $originalRow.find('.fileAction');
        $fileAction.data('original-fileAction', $fileAction.val());
        $fileAction.val("REPLACE");
        
        $targetRow.find('.replacement-text').text("(replacing " + originalFilename + ")");
        $targetRow.find('input, select').prop("disabled", true);
    }
    
    //TODO: pull out redundant sections before adam has a chance to put this in a ticket for me.
    var _cancelFileReplace = function($originalRow, $targetRow) {
        $targetRow.find('.replacement-text').text("");
        $targetRow.find('input, select').prop("disabled", false).each(function() {
            $(this).attr("name", $(this).data("original-name"));
        });
        
        $originalRow.find('.replacement-text').text('');
        $originalRow.removeClass("replacement-selected");
        
        var $fileAction = $originalRow.find('.fileAction');
        $fileAction.val($fileAction.data('original-fileAction'));
        
        var $filename = $originalRow.find('.fileReplaceName');
        $filename.val($filename.data('original-filename'));
        $.removeData($originalRow[0], "jqTargetRow");
    }
    
    
    var _enableRow = function(row) {
        $('button:not(.delete-button), select', row).prop('disabled', false);
        $('.delete-button', row).removeClass('btn-warning').addClass('btn-danger');
    };
    
    var _disableRow = function(row) {
        $('button:not(.delete-button), select', row).prop('disabled', true);
        $('.delete-button', row).addClass('btn-warning').removeClass('btn-danger');
    };
    
    //public: kludge for dealing w/ fileupload's template system, which doesn't have a good way to pass the row number of the table the template will be rendered to
    var _getRowId = function() {
        return _nextRowId++;
    }
    
    
    //expose public elements
    return {
        "registerUpload": _registerUpload,
        //FIXME: we can remove the need for this if we include it as closure to instanced version of _destroy.
        "informationResourceId": _informationResourceId,
        "updateFileAction": _updateFileAction,
        "getRowId": _getRowId
        
    };
}();