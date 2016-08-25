<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
    <#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/search/search-macros.ftl" as search>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>
    <#import "/${themeDir}/settings.ftl" as settings>

<head>
    <title>${authenticatedUser.properName}'s Dashboard</title>
    <meta name="lastModifiedDate" content="$Date$"/>
    <@edit.resourceDataTableJavascript />
</head>

<div id="titlebar" parse="true">
    <h1>${authenticatedUser.properName}'s Dashboard</h1>

        <@headerNotifications />
</div>

<div id="sidebar-right" parse="true">
    <div>
    	<div id="myProfile">
            <h2>About ${authenticatedUser.firstName}</h2>
            <strong>Profile:</strong>
			<a href="<@s.url value="/browse/creators/${authenticatedUser.id?c}"/>">${authenticatedUser.properName}</a>
			<#if authenticatedUser.institution??>
			<br><strong>Institution:</strong>
<a href="<@s.url value="/browse/creators/${authenticatedUser.institution.id?c}"/>">${authenticatedUser.institution.properName}</a></#if><br>
            <#if authenticatedUser.penultimateLogin??>
                <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br>
            </#if><br>
            <a class="button btn" href="<@s.url value='/entity/user/edit?id=${authenticatedUser.id?c}'/>">edit your profile</a>
            <hr/>
    	</div>
        <#if contributor>
            <#if (activeResourceCount != 0)>
                <@resourcePieChart />
                <hr/>
            </#if>
        <#else>
            <div id="myCarousel" class="carousel slide" data-interval="5000" data-pause="hover">
                <#assign showBuy = (!accounts?has_content) />
              <!-- Carousel items -->
              <div class="carousel-inner">
                <div class="active item">
                    <a href="${documentationUrl}">
                        <img class="" src="<@s.url value="/images/dashboard/learn.png"/>" width=120 height=150 alt="Read the Manual"/>
                            Read the Manual
                    </a>
                </div>
                    <#if (showBuy)>
                    <div class="item">
                        <a href="<@s.url value="/cart/add"/>">
                            <img class="" src="<@s.url value="/images/dashboard/upload.png"/>" width=120 height=150 alt="Purchase Space"/>
                                Buy tDAR now
                        </a>
                    </div>
                    </#if>
                <div class="item">
                        <a href="<@s.url value="/browse/explore"/>">
                            <img class="" src="<@s.url value="/images/dashboard/explore.png"/>" width=120 height=150 alt="Explore"/>
                                Explore Content now
                        </a>
                </div>
              </div>
              <div class="clearfix centered">
              <ol class="carousel-indicators" >
                <li data-target="#myCarousel" data-slide-to="0" class="active"></li>
                <#if showBuy><li data-target="#myCarousel" data-slide-to="1"></li></#if>
                <li data-target="#myCarousel" data-slide-to="2"></li>
              </ol>
                <!-- Carousel nav 
  <a class="carousel-control left" href="#myCarousel" data-slide="prev">&lsaquo;</a>
  <a class="carousel-control right" href="#myCarousel" data-slide="next">&rsaquo;</a>
                -->
              </div>

        </div>
        </#if>
        <@collectionsSection />
    </div>
</div>

<div class="row">
    <div class="span9">
        Welcome <#if authenticated.penultimateLogin?has_content>back,</#if> ${authenticatedUser.firstName}!
        <#if contributor>
            The resources you can access are listed below. To create a <a href="<@s.url value="/resource/add"/>">new resource</a> or
            <a href="<@s.url value="/project/add"/>">project</a>, or <a href="<@s.url value="/collection/add"/>">collection</a>, click on the "upload" button
            above.
        <p><strong>Jump To:</strong> <a href="#project-list">Browse Resources</a> | <a href="#collection-section">Collections</a> | <a href="#divAccountInfo">Profile</a>
            <#if payPerIngestEnabled>| <a href="#billing">Billing Accounts</a></#if>
            | <a href="#boomkarks">Bookmarks</a>
        </p>
        <hr/>
        </#if>
    </div>
</div>


    <#if contributor>
        <#if (activeResourceCount == 0)>
            <@gettingStarted />
        <hr/>
        <#else>
            <@recentlyUpdatedSection />
        </#if>

        <@emptyProjectsSection />
        <@browseResourceSection />
    <#else>
    <@searchSection />
    <#if featuredResources?has_content  >
    <hr/>
    <div class="row">
        <@view.featured colspan="9" header="Featured and Recent Content"/>
    </div>
    </#if>
    </#if>
<hr/>
    <@accountSection />
<hr/>

    <@bookmarksSection />


<#macro searchSection>
    <div class="row">
        <div class="span9">
            <form name="searchheader" action="<@s.url value="/search/results"/>">
                <input type="text" name="query" class="searchbox" placeholder="Search ${siteAcronym} &hellip; ">
                <@s.checkboxlist id="includedResourceTypes" numColumns=4 spanClass="span2" name='resourceTypes' list='resourceTypes'  listValue='label' label="Resource Type"/>
                <@s.submit value="Search" cssClass="btn btn-primary" />
            </form>
        </div>    
    </div>

</#macro>




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
    <div>
            <h2>At a glance</h2>

            <div class="pieChart" id="statusChart" data-columns="#statusTypeData" style="height:200px" data-click="dashboardStatusPieChartClick">
            </div>
            
            <div class="pieChart" id="resourceTypeChart" data-columns="#resourceTypeData" style="height:200px" data-click="dashboardResourcePieChartClick">
            </div>
            
            <#noescape>
            <script id='statusTypeData'>
            ${statusData}
			</script>

            <script id='resourceTypeData'>
            ${resourceTypeData}
            </script>
            </#noescape>
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
	                    <a href="<@s.url value='/resource/delete?'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
	                </span>
	                        <a href="<@s.url value='${res.detailUrl}' />"><@common.truncate res.title 60 /></a>
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
                        <li id="li-empty-project-${res.id?c}">
                            <a href="<@s.url value="${res.detailUrl}"/>">
                                <@common.truncate res.title 60 />
                            </a>
                            <small>(ID: ${res.id?c})</small>

                            <div class="recent-nav pull-right">
                                <a href="<@s.url value='/resource/add?projectId=${res.id?c}'><@s.param name="id" value="${res.id?c}"/></@s.url>"
                                   title="add a resource to this project">add resource</a> |
                                <a href="<@s.url value='/${res.urlNamespace}/edit'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.edit" /></a>
                                |
			                    <a href="<@s.url value='/resource/delete?'><@s.param name="id" value="${res.id?c}"/></@s.url>"><@s.text name="menu.delete" /></a>
                            </div>
                        </li>
                    </#list>
                </ol>
            </div>
        </div>
        <hr/>
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
        <h2>Collections</h2>
        <@common.listCollections collections=allResourceCollections showBadge=true>
            <li><a href="<@s.url value="/collection/add"/>">create one</a></li>
        </@common.listCollections>
    </div>
    <br>
        <#if sharedResourceCollections?? && !sharedResourceCollections.empty >
        <div class="">
            <h2>Collections Shared With You</h2>
            <@common.listCollections collections=sharedResourceCollections showBadge=true/>
        </div>
        </#if>

    </#macro>

    <#macro accountSection>
    <div id="accountSection" class="row">
        <div id="divAccountInfo" class="<#if payPerIngestEnabled>span4<#else>span9</#if>">
            <h2>About ${authenticatedUser.firstName}</h2>
            <strong>Full Name: </strong>${authenticatedUser.properName}<#if authenticatedUser.institution??>, ${authenticatedUser.institution.name}</#if><br>
            <#if authenticatedUser.penultimateLogin??>
                <strong>Last Login: </strong>${authenticatedUser.penultimateLogin?datetime}<br>
            </#if>
            <a href="<@s.url value='/entity/user/edit?id=${authenticatedUser.id?c}'/>">edit your profile</a><br>
            <a href="<@s.url value='/export/request'/>">Export</a>
        </div>

        <div class="span5" id="billing">
            <@common.billingAccountList accounts />
        </div>
    </div>
    </#macro>


    <#macro bookmarksSection>
    <div class="row">
        <div class="span9" id="bookmarks">
            <#if ( bookmarkedResources?size > 0)>
            <h2 >Bookmarks</h2>
                <@rlist.listResources resourcelist=bookmarkedResources sortfield='RESOURCE_TYPE' listTag='ol' headerTag="h3" />
            <#else>
            <h3>Bookmarked resources appear in this section</h3>
            Bookmarks are a quick and useful way to access resources from your dashboard. To bookmark a resource, click on the star <i class="icon-star"></i> icon next to any resource's title.
            </#if>
        </div>
    </div>
    </#macro>

<script>
    $(document).ready(function () {
        TDAR.notifications.init();
        TDAR.common.collectionTreeview();
        $("#myCarousel").carousel('cycle');
    });
</script>



<#macro headerNotifications>
    <#list currentNotifications as notification>
        <div class="${notification.messageType} alert" id="note_${notification.id?c}">
        <button type="button" id="close_note_${notification.id?c}" class="close" data-dismiss="alert" data-dismiss-id="${notification.id?c}" >&times;</button>
        <#if notification.messageDisplayType.normal>
        <@s.text name="${notification.messageKey}"/> [${notification.dateCreated?date?string.short}]
        <#else>
            <#local file = "../notifications/${notification.messageKey}.ftl" />
            <#if !notification.messageKey?string?contains("..") >
                <#attempt>
                    <#include file />
                <#recover>
                    Could not load notification.
                </#attempt>
            </#if>
        </#if>
        </div>
    </#list>

    <#list resourcesWithErrors![]>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.archiving_heading"/></h3>

        <p><@common.localText "dashboard.archiving_errors", serviceProvider, serviceProvider /> </p>
        <ul>
            <#items as resource>
                <li>
                    <a href="<@s.url value="${resource.detailUrl}" />">${resource.title}:
                        <#list resource.filesWithProcessingErrors as file><#if file_index !=0>,</#if>${file.filename!"unknown"}</#list>
                    </a>
                </li>
            </#items>
        </ul>
    </div>
    </#list>

<#if showUserSuggestions!false>
	<#list userSuggestions>
    <div class="alert-error alert">
        <h3>Are any of these you?</h3>
		<ul class="unstyled">
			<#items as sug>
				<li><input type="checkbox" name="merge" value="${sug.id?c}-${sug.properName}">&nbsp;<@s.a value="${sug.detailUrl}">${sug.properName}</@s.a></li>
			</#items>
		</ul>
		<form>
		<!-- fixme - send email via email-controller 
			construct list of people; contruct url for -- dedup /admin/authority/select-authority?selectedDupeIds=...&entityType=PERSON -->
		<button name="yes" class="button btn">Yes</button>
		<button name="no"  class="button btn">No</button>
		</form>
    </div>
	</#list>
</#if>
    <#list overdrawnAccounts![]>
    <div class="alert-error alert">
        <h3><@s.text name="dashboard.overdrawn_title"/></h3>

        <p><@s.text name="dashboard.overdrawn_description" />
            <a href="<@s.url value="/cart/add"/>"><@s.text name="dashboard.overdrawn_purchase_link_text" /></a>
        </p>
        <ul>
            <#items as account>
                <li>
                    <a href="<@s.url value="/billing/${account.id?c}" />">${account.name!"unamed"}</a>
                </li>
            </#items>
        </ul>
    </div>
    </#list>

</#macro>

</#escape>
