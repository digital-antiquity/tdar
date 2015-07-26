<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>

<head>
    <title>Data Integration: Filtered Results</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <meta http-equiv="content-type" content="text/html; charset=utf-8"/>
</head>


<div class="glide">
    <#if (result.workbook?has_content )>
        <div class="">
            <a class="btn btn-primary pull-right" href='<@s.url value="download?ticketId=${ticketId?c}"/>' id="downloadLink">Download results as an Excel
                document</a>

            <h2>Data Integration: Filtered Results</h2>
        </div>
    </#if>
</div>

<div class="glide">

    <h3>Summary of Integration Results (counts of integration column values)</h3>

    <table class="tableFormat table table-striped">
        <thead>
        <tr>
            <#list integrationColumns as column>
                    <#if column.integrationColumn>
                        <th>${column.name}</th>
                    </#if>
            </#list>
            <#list selectedDataTables as table>
                <th>${table.dataset.title} - ${table.displayName}</th>
            </#list>
            <th>Total</th>
        </tr>
        </thead>
        <tbody>
            <#assign pivot = result.rawPivotData />
            <#assign keys = pivot?keys >
            <#list keys?sort as key >
            <tr>
                <#list key as node >
                    <td><#if node??>
           ${node.displayName!''}</#if></td>
                </#list>
                <#assign totals = 0/>
                <#list selectedDataTables as table>


                    <td> 
                     <#if pivot.get(key)?? && pivot.get(key).get(table.id)??>
                    ${pivot.get(key).get(table.id)}
                        <#assign totals = totals + pivot.get(key).get(table.id)?number />
                    <#else>0</#if></td>
                </#list>
                <td>${totals}</td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>
<br/><br>
<h3>Preview Results <span>(1st 10 rows of each table)</span></h3>
<table class='table tableFormat'>
    <thead><tr>
        <th>Table</th>
    <#list result.integrationContext.integrationColumns as integrationColumn>
        <th>${integrationColumn.name}</th>
        <#if integrationColumn.integrationColumn>
            <th>${integrationColumn.name} (Mapped)</th>
            <th>${integrationColumn.name} (Sort)</th>
        </#if>
    </#list>
</tr></thead>
<tbody>
    <#list result.previewData as row>
        <tr>
            <#list row as col>
            <td>${col!""}</td>
            </#list>
        </tr>
    </#list>
</tbody>
</table>


</#escape>