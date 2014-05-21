<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="image"/>
    <#global itemLabel="image"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />
    <#global useBulkUpload=true />
    <#import "batch-common.ftl" as batchCommon>

<head>
    <title>Batch Upload Documents or Images</title>

</head>

    <#macro customH1>
    <h1>Batch Resource Upload</h1>
    </#macro>

    <#macro basicInformation>
        <@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
    </#macro>


    <#macro beforeUpload>
        <@batchCommon.printTemplate />

    <div>
        <br>

        <p><b>select all of the files you'd like to add to ${siteAcronym}. Each one will be associated with a new record along with any metadata specified
            either in the excel spreadsheet specified above, or with any values selected on this form.</b></p>
    </div>
    </#macro>

    <#macro localSection>
    <div class="glide">
        <h3>Note</h3>
        For all of the fields below, you can select values that will apply to all of the files you've chosen to upload above.
    </div>
    </#macro>


    <#macro footer>
    <table style="display:none;visibility:hidden" id="queuedFileTemplate">
        <@edit._fileProxyRow />
    </table>
    </#macro>

    <#macro localJavascript>
    $('#fileAsyncUpload').rules('add', 'asyncFilesRequired');
    </#macro>
</#escape>
