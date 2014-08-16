<#escape _untrusted as _untrusted?html>

    <#macro printTemplate>
    <div class="">
        <#if templateValidated>
            <@s.hidden name="templateFilename" />
            <strong>Using template file</strong>:${templateFilename}
        <#else>
            <h3>Upload record specific metadata</h3>

            <div data-tiplabel="Upload document(s)" data-tooltipcontent="The metadata entered on this form is tied to that
	            one file. Documents must be in the following types: xls,xlsx">
                <h4>Instructions:</h4>
                <ol>
                    <li><a href="template" class="button" target="_blank">Download</a> the mapping file template (<a href="template" target="_blank">link</a>).
                    </li>
                    <li>Create a row for each file you're uploading.</li>
                    <li>The first column should always be the filename the record references (e.g. <em>document1.pdf</em> ).</li>
                    <li>Enter in any other metadata by using the field names specified.</li>
                    <li>Upload the mapping file in the input immediately below.
                        <@s.file label="Upload Mapping File" cssClass="bulkValidateFileType" labelposition='top' name='uploadedFiles' size='40'/>
                    </li>
                </ol>
            </div>
        </#if>
    </div>
    </#macro>

<script>
    $(function () {
        var validate = $('.bulkValidateFileType');
        if ($(validate).length > 0) {
            $(validate).rules("add", {
                extension: "xls|xlsx",
                messages: {
                    extension: "Please enter a valid file (xls,xlsx)"
                }
            }); // end rules
        }
    });


</script>
</#escape>