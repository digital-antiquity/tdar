<#escape _untrusted as _untrusted?html >
<#import "/${themeDir}/settings.ftl" as settings>


<title>Admin Pages</title>


<#setting url_escaping_charset="UTF-8">
<#macro header>
<div class="glide">
    <h3>Statistics Menu</h3>
    <table cellpadding=4>
    <tr>
        <td><a href="resource">Resource Statistics</a> </td>
        <td><a href="user">User Statistics</a> </td>
        <td><a href="keyword-stats">Keyword Statistics</a> </td>
    </tr>
    </table>
</div>    
</#macro>



<#macro statsTable statsObj header>
<div class="glide">
    <h3>${header}</h3>
    <#assign maxGraphCount = 50>
    <#assign statsObjKeys = statsObj?keys?sort?reverse />
    <#assign start = ""/> <#assign end = ""/>
    <#assign labels= ""/> <#assign p1= ""/> <#assign p2= ""/> <#assign p3= ""/> <#assign p4= ""/> <#assign p5= ""/>
    <#assign p6= ""/> <#assign p7= ""/> <#assign p8= ""/> <#assign p9= ""/> <#assign max = 0/>
    
    <#assign output>
    <table class="tableFormat">
        <#assign first = true/>
       <#assign graphCount =0>
        <#list statsObjKeys as key>
            <#assign vals = statsObj.get(key) />
            <#assign valsKeys = vals?keys />
            <#if first>
     <#noescape>
             <thead>
              <tr>
                <th>Date</th>
	</#noescape>
                <#list valsKeys as key_>
                         <#noescape><th></#noescape>
                    ${key_.label}
                         <#noescape></th></#noescape>
                    <#if key__index == 0 >
                        <#assign labels=  key_.label?url /> 
                    <#else>
                        <#assign labels= labels + "|" + key_.label?url /> 
                    </#if>
                </#list>
     <#noescape>
              </tr>
             </thead>
     </#noescape>
             <#assign end=key?date/>
            </#if>
            <#assign first=false/>
     <#noescape>
             <tr>
               <td>
     </#noescape>
                ${key?date}
    <#noescape></td></#noescape>
    
                <#list valsKeys as key_>
    <#noescape><td></#noescape>
                   ${vals.get(key_)?default("0")}
    <#noescape></td></#noescape>
		                <#if graphCount &lt; maxGraphCount>
	                    <#if vals.get(key_) &gt; max ><#assign max= vals.get(key_)/></#if>
	                    <#if key_index == 0 >
	                      <#if key__index == 0> <#assign p1= vals.get(key_)?c /></#if> 
	                      <#if key__index == 1> <#assign p2= vals.get(key_)?c /></#if> 
	                      <#if key__index == 2> <#assign p3= vals.get(key_)?c /></#if> 
	                      <#if key__index == 3> <#assign p4= vals.get(key_)?c /></#if> 
	                      <#if key__index == 4> <#assign p5= vals.get(key_)?c /></#if> 
	                      <#if key__index == 5> <#assign p6= vals.get(key_)?c /></#if> 
	                      <#if key__index == 6> <#assign p7= vals.get(key_)?c /></#if> 
	                      <#if key__index == 7> <#assign p8= vals.get(key_)?c /></#if> 
	                      <#if key__index == 8> <#assign p9= vals.get(key_)?c /></#if> 
	                    <#else> 
	                      <#if key__index == 0> <#assign p1= vals.get(key_)?c + "," + p1 /></#if> 
	                      <#if key__index == 1> <#assign p2= vals.get(key_)?c + "," + p2 /></#if> 
	                      <#if key__index == 2> <#assign p3= vals.get(key_)?c + "," + p3 /></#if> 
	                      <#if key__index == 3> <#assign p4= vals.get(key_)?c + "," + p4 /></#if> 
	                      <#if key__index == 4> <#assign p5= vals.get(key_)?c + "," + p5 /></#if> 
	                      <#if key__index == 5> <#assign p6= vals.get(key_)?c + "," + p6 /></#if> 
	                      <#if key__index == 6> <#assign p7= vals.get(key_)?c + "," + p7 /></#if> 
	                      <#if key__index == 7> <#assign p8= vals.get(key_)?c + "," + p8 /></#if> 
	                      <#if key__index == 8> <#assign p9= vals.get(key_)?c + "," + p9 /></#if> 
	                    </#if>
			             <#assign start=key?date/>
                    </#if>
                </#list>
				<#assign graphCount=graphCount + 1 />
    <#noescape></tr></#noescape>
        </#list>
    <#noescape></table></#noescape>
    </#assign>
    <#assign width=700/>
    <#assign height=225/>
<img src="http://chart.apis.google.com/chart?chxt=x,y&chxr=1,0,${max }&chxl=0:|${start?url}|${end?url}&chs=${width}x${height}&chdlp=t&cht=lc&chco=<@settings.themeColors separator=","/>&chds=a&chf=bg,s,0000FF00&chd=t:<@append p1 ""/><@append p2 "|"/><@append p3 "|"/><@append p4 "|"/><@append p5 "|"/><@append p6 "|"/><@append p7 "|"/><@append p8 "|"/><@append p9 "|"/>&chdl=${labels}&chg=14.3,-1,1,1&chls=2,4,0|1&chma=|2,4&&chtt=${header}">
<#noescape>${output}</#noescape>
</div>

</#macro>
<#macro append v1 delim><#if v1?? && v1 != ''>${delim!""}${v1}</#if></#macro></#escape>
