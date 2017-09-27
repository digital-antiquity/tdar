<#escape _untrusted as _untrusted?html >
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
    <#import "/${config.themeDir}/settings.ftl" as settings>

<title>Usage Information for ${resource.title}</title>

<h1>Usage Information for <span>${resource.title}</span></h1>

<h2>Usage Stats</h2>
    <div class="lineGraph" id="statusChart"  data-table="#totalTable" style="height:200px" data-x="Date"  >
    </div>
    <#--  
<#if ((stats.allMap.size)!0 > 0)>
</#if>
     -->
 

 <#macro totalTable map={} label="" id="">
 <h3>${label}</h3> 
<table class="tableFormat table" id="${id}">
 <tr>
 <th>Date</th>
 <th>Views</th>
 <th>Views (bot)</th>
 <#list stats.filenames![] as f>
 <th>Downloads of ${f}</th>
 </#list>
 </tr>
 <#list map?values?reverse as v>
 <tr>
 	<td>${v.dateString}</td>
 	<td>${v.total!0}</td>
 	<td>${v.totalBot!0}</td>
 	<#list v.totalDownloads as down>
 		<td>${down}</td>
 	</#list>
 </tr>
 </#list>
 </table>
</#macro>

<@totalTable map=stats.dailyMap label="Week" />
<@totalTable map=stats.monthlyMap label="Month" />
<@totalTable map=stats.annualMap label="Year" />
<@totalTable map=stats.allMap label="Overall" id="totalTable" />

</#escape>