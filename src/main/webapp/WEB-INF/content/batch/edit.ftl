<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<title>Batch Upload Documents or Images</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>

<@s.form name='BulkMetadataForm' id='BulkMetadataForm'  cssClass="span9 form-horizontal"  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation "image" "batch" true>
    <@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
</@edit.basicInformation>
<@edit.citationInfo "image" false />


<div class="glide">
    <h3>Upload record specific metadata</h3>
        <div tiplabel="Upload your document(s)"  tooltipcontent="The metadata entered on this form is tied to that 
            one file. Documents must be in the following types: xls,xlsx">
             <h4>Instructions:</h4>
            <ol>
              <li><a href="template" onClick="navigateTempIgnore()">Download</a> the template file (<a href="template" onClick="navigateTempIgnore()">link</a>).</li>
              <li>create a row for each file you're uploading</li>
              <li>the first column should always be the filename the record references [eg. document1.pdf ]</li>
              <li>enter in any other metadata by using the field names specified.</li>
              <li>upload the mapping file in the mapping file in the input immediately below</li>
            </ol>
            </div>
            <br/>
                <@s.file label="Upload a Mapping File" cssClass="validateFileType" labelposition='top' name='uploadedFiles' size='40'/>
</div>

<@edit.asyncFileUpload "Files For Batch Upload" true "Upload" "divFileUpload">

select all of the files you'd like to add to ${siteAcronym}.  Each one will be associated with a new record along with any metadata specified either in the excel spreadsheet specified above, or with any values selected on this form.

</@edit.asyncFileUpload>

<div class="glide">
<h3>Note</h3>
For all of the fields below, you can select values that will apply to all of the files you've chosen to upload above.
</div>
<@edit.allCreators 'Creators' authorshipProxies 'authorship' />

<@edit.sharedFormComponents  fileReminder=false/>

</@s.form>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the bulk upload edit form.
    </div>
</div>
    <table style="display:none;visibility:hidden" id="queuedFileTemplate">
        <@edit.fileProxyRow />
    </table>
<@edit.resourceJavascript formSelector='#BulkMetadataForm' includeAsync=true includeInheritance=true>
<#if validFileExtensions??>

$(function() {
    var validate = $('.validateFileType');
    if ($(validate).length > 0) {
        $(validate).rules("add", {
            accept : "xls|xlsx",
            messages : {
                accept : "Please enter a valid file (xls,xlsx)"
            }
        }); // end rules
    }
});

</#if>

$(function(){
    $('#fileAsyncUpload').rules('add', 'asyncFilesRequired');
});
</@edit.resourceJavascript>

<@edit.asyncUploadTemplates />

</body>
