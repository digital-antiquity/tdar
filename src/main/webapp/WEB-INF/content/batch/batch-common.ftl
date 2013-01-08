<#escape _untrusted as _untrusted?html>

<#macro printTemplate>
	<div class="glide">
	<#if !ticketId?has_content>
	    <h3>Upload record specific metadata</h3>
	        <div tiplabel="Upload your document(s)"  tooltipcontent="The metadata entered on this form is tied to that 
	            one file. Documents must be in the following types: xls,xlsx">
	             <h4>Instructions:</h4>
	            <ol>
	              <li><a href="template" class="button" onClick="navigateTempIgnore()">Download</a> the template file (<a href="template" onClick="navigateTempIgnore()">link</a>).</li>
	              <li>create a row for each file you're uploading</li>
	              <li>the first column should always be the filename the record references [eg. document1.pdf ]</li>
	              <li>enter in any other metadata by using the field names specified.</li>
	              <li>upload the mapping file in the mapping file in the input immediately below</li>
	            </ol>
	            </div>
	            <br/>
	                <@s.file label="Upload a Mapping File" cssClass="validateFileType" labelposition='top' name='uploadedFiles' size='40'/>
	<#else>
	<@s.hidden name="templateFilename" />
	<strong>Template File</strong>:${templateFilename}
	</#if>
	</div>
</#macro>

<script>
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


</script>
</#escape>