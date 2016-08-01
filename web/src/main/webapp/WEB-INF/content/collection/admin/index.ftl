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
<div class="span9">

    <@search.totalRecordsSection tag="h2" helper=paginationHelper itemType="Record"/>
            <#if term?has_content >Limited to: ${term}</#if>
    
    <table class="table tableformat">
        <thead>
            <tr> <th>id</th><th>status</th><th>type</th><th>title</th><th>files</th></tr>
        </thead>
        <tbody>
        <#list results>
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
            <@search.basicPagination "Records" />

</div>
          <div class="span3">
          <h5>Search</h5>
          <@s.form action="" method="GET">
              <input type="search" class="form-control" placeholder="Search... " name="term"  value="${term!''}" />
          </@s.form>
          <h5>Sort</h5>
          <@s.select value="sortField" name='sortField' cssClass="input-large" theme="simple"
                      emptyOption='false' listValue='label' list='%{sortOptions}'/>
          <h5>Resource Type</h5>
          <@search.facetBy facetlist=resourceTypeFacets label="" facetParam="selectedResourceTypes" link=true liCssClass="" ulClass="unstyled" pictoralIcon=true />
          <#if (selectedResourceTypes?size > 0)>
          <a style="text-decoration: " href="<@s.url includeParams="all">
                        <@s.param name="selectedResourceTypes" value="" />
                        <@s.param name="startRecord" value=""/>
            </@s.url>">[remove this filter]</a></sup>
          </#if>

          <h5>Status</h5>
          <@search.facetBy facetlist=statusFacets label="" facetParam="selectedResourceStatuses" link=true liCssClass="" ulClass="unstyled"  />
          <#if (selectedResourceStatuses?size > 0)>
          <a style="text-decoration: " href="<@s.url includeParams="all">
                        <@s.param name="selectedResourceStatuses" value="" />
                        <@s.param name="startRecord" value=""/>
            </@s.url>">[remove this filter]</a></sup>
          </#if>

          <h5>File Access</h5>
          <@search.facetBy facetlist=fileAccessFacets label="" facetParam="fileAccessTypes" link=true liCssClass="" ulClass="unstyled"  />
          <#if (fileAccessTypes?size > 0)>
          <a style="text-decoration: " href="<@s.url includeParams="all">
                        <@s.param name="fileAccessTypes" value="" />
                        <@s.param name="startRecord" value=""/>
            </@s.url>">[remove this filter]</a></sup>
          </#if>
          
          
          <h5>Admin Tools</h5>
            <ul>
             <li> <a href="<@s.url value="/collection/admin/report/${collection.id?c}"/>">Admin Metadata Report</a></li>
             <li> <a href="<@s.url value="/search/download?collectionId=${collection.id?c}"/>">Export to Excel</a></li>
             <li> <a href="<@s.url value="/collection/admin/organize/${collection.id?c}"/>">Reorganize (BETA)</a></li>
             <#if administrator && !collection.whiteLabelCollection >
             <li>
                <form action="/collection/admin/makeWhitelabel/${id?c}" method="POST" class="inline">
                    <@s.submit cssClass="button btn btn-link tdar-btn-link" id="makeWhiteLabelCollection" value="Make Whitelabel"/>
                </form>
             
             </li>
            </#if>
             <#if administrator >
             <li>
                <form action="/collection/admin/reindex/${id?c}" method="POST" class="inline">
                    <@s.submit cssClass="button btn btn-link tdar-btn-link" id="reindexCollection" value="Reindex collection contents"/>
                </form>
             
             </li>
             <li>
                <form action="/collection/admin/changeSubmitter/${id?c}" method="POST" class="inline">
                    <@s.submit cssClass="button btn btn-link tdar-btn-link" id="reindexCollection" value="Set Submitter to 'System User'"/>
                </form>
             
             </li>
             </#if>
             <#if editor >
             <li>
                <form action="/collection/admin/makeActive/${id?c}" method="POST" class="inline">
                    <@s.submit cssClass="btn btn-link tdar-btn-link" id="makeActive" value="Make all Draft Resources Active"/>
                </form>
             </li>
             </#if>
         </ul>
            </div>

</div>
<script type="text/javascript">
    //pretty controls for sort options, sidebar options (pulled from main.js)
    $(function () {
        TDAR.common.initializeView();
        TDAR.advancedSearch.initializeResultsPage();
    });
</script>
</body>
</#escape>