<#-- 
$Id$ 
navigation freemarker macros
-->
<#escape _untrusted as _untrusted?html>
    <#import "list-macros.ftl" as list>


<#-- emit a toolbar for use on a resource view page
  @param namespace:string prefix of the action urls for the buttons on the toolbar (e.g. "dataset" becomes "dataset/delete"
  @param current:string? value of the action of the page being rendered
  @requires resource.id:Long a non-transient resource ID
  @requires resource.resourceType:string
  @requires sessionData:SessionData the current authenticated session
  @requires editable:boolean is resource editable by the authenticated user
  @requires persistable:Resource alias to the resource associated with the current view
  @requries ableToEditAnything:boolean override boolean indicating that the view page should render edit/delete links even if the authorized user
                would otherwise not be able to edit the current resource were they not an admin user.
  @requires authenticatedUser:Person person object of the authenticated user
-->
    <#macro toolbar namespace current="view">
        <#if resource??>
            <#if resource.id == -1>
                <#return>
            </#if>
        </#if>
        <#if (sessionData.authenticated)!false>
        <div class="span12 resource-nav  screen " id="toolbars" parse="true">
            <ul>
                <#if persistable??>
        <@makeLink namespace "view" "view" "view" current />
        <#if editable>
                    <@makeLink namespace "edit" "edit" "edit" current />
                </#if>
        <#if editable>
                    <#local _deleteable = persistable.status?? && persistable.status.toString().toLowerCase().equals('deleted') >
                    <@makeLink namespace "delete" "delete" "delete" current true _deleteable />
                </#if>
        <#if persistable.resourceType??>
                    <@list.bookmark resource true true />
                    <#if resource.resourceType == "PROJECT">
                        <@makeLink "resource" "add?projectId=${resource.id?c}" "add new resource to project" "add" "" false false "hidden-tablet hidden-phone"/>
                        <@makeLink "resource" "add?projectId=${resource.id?c}" "add item" "add" "" false false "hidden-desktop"/>
                    </#if>
                </#if>
        <#nested>
       <#elseif creator??>
                    <@makeLink namespace "view" "view" "view" current />
                    <#if ableToEditAnything>
                        <@makeLink namespace "edit" "edit" "edit" current />
                    </#if>
                <#else>
                    <@makeLink "workspace" "list" "bookmarked resources" "list" current false />
                    <@makeLink "workspace" "select-tables" "integrate data tables in your workspace" "select-tables" current false />
                </#if>
            </ul>
        </div>
        </#if>
    </#macro>

<#-- emit toolbar for use on a "creator" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires creator.creatorType:string either "institution" or "person"
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->
    <#macro creatorToolbar current>

        <#if editor || authenticatedUser?? && id == authenticatedUser.id>
            <#if creator??>
                <#local creatorType = creator.creatorType.toString().toLowerCase() />
            <#else>
                <#local creatorType = persistable.creatorType.toString().toLowerCase() />
            </#if>

            <#if sessionData?? && sessionData.authenticated>
            <div class="span12 resource-nav  screen" id="toolbars" parse="true">
                <ul>
                    <@makeLink "browse" "creators" "view" "view" current true />

    <#if "edit" != current>
                    <@makeLink "entity/${creatorType}" "edit" "edit" "edit" current true  />
                <#else>
                    <@makeLink "entity/${creatorType}" "edit" "edit" "edit" current true />
                </#if>
                </ul>
            </div>
            </#if>
        </#if>
    </#macro>

<#-- Emit a link to a page which corresponds  specified namespace and action and resourceId.  For example, <@makeLink "coding-sheet" "add">
    will emit <a href="http://{hostname}/coding-sheet/add">{label}</a>.

    If the specified URL is the same location of the current request,  this macro emits a text label instead of a link.

    Most arguments to this macro (namespace/action/label) are technically optional, however, their default values are
    are identifiers of the same name that must be defined at the time that freemarker renders this macro.
    @param namespace:string? struts namespace name
    @param action:string? struts action name
    @param label:string? text to display for the link
    @param current:string? struts action name of the current page.
 -->
    <#macro makeLink namespace=namespace action=action label=label name=name  current="" includeResourceId=true disabled=false  extraClass="">
        <#assign state = "" />
        <#if disabled>
            <#assign state="disabled" />
        <#elseif current?string == name?string>
            <#assign state="active" />
        </#if>
        <#local action_ = action/>
        <#if (action?last_index_of("?") > 0)>
            <#local action_ = action?substring(0,action?last_index_of("?")) />
        </#if>
        <#if action_ == 'creators'>
            <#local action_ = "view" />
        </#if>

    <li class="${state} ${extraClass}">
        <#if disabled>
        <span class="disabled">
        <#else>
        <a href="<#compress><@s.url value="/${namespace}/${action}">
	        <#if includeResourceId>
	            <#if persistable??>
	                <#local _id = persistable.id />
	            <#else>
	                <#local _id = creator.id />
	            </#if>
	            <@s.param name="id" value="${_id?c}" />
	        </#if>
	        </@s.url></#compress>">
        </#if>
        <i class="tdar-icon-${action_}<#if state?has_content>-${state}</#if>"></i>
        <#nested> ${label}<#if disabled></span><#else></a></#if>
    </li>
    </#macro>


<#-- emit "delete" button for use with repeatable form field rows -->
    <#macro clearDeleteButton id="" disabled=false title="delete this item from the list">
    <button class="btn  btn-mini repeat-row-delete" type="button" tabindex="-1" title="${title}" <#if disabled> disabled="disabled"</#if>><i
            class="icon-trash"></i></button>
    </#macro>
</#escape>

<#-- Return the URL associated with the current form. The URL always includes the scheme & host,  if the application uses a nonstandard
 port for the current scheme  (e.g. the https port is not 443),  the URL include scheme, host, and port -->
<#function getFormUrl absolutePath="/login/process" >
    <#local actionMethod>${absolutePath}</#local>
    <#local appPort = ""/>
    <#if httpsEnabled>
        <#if httpsPort != 443>
            <#local  appPort= ":" + httpsPort?c/>
        </#if>
        <#local actionMethod="https://${hostName}${appPort}${absolutePath}" />
    </#if>
    <#return actionMethod>
</#function>

