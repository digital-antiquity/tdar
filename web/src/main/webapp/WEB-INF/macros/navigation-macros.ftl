<#-- 
$Id$ 
navigation freemarker macros
-->
<#escape _untrusted as _untrusted?html>
    <#import "resource/list-macros.ftl" as list>


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
    <#macro toolbar namespace current="view" modern=false>
         <@_toolbar>
	        <#if editable>
	            <@makeLink namespace "edit" "edit" "edit" current />
	            <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
                <@makeLink "resource" "delete?id=${resource.id?c}" "delete" "delete" current true _deleteable />
                <@makeLink "resource" "rights/${resource.id?c}" "permissions" "permissions" current false false />
	        </#if>
	        <#if resource.resourceType.project >
	            <@makeLink "resource" "add?projectId=${resource.id?c}" "add item" "add" "" false false ""/>
	        </#if>
	        <#if ((billingAccounts![])?size > 0 || config.payPerIngestEnabled == false)>
			   <@makeLink "resource" "duplicate/duplicate?id=${resource.id?c}" "duplicate" "duplicate" "" false />
			</#if>
	        <#if editable>
				<@makeLink "resource" "usage/${resource.id?c}" "usage" "usage" "" false />
			</#if>
			<#if editor>
                <@makeLink "resource" "admin?id=${resource.id?c}" "admin" "admin" "" false />					
			</#if>
			<#nested>
         </@_toolbar>
    </#macro>


    <#macro _toolbar>
        <#if (sessionData.authenticated)!false && (persistable?has_content && persistable.id > 0)>
        <div class="resource-nav  screen modern row" >
                <label class="col-form-label list-inline-item" style="color:#666">Actions:</label>
            <div class="col ml-0 pl-0">
            <ul class=" list-unstyled list-inline">
                <#nested>
            </ul>
        </div>
        </div>
        </#if>
    
    </#macro>

    <#macro collectionToolbar namespace current="view">
         <@_toolbar>
        <#if editable>
                    <@makeLink resourceCollection.urlNamespace "edit" "edit" "edit" current />
                    <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
                    <@makeLink namespace "delete?id=${persistable.id}" "delete" "delete" current true _deleteable />
                    <@makeLink namespace "usage/${persistable.id?c}" "usage" "stats" current true false />
                    <#if editor && ((resourceCollection.managedResources![])?size > 0) >
	
	                    <@makeLink "resource" "compare?collectionId=${persistable.id?c}" "review" "review" "" false />
	                    <@makeLink "export" "request?collectionId=${persistable.id}" "export" "export" current true _deleteable />
					</#if>
             <#if administrator && whiteLabelCollection>
                        <@makeLink namespace "admin/whitelabel/${persistable.id?c}/edit" "Whitelabel" "Private Label Settings" current false />             
             </#if>
            <#if editor>
                <@makeLink namespace "admin/${persistable.id?c}" "admin" "admin" "" false />                   
            </#if>
        </#if>
        <#nested>
       </@_toolbar>
    </#macro>


    <#macro billingToolbar namespace current="view">
         <@_toolbar>
    		    <#if editable>
                    <@makeLink namespace "edit" "edit" "edit" current />
                    <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
                    <@makeLink "billing" "delete?id=${persistable.id}" "delete" "delete" current true _deleteable />
                    <@makeLink "export" "request?accountId=${persistable.id}" "export" "export" current true _deleteable />
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
         </@_toolbar>
    </#macro>

<#-- emit toolbar for use on a "creator" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires creator.creatorType:string either "institution" or "person"
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->
    <#macro creatorToolbar current>
        <#if editable!false >
            <#if (persistable.registered)!false>
                <#local creatorType = "user" />
            <#elseif creator??>
                <#local creatorType = creator.creatorType?lower_case />
            <#else>
                <#local creatorType = persistable.creatorType?lower_case />
            </#if>

             <@_toolbar>
			    <#if "edit" != current>
                    <@makeLink "entity/${creatorType}" "edit" "edit" "edit" current true  />
                <#else>
                    <@makeLink "entity/${creatorType}" "edit" "edit" "edit" current true />
                </#if>
                <#if creatorType == 'user'>
                	<@makeLink "entity/user" "rights/${id?c}" "rights" "rights" current false />
				</#if>
         </@_toolbar>
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
         <@_toolbar>

            <#if "edit" != current>
                    <@makeLink "entity/keyword" "edit?keywordType=${keywordType}" "edit" "edit" current true  />
                <#else>
                    <@makeLink "entity/keyword" "edit" "edit" "edit" current true />
                </#if>
         </@_toolbar>
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

    <li class="${state} ${extraClass} list-inline-item">
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
        <#local _name = name />
        <#if name == 'mapping'><#local _name = "icon-ontology"></#if>
        <#if name == 'duplicate'><#local _name = "duplicate_mono"></#if>
        <#if name == 'stats'><#local _name = "usage"></#if>
        <#if name == 'rights'><#local _name = "permissions"></#if>
        <svg class="svgicon toolbaricon"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${_name}"></use></svg>

<#--        <#else>
            <i class="tdar-icon-${action_}<#if state?has_content>-${state}</#if>"></i>
        </#if>-->
        <#nested>${label}<#if disabled></span><#else></a></#if>
    </li>
    </#macro>


    <#-- emit "delete" button for use with repeatable form field rows -->
    <#macro clearDeleteButton id="" disabled=false title="delete this item from the list">
    <div class="col-1">
    <button class="btn btn-sm form-control  repeat-row-delete" type="button" tabindex="-1" title="${title}" <#if disabled> disabled="disabled"</#if>> <i class="fas fa-trash-alt float-right"></i></button>
    </div>
    </#macro>
    
    <#-- emit the share sidebar components without the ul  -->
    <#macro shareSection id=id title=resource.title citation=resourceCitation.fullCitation >
        <#assign url="${((request.requestURL)!'')}" />

        <li class="media"><i class="fab fa-twitter icon-push-down  mr-2 ml-1"></i>
            <div class="media-body">
                <a href="https://twitter.com/intent/tweet?url=${url?url}&text=${((title)!'')?url}" target="_blank"
                   onClick="TDAR.common.registerShare('twitter','${currentUrl?js_string}','${id?c}')">Tweet</a>
             </div>
        </li>
    
        <li class="media"><i class="far fa-thumbs-up icon-push-down  mr-2 ml-1"></i>
            <div class="media-body">
                    <a  href="http://www.facebook.com/sharer/sharer.php?u=${url?url}&amp;t=${title?url}" target="_blank"
                        onClick="TDAR.common.registerShare('facebook','${currentUrl?js_string}','${id?c}')">Like</a>
            </div>
        </li>
        <li class="media">
        <i class="far fa-envelope  mr-2 ml-1 icon-push-down"></i>
            <div class="media-body">
                <a <#noescape>href="mailto:?subject=${title?url}&amp;body=${citation!''?trim?url}%0D%0A%0D%0A${url}"</#noescape>
                    onClick="TDAR.common.registerShare('email','${currentUrl?js_string}','${id?c}')">Email</a>
             </div>
        </li>

    </#macro>
    
    
</#escape>


