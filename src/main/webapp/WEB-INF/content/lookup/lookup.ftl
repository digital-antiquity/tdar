<#if callback??>${callback}(</#if>
{"${lookupSource.collectionName!"results"}":[
<#if results?has_content>
<#list results as jsonResult>
${jsonResult.toJSON().toString()}<#if !jsonResult_has_next>,</#if>
</#list>
</#if>
],
"status": {
   "recordsPerPage" : ${recordsPerPage?c},
   "startRecord" : ${startRecord?c},
   "totalRecords" : ${totalRecords?c},
   "sortField" : "${sortField!}"
}

}
<#if callback??>);</#if>
