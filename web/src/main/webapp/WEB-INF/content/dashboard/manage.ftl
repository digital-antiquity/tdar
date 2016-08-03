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
    <h1>Manage Rights &amp; Permissions</h1>
</div>
<div class="row">
<div class="span8">
        <@collectionsSection />
</div>
<div class="span4">
    <div>
    
        <h4>Grant Access</h4>
        <p><b>Grant access to the following users:</b></p>
        <div id="divAccessRights" data-tiplabel="Access Rights" data-tooltipcontent="#divCollectionAccessRightsTips" data-original-title="" title="">
        <div id="accessRightsRecords" class="repeatLastRow" data-addanother="add another user">
            <div class="control-group">
                <div class="controls">
                            <div class="controls-row repeat-row" id="authorizedUsersRow_0_">
                                <div class="span6">


    <div id="authorizedUsersRow_0_p" class="creatorPerson  ">
<input type="hidden" name="authorizedUsers[0].user.id" value="139906" id="authorizedUsersId__id_0_p" autocompleteparentelement="#authorizedUsersRow_0_p">        <div class="controls-row">
            
<input type="text" name="authorizedUsersFullNames[0]" maxlength="255" value="Ruth Van Dyke" id="metadataForm_authorizedUsersFullNames_0_" class="span3 userAutoComplete notValidIfIdEmpty  " autocompleteparentelement="#authorizedUsersRow_0_p" data-msg-notvalidifidempty="Invalid user name.  Please type a name (or partial name) and choose one of the options from the menu that appears below." autocomplete="off" placeholder="Name" autocompletename="properName" autocompleteidelement="#authorizedUsersId__id_0_p">
<select name="authorizedUsers[0].generalPermission" id="metadataForm_authorizedUsers_0__generalPermission" class="creator-rights-select span3">
    <option value="VIEW_ALL" selected="selected">View and Download</option>
    <option value="MODIFY_METADATA">Modify Metadata</option>
    <option value="MODIFY_RECORD">Modify Files &amp; Metadata</option>
    <option value="ADMINISTER_GROUP">Add/Remove Items from Collection</option>


</select>
<input type="hidden" name="authorizedUsers[0].generalPermission" value="VIEW_ALL" id="authorizedUsers[0]hdnGeneralPermission" class="repeat-row-remove">        </div>
    </div>
                                </div>
                                <div class="span1">
    <button class="btn btn-mini repeat-row-delete" type="button" tabindex="-1" title="delete this item from the list"><i class="icon-trash"></i></button>
                                </div>
                            </div>
                </div>
            </div>
        </div><div class="control-group add-another-control"><div class="controls"><button class="btn addanother" id="accessRightsRecordsAddAnotherButton" type="button"><i class="icon-plus-sign"></i>add another</button></div></div>



    </div>
        
                
        <p><b>To:</b></p>
    <div class="" id="project-list">
        <div>
            <@edit.resourceDataTable />
        </div>
    </div>
        
        
        
    </div>

</div>
</div>

    <#macro repeat num val>
        <#if (num > 0)>
            <@repeat (num-1) val /><#noescape>${val}</#noescape>
        </#if>
    </#macro>

    <#macro collectionsSection>

    <div class="" id="collection-section">
        <h2>Rights Groups</h2>
        <table class="table">
            <thead>
                <th>Name</th>
                <th># of resources</th>
                <th># of users</th>
                <th>action</th>
            </thead>
            <tbody>
            <#list allResourceCollections as collection>
                <tr>
                <td>${collection.name}</td>
                <td>${collection.resources?size}</td>
                <td>${collection.authorizedUsers?size}</td>
                <td>edit | delete | details</td>
                </tr>
                </#list>
            </tbody>
        </table>
        <a href="<@s.url value="/collection/add"/>">create one</a>
    </div>
    <br>
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
