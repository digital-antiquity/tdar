<#if callback??>${callback}(</#if>
{"${lookupSource.collectionName!"results"}":[
<@s.iterator value="results" var="jsonResult" status="status">
${jsonResult.toJSON().toString()}<@s.if test="!#status.last">,</@s.if>
</@s.iterator>
],
"status": {
   "recordsPerPage" : ${recordsPerPage?c},
   "startRecord" : ${startRecord?c},
   "totalRecords" : ${totalRecords?c},
   "sortField" : "${sortField!}"
}

}
<#if callback??>);</#if>
