/* ASYNC FILE UPLOAD SUPPORT */

TDAR.namespace("fileupload");

TDAR.fileupload = function() {
    'use strict';
    
    var _informationResource;
    var _informationResourceId = -1;
    
    //main file upload registration function
    var _registerUpload  = function(options) {
        
        //combine options w/ defaults
        var _options = $.extend({formSelector: "#resourceMetadataForm"}, options);
        
        
        //pass off our options to fileupload options (to allow easy passthrough of page-specific (options e.g. acceptFileTypes)
        var $fileupload = $(_options.formSelector).fileupload($.extend({
            add: function(e, data) {
                data.submit(); //upload after file selected
            },
            formData: function(){
                return [{name:"ticketId", value:$('#ticketId').val()}]
            },
            url: TDAR.uri('upload/upload'),
            
            destroy: _destroy
        }, _options));
        
        
        $fileupload.bind("fileuploadcompleted", _updateReplaceButtons);
        
        
        //hack: disable until we have a valid ticket
        $fileupload.fileupload("disable");
        
        $.post(TDAR.uri("upload/grab-ticket"), function(data) {
            $('#ticketId').val(data.id);
            $fileupload.fileupload('enable');
        }, 'json');
        
        //populate list of peviously uploaded files,  if available.
        if(_options.informationResourceId) {
            _informationResourceId = _options.informationResourceId;
            $.ajax(
                    {
                        url: TDAR.uri("upload/list-resource-files"), 
                        data: {informationResourceId: _options.informationResourceId},
                        success: function(data){
                            var files = _translateIrFiles(data);
                            console.log("files.length: %s", files.length);
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
                fileId: proxy.fileId,
                
            };
        });
    };

    //mark a file as deleted/undeleted and properly set appearance and fileproxy state
    var _destroy = function(e, data){
        // data.context: download row,
        // data.url: deletion url,
        // data.type: deletion request type, e.g. "DELETE",
        // data.dataType: deletion response type, e.g. "json"
        
        //HACK: this is ugly jim fix this asap!
        //FIXME: any button in the "delete" TD is treaded as delete. either put REPLACE button in separate <td> or find a way to disregard
        
        var newUpload = TDAR.fileupload.informationResourceId === -1;
        var $btnDelete = $("button.delete-button", data.context);
        var $hdnAction = $(".fileAction", data.context);
        if($btnDelete.data("type") === "DELETE") {
            $hdnAction.data("prev", $hdnAction.val());
            $hdnAction.val(newUpload ? "NONE" : "DELETE");
            $(data.context).addClass("deleted-file");
            $btnDelete.data("type", "UNDELETE");
            //TODO: make row look "deleted", disable everything except 'undelete' button
        } else {
            //re-enable row appearance and change label back to previous state
            $hdnAction.val($hdnAction.data("prev"));
            $(data.context).removeClass("deleted-file");
            $btnDelete.data("type", "DELETE");
        }
        //show the correct button label
        $("span", $btnDelete).html({
            "DELETE": locale.fileupload.destroy,
            "UNDELETE": "Undelete"
        }[$btnDelete.data("type")] );
               
        
        _updateReplaceButtons(e, data);
        console.log("destroy called. context:%s", data.context[0]);
    };
    
    
    var _updateReplaceButtons = function(e, data) {
        console.log("_updateReplaceButtons")
        //if there are any newfile rows,  enable all the replace buttons
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
        var $tbody = $(button).closest("tbody.files");
        var $newfiles = $('.new-file:not(.replace-target,.deleted-file)');
        var data = {
                jqnewfiles: $newfiles,
                //TODO: figure  out if this existing file has already chosen a replacement, if so,  render a "cancel" option.
                bReplacePending: false};
        var repoHtml = tmpl("template-replace-menu", data);
        $ul.html(repoHtml);
    };
    
    //todo: bIsReplacementAvailable()
    
    //todo: handle additions to replacementList
    
    //todo: handle deletions from replacementList
    
    //todo: dynamically generate replace UL contents
    
    //FIXME:   preventDefault on menuItems
    
    
    //expose public elements
    return {
        "registerUpload": _registerUpload,
        //FIXME: we can remove the need for this if we include it as closure to instanced version of _destroy.
        "informationResourceId": _informationResourceId,
        "updateFileAction": _updateFileAction
    };
}();