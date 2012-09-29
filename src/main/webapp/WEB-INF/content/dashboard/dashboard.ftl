<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<title>${authenticatedUser.properName}'s Information Resources</title>
<meta name="lastModifiedDate" content="$Date: 2011-08-09 06:30:06 -0700 (Tue, 09 Aug 2011) $"/>


<script type='text/javascript' src='<@s.url value="/includes/datatable-support.js"/>'></script>

<@edit.resourceDataTableJavascript />

<style type="text/css">
    div.recent-title {display:inline-block; width:600pt;}
    div.recent-nav {display:inline-block; float:right;}
    #recentlyEditedResources li:hover{background-color: #eee9d5} 
    #emptyProjects li:hover{background-color: #eee9d5} 
</style>
</head>

<div class="glide">
Welcome back, ${authenticatedUser.firstName}!  The resources you can access are listed below.  To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, use the "new" menu above.
<br/>
</div>

<!--
<div class="glide news">
We just upgraded tDAR with a bunch of additional features, a list of features are available <a href="http://www.tdar.org/news/2011/10/tdar-software-update-fluvial/">here</a> on the tDAR website. 
</div>
-->
<#if (activeResourceCount == 0)>
<div class="glide">
<h3>Getting Started</h3>
<ol style='list-style-position:inside'>
    <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
    <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
</ol>
</div>

<#else>

<div class="glide">
<h3>At a glance</h3>

    <div style="float:right"><@pieChart statusCountForUser "statusForUser" "includedStatuses" /></div>
    <@pieChart resourceCountForUser "resourceForUser" "resourceTypes" />


</div>

<div class="glide">
<h3>Item(s) You've Recently Updated</h3>
<ol style='list-style-position:inside' id='recentlyEditedResources'>
    <@s.iterator value='recentlyEditedResources' status='recentEditStatus' var='res'>
    <li id="li-recent-resource-${res.id?c}">
        <div class="recent-nav">
            <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>">edit</a> |
            <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>">delete</a>
        </div>
           <span class="fixed"> [${res.resourceType.label}] <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@rlist.abbr maxlen=65>${res.title}</@rlist.abbr></a></span>
    </li>
    </@s.iterator>
</ol>
</div>

</#if>

<#if (emptyProjects?? && !emptyProjects.empty )>
<div class="glide" id="divEmptyProjects">
    <h3>Empty Projects</h3>
    <ol style='list-style-position:inside' id="emptyProjects">
    <@s.iterator value='emptyProjects' status='recentEditStatus' var='res'>
    <li id="li-recent-resource-${res.id?c}">
            <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>">
                <@rlist.abbr>${res.title}</@rlist.abbr>
            </a> 
        <div class="recent-nav">
            <a href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>" title="add a resource to this project">add resource</a> |
            <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>">edit</a> |
            <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>" >delete</a>
        </div>
    </li>
    </@s.iterator>
    </ol>
</div>
</#if>

<div class="glide" id="project-list">
<h3>Browse Resources By Project</h3>
<form action=''>
<@edit.resourceDataTable />
</form>
</div>
<div id="sidebar" parse="true">
<style>
#sidebar ul ul {font-size:100% !important}
</style>
<#macro repeat num val>
 <#if (num > 0)>
  <@repeat (num-1) val />${val}
 </#if>
</#macro>

   <div class="glide"><h3>Collections You Created </h3>
      <@listCollections resourceCollections>
        <#if (resourceCollections?? && resourceCollections.size() == 0)>
          <li><a href="<@s.url value="/collection/add"/>">create one</a></li>
        </#if>
      </@listCollections>
   </div>
   <br/>
   <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
     <div class="glide"><h3>Collections Shared With You</h3>
       <@listCollections sharedResourceCollections />
    </div>
  </#if>
</div>

<#macro listCollections resourceCollections_ >
      <ul>
      <#assign currentIndent =1 />
        <#list resourceCollections_ as collection>
          <#assign itemIndent = collection.parentNameList.size() />
          <#if itemIndent != currentIndent>
            <#if (itemIndent > currentIndent) >
              <@repeat (itemIndent - currentIndent) "<ul>"/>
            </#if>
            <#if (itemIndent < currentIndent) >
              <@repeat (currentIndent - itemIndent)  "</ul>"/>
            </#if>
            <#assign currentIndent = itemIndent />
          </#if>
            <li><a href="<@s.url value="/collection/${collection.id?c}"/>">
                  <#if collection.name?? && collection.name != ''>
                      ${collection.name!"no title"}
                  <#else>No Title</#if>
            </a></li>
      </#list>
      <#if (currentIndent > 1)>
              <@repeat (currentIndent - 1)  "</ul>"/>      
      </#if>
      <#nested>
      </ul>

</#macro>

<#macro pieChart map name type width=300 height=100>
    <#assign ilist = map />
    <#assign ikeys=ilist?keys />
    <#assign values = ""/>
    <#assign labels = ""/>
    <#assign keys = "" />
    <#list ikeys as ikey>
      <#assign val = ilist.get(ikey) />
      <#assign label = ikey.label />
      <#if (val?? && val > 0)>
      <#assign values>${values}${val},</#assign>
      <#assign labels>${labels}${label}|</#assign>
      <#assign keys>${keys}${ikey}|</#assign>
      </#if>
    </#list>

    <#if values!="" && labels !="">
    <#assign values = values?substring(0,values?last_index_of(","))/>
    <#assign labels = labels?substring(0,labels?last_index_of("|"))/>
     <!-- http://code.google.com/apis/chart/image/docs/chart_params.html -->
     <#assign pieUrl>http://chart.googleapis.com/chart?cht=p&chd=t:${values}&chs=${width}x${height}&chl=${labels}&chf=bg,s,0000FF00&chco=4B514D|C3AA72|DC7612|2C4D56|BD3200|A09D5B|F6D86B</#assign>
    <img usemap="${name}" id="${name}-img" src="${pieUrl}">
    <script>
$.ajax({
  url: "${pieUrl}&chof=json",
  dataType: 'json',
  success: function(data) { makeMap(data,"${name}",'${type}', "${keys}");} 
});    
    </script>
    </#if>

</#macro>

