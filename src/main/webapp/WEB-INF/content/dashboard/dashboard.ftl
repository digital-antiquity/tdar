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

    <#if payPerIngestEnabled>
    <div class="news alert" id="alert-charging">
		<button type="button" class="close" data-dismiss="alert" data-dismiss-cookie="alert-charging">&times;</button>
        <B>${siteAcronym} Update:</B>
        Please note we are now charging to upload materials to ${siteAcronym}, please see <a href="http://www.tdar.org/about/pricing"> our website</a> for more information. 
        <br/>
        <br/>
        <#if (authenticatedUser.id < 145165 )> 
        If you are a contributor who uploaded files to tDAR during the free period, we've generated an account for those files.  As a thank you for your support, we have credited your account with one additional file (up to 10 MB, a $50 value) to get your next project started.  </#if>
        <br/>
    </div>
    </#if>

    <div class="news alert" id="alert-jar">
		<button type="button" class="close" data-dismiss="alert" data-dismiss-cookie="alert-jar">&times;</button>
        <B>${siteAcronym} Update:</B>
        Welcome to Jar! What's new?
        <br/>
    </div>
</div>


<#if overdrawnAccounts?has_content>
<div class="alert-error alert">
<h3>The following accounts are overdrawn</h3>
<p>An overdrawn account means that some resources will not be visible or usable within tDAR, and may ultimately be deleted or removed from tDAR.  To address this, please <a href="<@s.url value="/cart/add"/>">purchase more space or files</a> to cover the additional usage.</p>
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
<#if contributor>
	<#if (activeResourceCount != 0)>
		<@resourcePieChart />
		<hr/>
	</#if>
</#if>
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
    <p><strong>Jump To:</strong><a href="#project-list">Browse Resources</a> | <a href="#collection-section">Collections</a> | <a href="#divAccountInfo">Your Profile</a> | <a href="#billing">Billing Accounts</a> | <a href="#boomkarks">Bookmarks</a>
    </p>
    <hr/>
    </div>
</div>


<#if contributor>
	<#if (activeResourceCount == 0)>
		<@gettingStarted />
	<hr /> 
	<#else>
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
	    <div class="span3">
	        <h2>At a glance</h2>
	            <div class="piechart row">
	            <@common.generatePieJson statusCountForUser "statusForUser" />
	            <@common.barGraph  data="statusForUser" searchKey="includedStatuses" graphHeight=150 context=true graphLabel="Your Resources By Status"/>
		     </div>
	         <div class="piechart row">
 	            <@common.generatePieJson resourceCountForUser "resourceCountForUser" />
 	            <script>
 	            var pcconfig = {
 	               legend: { show:true, location: 's', rendererOptions: {numberColumns: 3} }
 	               };
 	            </script>
	            <@common.pieChart  data="resourceCountForUser" searchKey="resourceTypes" graphHeight=300 context=true config="pcconfig" graphLabel="Your Resources By Type"/>
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
	                        <a href="<@s.url value='/${res.urlNamespace}/view'
	                           ><@s.param name="id" value="${res.id?c}"/></@s.url>"><@common.truncate res.title 60 /></a>
                            <small>(ID: ${res.id?c})</small>
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
	        <ol id="emptyProjects">
	        <#list emptyProjects as res>
	        <li id="li-recent-resource-${res.id?c}">
	                <a href="<@s.url value='/${res.urlNamespace}/view'><@s.param name="id" value="${res.id?c}"/></@s.url>">
	                    <@common.truncate res.title 60 />
	                </a>
                    <small>(ID: ${res.id?c})</small>
	                 
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
	
   <div class="" id="collection-section">
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
	    <div class="span5" id="billing">
	       <@common.billingAccountList accounts />
	    </div>
	</#if>
</div>
</#macro>


<#macro bookmarksSection>
<div class="row">
	<div class="span9">
	<h2 id="bookmarks">Your Bookmarks</h2>
	<#if bookmarkedResources??>
	<#--	   <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' editable=false bookmarkable=true  orientation='LIST_LONG' listTag='ol' headerTag="h3" /> -->
	
	   <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' editable=false bookmarkable=true  listTag='ol' headerTag="h3" />
	</#if>
	</div>
</div>
</#macro>

<#macro collectionListItem collection>
	<#compress><li>
    <a href="<@s.url value="/collection/${collection.id?c}"/>">
    <#if collection.name?? && collection.name != ''>${collection.name!"no title"}<#t/><#else>No Title</#if> (${collection.resources?size})</a>
          <#if collection.transientChildren?has_content>
			<ul>
		        <#list collection.transientChildren as child>
		        	<@collectionListItem child />
				</#list>
			</ul>          	
          </#if>
	</li></#compress>
</#macro>

<#macro listCollections resourceCollections_ >
      <ul>
        <#list resourceCollections_ as collection>
        	<@collectionListItem collection />
        </#list>
      <#nested>
      </ul>

</#macro>
<script>
$(document).ready(function() {
	$("[data-dismiss-cookie]").each(function(){
		var $this = $(this);
		var id = $this.data('dismiss-cookie');
		if ($.cookie(id)) {
			$("#"+id).hide();
		} else {
			$this.click(function() {
				$.cookie(id, id);
			});
		}
	});
});
</script>
</#escape>