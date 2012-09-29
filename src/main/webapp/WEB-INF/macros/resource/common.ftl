<#-- 
$Id:Exp$
Common macros used in multiple contexts
-->
<#macro convertFileSize filesize=0>
<#assign mb = 1048576 />
<#assign kb = 1024 />
<#if (filesize > mb)>
${(filesize / mb)?string(",##0.00")}mb
<#elseif (filesize > kb)>
${(filesize / kb)?string(",##0.00")}kb
<#else>
${filesize?string(",##0.00")}b
</#if>
</#macro>



<#macro resourceCollectionsRights effectiveResourceCollections_>
    <#if !effectiveResourceCollections_.empty>
    <h4>Rights Inherited from Associated tDAR Collections</h4>
    <#nested />
    <table class="tableFormat zebracolors">
    <thead><th>collection</th><th>user</th><th>right</th></thead>
    <#list effectiveResourceCollections_ as collection_>
      <#if collection_??>
        <#list collection_.authorizedUsers as user>
        <tr>
          <td>
            <#if !collection_.internal>
               <a href="<@s.url value="/collection/${collection_.id?c}"/>"> ${collection_.name!"<em>un-named</em>"}</a>
            </#if>
          </td>
          <td>
            ${user.user.properName}
          </td>
          <td>
           ${user.generalPermission.label}
           </td>
         </tr>
        </#list>
      </#if>
    </#list>
    </table>
    </#if>
</#macro>
