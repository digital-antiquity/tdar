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
            maxNumberOfFiles: 5,
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
        $listItems.find('a').bind("click", _replacementFileSelected);
        $ul.empty();
        $ul.append($listItems);
    };

    //"replacement file chosen" event handler.  update the hidden replacement filename field of the replacement file row, and mark target as selected
    var _replacementFileSelected = function(e) {
        var anchorElement = this;
        var $tr = $(anchorElement).parents("tr.existing-file");
        var $filename  = $('.fileReplaceName', $tr);
        
        if($(e).hasClass("cancel")) {
            $filename.val($filename.data("original-value"));
            $('.replacement-text', $tr).text("").fadeOut();
            $tr.removeClass('replacement-selected');
        } else {
            var newFilename = $(anchorElement).data("filename");
            $filename.data("original-value", $filename.val());
            $filename.val(newFilename);
            $('.replacement-text', $tr).text(" (will be replaced by '" + newFilename + "')").fadeIn();
            $tr.addClass('replacement-selected');
            //TODO: figure out how to prevent user from selecting same target for two existing files.
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