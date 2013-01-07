<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#import "batch-common.ftl" as batchCommon>

<head>
<title>Batch Upload Documents or Images</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />
<h1>Batch Resource Upload</h1>
<@s.form name='BulkMetadataForm' id='BulkMetadataForm'  cssClass="span9 form-horizontal"  method='post' enctype='multipart/form-data' action='save'>


<@s.hidden name="expectedManifestName" id="hdnExpectedManifestName" />
<#if expectedManifestName??>
<div class="alert alert-block" id="divExpectedManifestAlert">
    <h4>Template Validated</h4> ${siteAcronym} will expect "${expectedManifestName}" in your submission, and return an error if you do not include it.
    <button class="btn" data-dismiss="alert" href="#">Nevermind: I plan to upload a different template</button>
</div>
<script>
    $('#divExpectedManifestAlert').bind('close', function() {$('#hdnExpectedManifestName').remove()});
</script>

</#if>

<@edit.basicInformation "image" "batch" true>
<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
</@edit.basicInformation>

<@edit.citationInfo "image" false />

<@batchCommon.printTemplate />

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
    <table style="display:none;visibility:hidden" id="queuedFileTemplate">
        <@edit.fileProxyRow />
    </table>
<@edit.resourceJavascript formSelector='#BulkMetadataForm' includeAsync=true includeInheritance=true>

$(function(){
    $('#fileAsyncUpload').rules('add', 'asyncFilesRequired');
});
</@edit.resourceJavascript>

<@edit.asyncUploadTemplates />

</body>
