<#escape _untrusted as _untrusted?html>
<head>
    <title>Add a source citation to ${linkedInformationResource.title}</title>
    <script type='text/javascript'>
        $(document).ready(function () {
            $('#selectResourceTypeForm').validate();
            $('#documentTypeId').rules("add", {
                required: true,
                messages: {
                    required: "Please enter the type of document you wish to add."
                }
            });
        });

    </script>
</head>

<h2>Add a source citation</h2>
<p>
    Adding a source citation to the information resource entitled: <span class='highlight'>${linkedInformationResource.title}</span>
</p>
    <@s.form id='selectResourceTypeForm' method='post' action='add'>
        <@s.token name='struts.csrf.token' />
        <@s.hidden name='linkedResourceId' value='${linkedInformationResource.id}'/>
        <@s.hidden name='linkType' value='source'/>
        <@s.select labelposition='left' label='Document type' id='documentTypeId' name='documentType' emptyOption='true' listValue="label" list='%{documentTypes}' />
        <@s.submit align='left' value='Continue'/>
    </@s.form>
</#escape>
