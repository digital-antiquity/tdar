<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/${themeDir}/settings.ftl" as settings>

<head>
<title>${authenticatedUser.properName}'s Dashboard</title>
<meta name="lastModifiedDate" content="$Date$"/>


<@edit.resourceDataTableJavascript />

</head>

<div id="titlebar" parse="true">
	<h1>${authenticatedUser.properName}'s Dashboard</h1>
	
	<div class="news alert">
	<B>${siteAcronym} Update:</B>
	We just upgraded tDAR with a bunch of additional features, a list of features are available <a href="http://www.tdar.org/news/2012/07/tdar-software-update-harris/">here</a> on the tDAR website. 
	</div>
</div>


<div id="messages" style="margin:2px" class="hidden lt-ie8">
    <div id="message-ie-obsolete" class="message-error">
    You appear to be using an older version of Internet Explorer.  Note that certain features in ${siteAcronym} may not work properly.  
    <a href="http://www.microsoft.com/ie">Click here to learn more about Internet Explorer</a>
    </div>
</div>

<br/>
<div class="row">
<div class="span9">
Welcome back, ${authenticatedUser.firstName}! 
<#if contributor>
 The resources you can access are listed below.  To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or 
 <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, click on  the "upload" button above.
</#if>
<br/>
</div>
</div>
<br/>

<#if contributor>
<#if (activeResourceCount == 0)>
	<div class="span9 noindent">
		<h3>Getting Started</h3>
		<ol style='list-style-position:inside'>
		    <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
		    <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
		</ol>
	</div>
<#else>
	<div class="span9 noindent">
		<h3>At a glance</h3>
		<div class="row">
	    <div class="span4"><@common.pieChart statusCountForUser "statusForUser" "userSubmitterContext=true&includedStatuses" /></div>
	    <div class="span4"><@common.pieChart resourceCountForUser "resourceForUser" "useSubmitterContext=true&resourceTypes" /></div>
	    </div>
	    <br/>
	</div>
	<hr />	
    
    <div class="span9 noindent">
        <h3>Item(s) You've Recently Updated</h3>
        <ol id='recentlyEditedResources'>
          
            <#list recentlyEditedResources as res>
            <li id="li-recent-resource-${res.id?c}">
               <span class="fixed">
                        <@common.cartouche res true>
                <span class="recent-nav">
                    <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>">edit</a> |
                    <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>">delete</a>
                </span>
                        <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@common.truncate res.title 65 /></a>
                        </@common.cartouche>
                </span>
            </li>
            </#list>
        </ol>
    </div>

</#if>


<#if (emptyProjects?? && !emptyProjects.empty )>
	<div class="span9 noindent" id="divEmptyProjects">
	    <h3>Empty Projects</h3>
	    <ol style='list-style-position:inside' id="emptyProjects">
	    <@s.iterator value='emptyProjects' status='recentEditStatus' var='res'>
	    <li id="li-recent-resource-${res.id?c}">
	            <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>">
	                <@common.truncate res.title 70 />
	            </a> 
	        <div class="recent-nav pull-right">
	            <a href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>" title="add a resource to this project">add resource</a> |
	            <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>">edit</a> |
	            <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>" >delete</a>
	        </div>
	    </li>
	    </@s.iterator>
	    </ol>
	</div>
<hr />
</#if>

<div class="span9 noindent" id="project-list">
<h3>Browse Resources</h3>
<form action=''>
<@edit.resourceDataTable />
</form>
</div>
<div id="sidebar-right" parse="true">
<#macro repeat num val>
 <#if (num > 0)>
  <@repeat (num-1) val /><#noescape>${val}</#noescape>
 </#if>
</#macro>

   <div class="glide"><h3>Collections You Created </h3>
      <@listCollections resourceCollections>
        <#if (!resourceCollections?has_content )>
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
<hr />
<div class="span9 noindent" id="divAccountInfo">
<h3>About Your Account</h3>
    <strong>Full Name: </strong>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br />
    <#if authenticatedUser.penultimateLogin??>
        <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br/>
    </#if>
	<a href="<@s.url value='/entity/person/edit?id=${sessionData.person.id?c}'/>">edit your profile</a>
</div>

<#macro listCollections resourceCollections_ >
      <ul>
      <#assign currentIndent =1 />
        <#list resourceCollections_ as collection>
          <#assign itemIndent = collection.parentNameList?size />
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