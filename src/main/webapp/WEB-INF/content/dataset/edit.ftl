<#escape _untrusted as _untrusted?html>
<#global itemPrefix="dataset"/>
<#global itemLabel="dataset"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=false />

<head>
<style>
.deleteButton, .replaceButton {display:none;}
</style>
</head>
	
	<#macro basicInformation>
		<br/>
		<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
		<br/>
	</#macro>
	
	
	
	<#macro localJavascript>
	    <#if validFileExtensions??>
	    var validate = $('.validateFileType');
	    if ($(validate).length > 0) {
	        $(validate).rules("add", {
	            accept: "<@edit.join sequence=validFileExtensions delimiter="|"/>",
	            messages: {
	                accept: "Please enter a valid file (<@edit.join sequence=validFileExtensions delimiter=", "/>)"
	            }
	        });
	    }
	    </#if>
	</#macro>

</#escape>