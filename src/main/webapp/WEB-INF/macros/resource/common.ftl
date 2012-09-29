<#escape _untrusted as _untrusted?html>
<#import "/${themeDir}/settings.ftl" as settings>
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


<#macro pieChart map name type width=300 height=100>
    <#assign ilist = map />
    <#assign ikeys=ilist?keys />
    <#assign values = ""/>
    <#assign labels = ""/>
    <#assign keys = "" />
    <#list ikeys as ikey>
      <#assign val = ilist.get(ikey) />
      <#assign label = ikey />
      <#if ikey.label??><#assign label=ikey.label ></#if>
      <#if (val?? && val > 0)>
	      <#assign values>${values}${val?url},</#assign>
	      <#assign labels>${labels}${label?url}|</#assign>
	      <#assign keys>${keys}${ikey}|</#assign>
      </#if>
    </#list>

    <#if values!="" && labels !="">
    <#assign values = values?substring(0,values?last_index_of(","))/>
    <#assign labels = labels?substring(0,labels?last_index_of("|"))/>
     <!-- http://code.google.com/apis/chart/image/docs/chart_params.html -->
     <#noescape>
     <#assign pieUrl>http://chart.googleapis.com/chart?cht=p&chd=t:${values}&chs=${width?c}x${height?c}&chl=${labels}&chf=bg,s,0000FF00&chco=<@settings.themeColors separator="|"/></#assign>
    <img usemap="${name}" id="${name}-img" src="${pieUrl}">
     </#noescape>
    <script>
$.ajax({
  url: "<#noescape>${pieUrl}</#noescape>&chof=json",
  dataType: 'json',
  success: function(data) { makeMap(data,"${name?js_string}",'${type?js_string}', "${keys?js_string}");} 
});    
    </script>
    </#if>

</#macro>

<#macro truncate text len=80>
  <#if text??>
  <!-- if text if greater than length -->
    <#if (text?length > len)>
    <!-- set pointer to last space before length (len) -->
     <#local ptr=text?last_index_of(" ",len) />
     <!-- if pointer to last space is greater than 1/2 of the max length, truncate at the pointer, 
     	  otherwise truncate at 3 before length -->
       <#if (ptr > len / 2)>
           ${text?substring(0,ptr)}...
       <#else>
           ${text?substring(0,len -3)}...
       </#if>
    <#else>
      ${text}
    </#if>
  </#if>
</#macro>

<#noescape>
<#macro safenum num=""><#t>
<#if num?? && num?is_number && num != 0>${(num?c)!}</#if><#t>
</#macro><#t>
</#noescape>

</#escape>