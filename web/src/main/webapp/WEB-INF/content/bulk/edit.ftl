<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="image"/>
    <#global itemLabel="image"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />
    <#global useBulkUpload=true />

<head>
    <title>Batch Upload Documents or Images</title>

</head>

    <#macro customH1>
    <h1>Bulk Resource Upload</h1>
    <div class="">
        <p><b>Note</b>
        For all of the fields below, you can select values that will apply to all of the files you've chosen to upload above.
        </p>
    </div>
    </#macro>

    <#macro basicInformation>
        <@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
    </#macro>


    <#macro beforeUpload>

    <div class="alert">
        <p>
            Select the files you'd like to add to ${siteAcronym}. Each file will be associated with a new record along with any metadata specified
            either in the excel spreadsheet specified above, or with any values selected on this form.</p>
    </div>
    </#macro>

    <#macro localSection>

    <p></p>
    <div class="well">
        <span class="label label-important">Please Note </span>
        ${siteAcronym} will apply information in the fields below to all of the records you've chosen to upload above.
    </div>
    </#macro>

</#escape>
