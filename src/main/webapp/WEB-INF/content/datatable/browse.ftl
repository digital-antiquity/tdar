<#if callback??>${callback}(</#if>
{<#if resultsWrapper??>
"${lookupSource!"results"}":${resultsWrapper.toJSON()},
"sColumns":"${resultsWrapper.sColumns?js_string}",
"status": {
   "recordsPerPage" : ${recordsPerPage?c},
   "startRecord" : ${resultsWrapper.startRecord?c},
   "totalRecords" : ${resultsWrapper.totalRecords?c},
   "sortField" : "${sortField!}"
}
</#if>}
<#if callback??>);</#if>
