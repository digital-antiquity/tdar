<#escape _untrusted as _untrusted?html>
<#global itemPrefix="dataset"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=true />

	<#macro basicInformation>
		<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
	</#macro>
	
</#escape>