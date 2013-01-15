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
    We just upgraded tDAR with a bunch of additional features, a list of features are available <a href="http://www.tdar.org/news/2013/01/tdar-software-update-in-situ/">here</a> on the tDAR website. 
    </div>
</div>



<#if overdrawnAccounts?has_content>
<div class="alert-error alert">
<h3>The following accounts are overdrawn</h3>
<ul> 
<#list overdrawnAccounts as account>
   <li>
        <a href="<@s.url value="/billing/${account.id?c}" />">${account.name!"unamed"}</a>
    </li>
</#list>
</ul>
</div>
</#if>

<div id="messages" style="margin:2px" class="hidden lt-ie8">
    <div id="message-ie-obsolete" class="message-error">
    You appear to be using an older version of Internet Explorer.  Note that certain features in ${siteAcronym} may not work properly.  
    <a href="http://www.microsoft.com/ie" target="_blank">Click here to learn more about Internet Explorer</a>
    </div>
</div>

<div id="sidebar-right" parse="true">
<div>
 <@collectionsSection />
</div>
</div>

<div class="row">
    <div class="span9">
    Welcome back, ${authenticatedUser.firstName}! 
    <#if contributor>
     The resources you can access are listed below.  To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or 
     <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, click on  the "upload" button above.
    </#if>
    </div>
</div>


<#if contributor>
	<#if (activeResourceCount == 0)>
		<@gettingStarted />
	<hr /> 
	<#else>
		<@resourcePieChart />
		<hr/>
	
		<@recentlyUpdatedSection />
	</#if>

	<@emptyProjectsSection />	
	<@browseResourceSection />
</#if>
<hr/>
<@accountSection />
<hr/>

<@bookmarksSection />





<#macro gettingStarted>
	<div class="row">
	    <div class="span9">
	        <h2>Getting Started</h2>
	        <ol style='list-style-position:inside'>
	            <li><a href="<@s.url value="/project/add"/>">Start a new Project</a></li>
	            <li><a href="<@s.url value="/resource/add"/>">Add a new Resource</a></li>
	        </ol>
	    </div>
	</div>
</#macro>

<#macro resourcePieChart>
	<div class="row">
	    <div class="span9">
	        <h2>At a glance</h2>
	        <div class="row">
	            <div class="span4 piechart"><@common.pieChart statusCountForUser "statusForUser" "userSubmitterContext=true&includedStatuses" /></div>
	            <div class="span5 piechart"><@common.pieChart resourceCountForUser "resourceForUser" "useSubmitterContext=true&resourceTypes" /></div>
	        </div>
	    </div>
	</div>

</#macro>

<#macro recentlyUpdatedSection>

	<div class="row">
	    <div class="span9">
	        <h2>Item(s) You've Recently Updated</h2>
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
	</div>


</#macro>

<#macro emptyProjectsSection>
	<#if (emptyProjects?? && !emptyProjects.empty )>
	<div class="row">
	    <div class="span9" id="divEmptyProjects">
	        <h2>Empty Projects</h2>
	        <ol style='list-style-position:inside' id="emptyProjects">
	        <#list emptyProjects as res>
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
	        </#list>
	        </ol>
	    </div>
	</div>
	<hr />
	</#if>
</#macro>



<#macro browseResourceSection>
	<div class="" id="project-list">
	    <h2>Browse Resources</h2>
	    <form action=''>
	    <@edit.resourceDataTable />
	    </form>
	</div>
</#macro>

<#macro repeat num val>
 <#if (num > 0)>
  <@repeat (num-1) val /><#noescape>${val}</#noescape>
 </#if>
</#macro>

<#macro collectionsSection>
	
   <div class="">
   <h2>Collections You Created </h2>
      <@listCollections resourceCollections>
          <li><a href="<@s.url value="/collection/add"/>">create one</a></li>
      </@listCollections>
   </div>
   <br/>
   <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
     <div class="">
     <h2>Collections Shared With You</h2>
       <@listCollections sharedResourceCollections />
    </div>
  </#if>

</#macro>

<#macro accountSection>
<div id="accountSection" class="row">
	<div id="divAccountInfo" class="<#if payPerIngestEnabled>span4<#else>span9</#if>">
	<h2>About You</h2>
	    <strong>Full Name: </strong>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br />
	    <#if authenticatedUser.penultimateLogin??>
	        <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br/>
	    </#if>
	    <a href="<@s.url value='/entity/person/edit?id=${sessionData.person.id?c}'/>">edit your profile</a>
	</div>

	<#if payPerIngestEnabled>
	    <div class="span5">
	        <h2>Your Account(s)</h2>
	        <ul>
	        <#list accounts as account>
	            <li>
	                <a href="<@s.url value="/billing/${account.id?c}" />">${account.name!"unamed"}</a>
	            </li>
	        </#list>
	        <li><a href="/cart/add">Create a new account or add more to an existing one	</a></li>
	        </ul>        
	    </div>
	</#if>
</div>
</#macro>


<#macro bookmarksSection>
<div class="row">
	<div class="span9">
	<h2 id="bookmarks">Your Bookmarks</h2>
	<@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' editable=false bookmarkable=true  expanded=true listTag='ol' headerTag="h3" />
	</div>
</div>
</#macro>

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