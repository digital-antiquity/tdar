<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "../common-collection.ftl" as commonCollection>
<#import "/WEB-INF/macros/search/search-macros.ftl" as search>

<head>
<title>Admin: ${collection.name}</title>
</head>
<body>

<h1>Collection Admin: <span class="red small">${collection.name}</span></h1>

<div class="row">
<div class="span12">
            <@common.resourceUsageInfo />
</div>
</div>
<div class="row">
    <div class="span12">
        
        <table class="table tableformat">
            <thead>
                <tr> <th>id</th><th>status</th><th>type</th><th>title</th><th>files</th></tr>
            </thead>
            <tbody>
            <#list collection.resources>
                <#items as res>
                   <tr class="${res.status}" >
                    <td>${res.id?c}</td>
                    <td>${res.status}</td>
                    <td>${res.resourceType}</td>
                    <td><a href="${res.detailUrl}">${res.title}</a></td>
                    <td>${(res.totalNumberOfActiveFiles!0)?c}</td>
                   </tr>
                </#items>
            </#list>
            </tbody>
        </table>
    
    </div>

</div>
</body>
</#escape>