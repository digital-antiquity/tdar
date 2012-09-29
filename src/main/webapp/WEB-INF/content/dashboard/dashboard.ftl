<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<head>
<title>${authenticatedUser.properName}'s Dashboard</title>
<meta name="lastModifiedDate" content="$Date$"/>


<script type='text/javascript' src='<@s.url value="/includes/datatable-support.js"/>'></script>
<script type='text/javascript'>
try {
    $(function() {
        //here we assume that IE reports browser versions as a string, leading w/ major version
        if($.browser.msie &&  getBrowserMajorVersion() < 8) {
            $('#message-ie-obsolete').show();
        }
    });
} catch(ex) {
    console.error("browser check failed");
}
</script>

<@edit.resourceDataTableJavascript />

<style type="text/css">
    div.recent-title {display:inline-block; width:600pt;}
    div.recent-nav {display:inline-block; float:right;}
    #recentlyEditedResources li:hover{background-color: #eee9d5} 
    #emptyProjects li:hover{background-color: #eee9d5} 
</style>
</head>

<div id="messages" style="margin:2px">
    <div id="message-ie-obsolete" class="message-error" style="display:none">
    You appear to be using an older version of Internet Explorer.  Note that certain features in tDAR may not work properly.  
    <a href="http://www.microsoft.com/ie">Click here to learn more about Internet Explorer</a>
    </div>
</div>

<br/>
<div class="glide">
Welcome back, ${authenticatedUser.firstName}! 
<#if contributor>
 The resources you can access are listed below.  To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, use the "new" menu above.
</#if>
<br/>
</div>
<!--
<div class="glide news">
<B>FIXME: ADD NEW BLOG POST</B>
We just upgraded tDAR with a bunch of additional features, a list of features are available <a href="http://www.tdar.org/news/2011/10/tdar-software-update-fluvial/">here</a> on the tDAR website. 
</div>
-->
<#if contributor>
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

    <div style="float:right"><@common.pieChart statusCountForUser "statusForUser" "includedStatuses" /></div>
    <@common.pieChart resourceCountForUser "resourceForUser" "resourceTypes" />


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
           <span class="fixed"> [${res.resourceType.label}] <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@common.truncate res.title 65 /></a></span>
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
                <@common.truncate res.title 70 />
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
<#macro repeat num val>
 <#if (num > 0)>
  <@repeat (num-1) val /><#noescape>${val}</#noescape>
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
</#if>
<div class="glide" id="divAccountInfo">
<h3>About Your Account</h3>
    <em class="label">Full Name</em>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br />
    <#if authenticatedUser.penultimateLogin??>
        <em class="label">Last Login</em>${authenticatedUser.penultimateLogin?datetime}<br/>
    </#if>
<em class="label">&nbsp;</em><a href="<@s.url value='/entity/person/edit?id=${sessionData.person.id?c}'/>">edit your profile</a>
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
</#escape>