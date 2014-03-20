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
		<button type="button" class="close" data-dismiss="alert" data-dismiss-cookie="alert-knap">&times;</button>
        <B>${siteAcronym} Update:</B>
        Welcome to Knap! Learn all about what has changed <a href="http://www.tdar.org/news/2014/04/tdar-software-update-knap/">here</a>.
        <br/>
    </div>
</div>


<#if resourcesWithErrors?has_content>
<div class="alert-error alert">
<h3><@s.text name="dashboard.archiving_heading"/></h3>
<p><@common.localtext "dashboard.archiving_errors", serviceProvider, serviceProvider /> </p>
<ul> 
<#list resourcesWithErrors as resource>
   <li>
        <a href="<@s.url value="/${resource.resourceType.urlNamespace}/${resource.id?c}" />">${resource.title}:
        <#list resource.filesWithProcessingErrors as file><#if file_index !=0>,</#if>${file.filename!"unknown"}</#list>
        </a>
    </li>
</#list>
</ul>
</div>
</#if>


<#if overdrawnAccounts?has_content>
<div class="alert-error alert">
<h3><@s.text name="dashboard.overdrawn_title"/></h3>
<p><@s.text name="dashboard.overdrawn_description" />
	<a href="<@s.url value="/cart/add"/>"><@s.text name="dashboard.overdrawn_purchase_link_text" /></a>
	</p>
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
    <@common.localtext "dashboard.ie_warning", siteAcronym, "<a href=\"http://www.microsoft.com/ie\" target=\"_blank\">","</a>" />
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
    <p><strong>Jump To:</strong><a href="#project-list">Browse Resources</a> | <a href="#collection-section">Collections</a> | <a href="#divAccountInfo">Profile</a> | <a href="#billing">Billing Accounts</a> | <a href="#boomkarks">Bookmarks</a>
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
	            <@common.barGraph  data="statusForUser" searchKey="includedStatuses" graphHeight=150 context=true graphLabel="Resources By Status"/>
		     </div>
	         <div class="piechart row">
 	            <@common.generatePieJson resourceCountForUser "resourceCountForUser" />
 	            <script>
 	            var pcconfig = {
 	               legend: { show:true, location: 's', rendererOptions: {numberColumns: 3} }
 	               };
 	            </script>
	            <@common.pieChart  data="resourceCountForUser" searchKey="resourceTypes" graphHeight=280 context=true config="pcconfig" graphLabel="Resources By Type"/>
	        </div>
	    </div>
	</div>

</#macro>

<#macro recentlyUpdatedSection>

	<div class="row">
	    <div class="span9">
	        <h2><@s.text name="dashboard.recently_updated"/></h2>
	        <ol id='recentlyEditedResources'>
	          
	            <#list recentlyEditedResources as res>
	            <li id="li-recent-resource-${res.id?c}">
	               <span class="fixed">
	                        <@common.cartouche res true>
	                <span class="recent-nav">
	                    <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a> |
	                    <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
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
	                <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a> |
	                <a href="<@s.url value='/${res.urlNamespace}/delete'><@s.param name="id" value="${res.id?c}"/></@s.url>" ><@s.text name="menu.delete" /></a>
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
    <@common.reindexingNote />
	<div class="" id="project-list">
	    <h2>Browse Resources</h2>
	    <div>
	        <@edit.resourceDataTable />
	    </div>
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
      <@common.listCollections collections=resourceCollections>
          <li><a href="<@s.url value="/collection/add"/>">create one</a></li>
      </@common.listCollections>
   </div>
   <br/>
   <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
     <div class="">
     <h2>Collections Shared With You</h2>
       <@common.listCollections collections=sharedResourceCollections />
    </div>
  </#if>

</#macro>

<#macro accountSection>
<div id="accountSection" class="row">
	<div id="divAccountInfo" class="<#if payPerIngestEnabled>span4<#else>span9</#if>">
	<h2>About ${authenticatedUser.firstName}</h2>
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
	<h2 id="bookmarks">Bookmarks</h2>
	<#if bookmarkedResources??>
	<#--	   <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE'  bookmarkable=true  orientation='LIST_LONG' listTag='ol' headerTag="h3" /> -->
	
	   <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' listTag='ol' headerTag="h3" />
	</#if>
	</div>
</div>
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

    TDAR.common.collectionTreeview();
});
</script>
</#escape>