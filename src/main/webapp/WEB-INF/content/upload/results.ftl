{"totalSize":${uploadFileSize?c},
 "files":[<#list uploadFileFileName as fileName>
{	"name":"${fileName!"error"}",
	"type":"${uploadFileContentType[fileName_index]!"error"}",
	"size":"${(uploadFileSizes[fileName_index]!0)?c}"}<#if fileName_has_next>,</#if>
</#list>]}