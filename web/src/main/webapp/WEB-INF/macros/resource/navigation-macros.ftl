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
			            <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
			            <@makeLink "resource" "delete?id=${resource.id}" "delete" "delete" current true _deleteable />
			        </#if>
			        <@list.bookmark resource true true />
			        <#if resource.resourceType.project >
			            <@makeLink "resource" "add?projectId=${resource.id?c}" "add new resource to project" "add" "" false false "hidden-tablet hidden-phone"/>
			            <@makeLink "resource" "add?projectId=${resource.id?c}" "add item" "add" "" false false "hidden-desktop"/>
			        </#if>
					<@makeLink "resource" "duplicate/duplicate?id=${resource.id?c}" "duplicate" "duplicate" "" false />
			        <#if editable>
						<@makeLink "resource" "usage/${resource.id?c}" "usage" "usage" "" false />
					</#if>
			    </#if>
			    <#nested>
			</ul>
		</div>
		</#if>
    </#macro>


    <#macro collectionToolbar namespace current="view">
        <#if persistable??>
        <#if (sessionData.authenticated)!false>
        <div class="span12 resource-nav  screen " id="toolbars" parse="true">
            <ul>
        <@makeLink namespace "view" "view" "view" current />
        <#if editable>
                    <@makeLink namespace "edit" "edit" "edit" current />
                    <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
                    <@makeLink namespace "delete?id=${persistable.id}" "delete" "delete" current true _deleteable />
                    <#local _large = (persistable.resources?size &gt; 50000) />
                    <@makeLink namespace "usage/${persistable.id?c}" "usage" "stats" current true _large />

             <#if administrator && whiteLabelCollection>
                        <@makeLink namespace "admin/${persistable.id?c}/edit" "Whitelabel" "Private Label Settings" current false />             
             </#if>
        </#if>
        <#nested>
			</ul>
		</div>

			</#if>
		</#if>
    </#macro>


    <#macro billingToolbar namespace current="view">
        <#if persistable??>
        <#if (sessionData.authenticated)!false>
        <div class="span12 resource-nav  screen " id="toolbars" parse="true">
            <ul>
	        	<@makeLink namespace "view" "view" "view" current />
    		    <#if editable>
                    <@makeLink namespace "edit" "edit" "edit" current />
                    <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
                    <@makeLink "billing" "delete?id=${persistable.id}" "delete" "delete" current true _deleteable />
		        </#if>
	        	<@makeLink "cart" "add?accountId=${persistable.id?c}" "add invoice" "add" "" false false />
    	    	<#if administrator>
    		        <@makeLink "billing" "updateQuotas?id=${persistable.id?c}" "Reset Totals" "add" "" false false />
		        </#if>
                <#local edit = !(editable || administrator) />
    		    <#if !edit>
                    <#local _large = (persistable.resources?size &gt; 50000) />
	                <@makeLink namespace "usage/${persistable.id?c}" "usage" "stats" current true _large />
		        </#if>
                <@makeLink namespace "transfer/${persistable.id?c}" "transfer" "transfer" current true edit />
			</ul>
		</div>

			</#if>
		</#if>
    </#macro>

<#-- emit toolbar for use on a "creator" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires creator.creatorType:string either "institution" or "person"
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->
    <#macro creatorToolbar current>

        <#if editable >
            <#if (persistable.registered)!false>
                <#local creatorType = "user" />
            <#elseif creator??>
                <#local creatorType = creator.creatorType?lower_case />
            <#else>
                <#local creatorType = persistable.creatorType?lower_case />
            </#if>

            <#if (sessionData.authenticated)!false>
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


<#-- emit toolbar for use on a "keyword" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires keywordType:string
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->

    <#macro keywordToolbar current>

        <#if editor>
            <div class="span12 resource-nav  screen" id="toolbars" parse="true">
                <ul>
                    <@makeLink keyword.urlNamespace "" "view" "view" current true />

            <#if "edit" != current>
                    <@makeLink "entity/keyword" "edit?keywordType=${keywordType}" "edit" "edit" current true  />
                <#else>
                    <@makeLink "entity/keyword" "edit" "edit" "edit" current true />
                </#if>
                </ul>
            </div>
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
        <#local localAction="/" + action />
        <#if localAction == '/'>
            <#local localAction="" />
        </#if>
        <#if persistable??>
            <#local _id = persistable.id />
        <#elseif creator?? >
            <#local _id = creator.id />
        <#elseif keyword?? >
            <#local _id = keyword.id />
        </#if>

        <#if action?contains('columns') || action?contains("usage/") >
            <#local includeResourceId = false/>
        </#if>


		<#if action == 'view' || action == "creators" || action == 'stats' >
			<#local includeResourceId = false/>
			<#local localAction="/${_id?c}"/>
            <#if action == "creators">
                <#local localAction="/creators/${_id?c}"/>
            </#if>
		</#if>
        <a href="<#compress><@s.url value="/${namespace}${localAction}">
	        <#if includeResourceId>
	            <@s.param name="id" value="${_id?c}" />
	        </#if>
	        </@s.url></#compress>" class="toolbar-${name}">
        </#if>
        <i class="tdar-icon-${action_}<#if state?has_content>-${state}</#if>"></i>
        <#nested>${label}<#if disabled></span><#else></a></#if>
    </li>
    </#macro>


<#-- emit "delete" button for use with repeatable form field rows -->
    <#macro clearDeleteButton id="" disabled=false title="delete this item from the list">
    <button class="btn btn-mini repeat-row-delete" type="button" tabindex="-1" title="${title}" <#if disabled> disabled="disabled"</#if>><i class="icon-trash"></i></button>
    </#macro>
</#escape>


