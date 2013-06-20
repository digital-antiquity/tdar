<#if processedFileNames??>
<ul>
<#list processedFileNames as processedFileName>
    <li>${processedFileName?html}</li>
</#list>
</ul>
</#if>