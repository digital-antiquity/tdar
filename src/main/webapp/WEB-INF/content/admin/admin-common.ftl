<#escape _untrusted as _untrusted?html >
<#import "/${themeDir}/settings.ftl" as settings>
<#import "/WEB-INF/macros/resource/common.ftl" as common>


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



<#macro statsTable statsObj header="HEADER" cssid="CSS_ID" valueFormat="number">
  <#assign width=900/>
  <#assign height=225/>
<div class="glide">
    <h3>${header}</h3>
<div id="graph${cssid}" style="width:${width}px;height:${height}px"></div>
<#assign statsObjKeys = statsObj?keys?sort?reverse />
<#assign numSets = 0/>
<#assign totalRows = 0/>
    <table class="tableFormat">
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
             <tr>
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
            d${key__index}.push([${(key.time)?c}, ${vals.get(key_)?default("0")?c}]);
            d${key__index}.label = "${key_.label}";
            d${key__index}.color = "${settings.barColors[key__index % settings.barColors?size ]}";
            </#list>
        </#list>

    $.plot($("#graph${cssid}"), [ <#list 0..numSets as i><#if i != 0>,</#if>{label: d${i}.label, data: d${i},color: d${i}.color }</#list> ],{
        xaxis: {
            mode:"date",
          tickFormatter: function (val, axis) {
            var d = new Date(val);
            return  d.getFullYear() + "-" + (d.getUTCMonth() + 1) + '-' + d.getUTCDate();
          }
        },
        yaxis: {
       //        transform: function (v) { return Math.log(v); },
            inverseTransform: function (v) { return Math.exp(v); }
        },
        legend : {
            show:true,
            position:"nw"
        }
    });
});
</script>
</#noescape>
</div>

</#macro>
<#macro append v1 delim><#if v1?? && v1 != ''>${delim!""}${v1}</#if></#macro></#escape>
