<#escape _untrusted as _untrusted?html>
<#macro sidebar current="dashboard">
    <ul class="nav nav-pills nav-fill nav-list nav-stacked dashboard-nav">
        <li class="nav-header"><b>Dashboard</b></li>

        <li class="nav-item"><a  <@activeIf current "dashboard" /> href="/dashboard">Resources</a></li>
<!--        <li class="nav-item"><a  <@activeIf current "collections" /> href="/dashboard/collections">Collections</a></li> -->
	<#if contributor>
        <li class="nav-item"><a  <@activeIf current "rights" /> href="/dashboard/rights">Collections</a></li>
    </#if>
        <li class="nav-item"><a  <@activeIf current "bookmarks" /> href="/dashboard/bookmarks"> Bookmarks</a></li>

        <#--<li <@activeIf current "share" />><a href="/share">Share</a></li>-->

    <#if editor>
        <li class="nav-item"><a  <@activeIf current "files" /> href="/dashboard/files">Files</a></li>
    </#if>


        <#--<li class="nav-header">My Library</li>-->
        <li class="nav-item" ><a <@activeIf current "myprofile"/> href="/entity/user/myprofile">My Profile</a></li>
        <#if contributor>
            <li class="nav-item" ><a <@activeIf current "billing" /> href="/dashboard/billing">Billing Accounts</a></li>
            <li class="nav-item" ><a <@activeIf current "export" /> href="/export/request">Export</a></li>
        </#if>
    </ul>
</#macro>

<#macro activeIf current test>
class="<#if current == test>active<#else></#if> nav-link"
</#macro>


<#macro headerNotifications>
    <#list currentNotifications as notification>
        <div class="${notification.messageType} alert alert-warning" id="note_${notification.id?c}" role="alert">
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

<#macro collectionLegend collection>
<span class="collection-embed pull-right">
            <span title="# of resources"><i class="icon-file"></i> ${((collection.unmanagedResources?size)!0 + collection.managedResources?size!0)} Resources</span>
            <#local children= (collection.transientChildren?size)!0/>
            <#local users= (collection.authorizedUsers?size)!0/>
            
            <#local folder = "icon-folder-close" />
            <#if (children > 0)>
                <#local folder="icon-folder-open" />
            </#if>
            <span title="# of collections"><i class="${folder}"></i> ${children} Child Collections</span>
            <span title="# of users"><i class="icon-user"></i> ${users} Users</span>
<#--             <#if (children + users > 0)><li><i class="icon-circle-arrow-right" data-toggle="collapse" data-target="#details${collection.id?c}"></i> </li></#if> 
        </ul>
-->
        <#-- 
        <div id="details${collection.id?c}" class="collapse">
          <ul class="">
            <#list collection.transientChildren><li><i class="${folder}"></i> <#items as child>${child.name}<#sep> <i class="${folder}"></i> </#sep></#items></li></#list>
            <#list collection.authorizedUsers><li><i class="icon-user"></i> <#items as user>${user.user.properName} (${user.generalPermission.label}) <#sep><i class="icon-user"></i></#sep> </#items></li></#list>
        </ul>
       </div> -->
   </span>
 
</#macro>

</#escape>