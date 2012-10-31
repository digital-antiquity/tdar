<head>
</head>

<h2>New School Way - using jquery file upload</h2>
    <strong>Current ticket:</strong><span id="txtCurrentId">not set</span> &nbsp;&nbsp; <strong>User</strong>:${authenticatedUser}<br />
<@s.form id="file_upload" action="upload" method="POST" enctype="multipart/form-data">
    <div id="divFileUpload">
        <input type="hidden" name="ticketId" id="ticketId" value="" />
        <input type="file" name="uploadFile" multiple>
        <button>Upload</button>
        <div>Upload files</div>
    </div>
    
    <div>here's a div</div>
</@s.form>

<table id="files"></table>

<@s.form action="list">
<h2>Retrieval Form</h2>
<p>Use this form to lookup the files you've uploaded so far</p>
    <input type="hidden" name="ticketId" id="retrievalTicketId" value="" />
    <@s.submit value="list my files" />
</@s.form>
<script>
/*global $ */
$(function () {
    $('#file_upload').fileUploadUI({
        namespace: 'file_upload_1',
        multiFileRequest:true,
        uploadTable: $('#files'),
        downloadTable: $('#files'),
        url: getBaseURI() + "upload/upload",
        formData: getFormData,
        dropZone: $('#divFileUpload'),
        buildUploadRow: function (files, index) {
            console.debug(files);
            
            if(index) { //browser doesn't support multi-file uploads, so this callback called once per fiile
                return $('<tr><td>' + files[index].name + '<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            } else { //browser supports multi-file uploads...
                return $('<tr><td> Uploading ' + files.length + ' files<\/td>' +
                        '<td class="file_upload_progress"><div><\/div><\/td>' +
                        '<td class="file_upload_cancel">' +
                        '<button class="ui-state-default ui-corner-all" title="Cancel">' +
                        '<span class="ui-icon ui-icon-cancel">Cancel<\/span>' +
                        '<\/button><\/td><\/tr>');
            }
            
        },
        buildDownloadRow: function (file) {
            return $('<tr><td>' + file.name + '<\/td><\/tr>');
        }
    });
});


//grab a ticket (if needed) from the server prior to yielding control to the file processor.
function initFileUpload(event, files, index, xhr, handler, callBack) {
    if($('#ticketId').val()){
        callBack();
    } else {
        //grab a ticket and set the ticket id back to the hidden form field
        var ticketUrl = getBaseURI() + "upload/grab-ticket";
        $.getJSON(ticketUrl, function(data) {
            console.debug("loaded json");
            $('#ticketId').val(data.id);
            getFormData();
            console.debug('uploading file');
            callBack(); //proceed with the upload
        });
        
        
    }
}

//additional form data sent with the ajax file upload.  for now we only need the ticket id
function getFormData() {
    var frmdata = [{name: 'ticketId', value:$('#ticketId').val()}];
    console.debug("getFormData:" + frmdata);   
    console.debug(frmdata);   
    $('#txtCurrentId').text($('#ticketId').val());
    $('#retrievalTicketId').val($('#ticketId').val());
    return frmdata;
}


</script> 
</html>
