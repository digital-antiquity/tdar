<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "../common-collection.ftl" as commonCollection>

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

          <div class="span6">
            <p><b>Admin Tools</b>
            <ul>
             <li> <a href="<@s.url value="/collection/report/${collection.id?c}"/>">Admin Metadata Report</a></li>
             <li> <a href="<@s.url value="/search/download?collectionId=${collection.id?c}"/>">Export to Excel</a></li>
             <#if administrator && !collection.whiteLabelCollection >
             <li>
                <b>Make Whitelabel</b>
                <form action="/collection/admin/makeWhitelabel/${id?c}" method="POST">
                    <@s.submit cssClass="button btn tdar-button" id="makeWhiteLabelCollection" />
                </form>
             
             </li>
            </#if>
             <li>
                <b>Reindex collection contents</b>
                <form action="/collection/admin/reindex/${id?c}" method="POST">
                    <@s.submit cssClass="button btn tdar-button" id="reindexCollection" />
                </form>
             
             </li>
             <li>
                <b>Make all DRAFT Resources Active</b>
                <form action="/collection/admin/makeActive/${id?c}" method="POST">
                    <@s.submit cssClass="button btn tdar-button" id="makeActive" />
                </form>
             
             </li>
         </ul>
            </div>

</div>
</body>
</#escape>