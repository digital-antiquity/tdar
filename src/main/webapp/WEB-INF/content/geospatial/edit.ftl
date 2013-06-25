<#escape _untrusted as _untrusted?html>
<#global itemPrefix="geospatial"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=true />
<#global hideRelatedCollections=true/>
<#global hideKeywordsAndIdentifiersSection=true/>

<#macro localJavascript>
console.log("adding gis validation rules");
TDAR.fileupload.addGisValidation(TDAR.fileupload.validator);
</#macro>

<#macro footer>

</#macro>
</#escape>