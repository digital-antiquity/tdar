<#escape _untrusted as _untrusted?html >
<#import "/${themeDir}/settings.ftl" as settings>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "admin-common.ftl" as admin>


<title>Admin Pages</title>


<#setting url_escaping_charset="UTF-8">
<#macro header>
<div class="glide">
    <h3> Menu</h3>
    <table cellpadding=4>
    <tr>
        <td><a href="<@s.url value="/admin/"/>">Admin Home</a> </td>
        <td><a href="<@s.url value="/admin/resource"/>">Resource Statistics</a> </td>
        <td><a href="<@s.url value="/admin/usage/stats"/>">Usage Statistics</a> </td>
        <td><a href="<@s.url value="/admin/user"/>">User Statistics</a> </td>
        <td><a href="<@s.url value="/admin/keyword-stats"/>">Keyword Statistics</a> </td>
        <td><a href="<@s.url value="/admin/contributors"/>">Contributor Requests</a></td>
        <td><a href="<@s.url value="/admin/file-info"/>">File Information</a></td>
		<td><a href="<@s.url value="/admin/authority-management/index"/>">DeDupe</a></td>

		<#if billingManager || editor>
			<td><a href="<@s.url value="/billing/list"/>">List Billing Accounts</a></td>
			<td><a href="<@s.url value="/billing/listInvoices"/>">List Invoices</a></td>
		</#if>
        <#if administrator >
        <td><a href="<@s.url value="/admin/system/activity"/>">System Activity</a> </td>
            <td><a href="<@s.url value="/admin/searchindex/build"/>">Reindex</a></td>
        </#if>
</tr>
    </table>
</div>    
</#macro>



<#macro statsTable statsObj header="HEADER" cssid="CSS_ID" valueFormat="number">
  <#assign height=225/>
<div class="glide">
    <h3>${header}</h3>
<div id="graph${cssid}" style="height:${height}px"></div>
<#assign statsObjKeys = statsObj?keys?sort?reverse />
<#assign numSets = 0/>
<#assign totalRows = 0/>
	<table class="tableFormat table">
        <#assign first = true/>
        <#list statsObjKeys as key>
            <#assign vals = statsObj.get(key) />
            <#assign valsKeys = vals?keys />
             <#if first>
             <thead>
              <tr>
                <th>Date</th>
                <#list valsKeys as key_>
                         <th>
                    ${key_.label}
                         </th>
                <#if (numSets < key__index )>
                    <#assign numSets = key__index />
                </#if>
                </#list>
              </tr>
             </thead>
             <#else>
             <#assign totalRows = totalRows +1 />
             </#if>
             <#assign first = false/>
             <tr class="<#if (totalRows > 15)>hidden</#if>">
               <td>
                ${key?date}
              </td>
    
                <#list valsKeys as key_>
               <td>
                   <#if valueFormat == "number">
                   ${vals.get(key_)?default("0")}
                <#elseif valueFormat == "filesize">
                    <@common.convertFileSize filesize=vals.get(key_)?default("0") />
                </#if>
              </td>
                </#list>
            </tr>
        </#list>
             <#if (totalRows > 15)>
             <tr>
             <td><a href="#" onClick="$(this).parents('table').find('tr').removeClass('hidden');$(this).parent().parent().addClass('hidden');return false;">show all</a></td>
             </tr>
             </#if>
             
    </table>

<#noescape>
<script>
$(function() {

<#list 0..numSets as i>
var d${i} = [];
</#list>
<#assign ticks = "" />
<#assign total = (totalRows / 10 )?ceiling />

     <#list statsObjKeys as key>
        <#assign vals = statsObj.get(key) />
        <#assign valsKeys = vals?keys />
        <#list valsKeys as key_>
            d${key__index}.push(["${key?string("yyyy-MM-dd")}", ${vals.get(key_)?default("0")?c}]);
            d${key__index}.label = "${key_.label}";
            </#list>
        </#list>

	var labels = [<#list 0..numSets as i><#if i != 0>,</#if>d${i}.label</#list> ];
	  var plot${cssid} = $.jqplot('graph${cssid}', [<#list 0..numSets as i><#if i != 0>,</#if>d${i}</#list> ], {
	    axes:{
	        xaxis:{
	            renderer:$.jqplot.DateAxisRenderer
	        }
	    },
        highlighter: {
            show: true,
    	    sizeAdjust: 7.5
        },
        legend: {
            show: true,
            placement: 'outsideGrid',
            labels: labels,
            location: 'ne',
            rowSpacing: '0px'
        },
	    seriesDefaults:{lineWidth:1,showLabel:true, showMarker:false}
	  });
});
</script>
</#noescape>
</div>

</#macro>
<#macro append v1 delim><#if v1?? && v1 != ''>${delim!""}${v1}</#if></#macro></#escape>
