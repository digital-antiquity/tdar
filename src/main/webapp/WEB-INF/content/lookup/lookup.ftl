<#if callback??>${callback}(</#if>
{"${lookupSource.collectionName!"results"}": ${jsonResults},
"status": {
"recordsPerPage" : ${recordsPerPage?c},
"startRecord" : ${startRecord?c},
"totalRecords" : ${totalRecords?c},
"sortField" : "${sortField!}"
}

}
<#if callback??>);</#if>
