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
             <@makeLink2 namespace=namespace link="${persistable.id?c}/edit" label="edit" disabled=disabled icon="edit" />
            <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
             <@makeLink2 namespace="resource" link="delete?id=${persistable.id?c}" label="delete" disabled=disabled icon="delete" />
             <@makeLink2 namespace="resource" link="rights/${persistable.id?c}" label="permissions" disabled=disabled icon="permissions" />
	        <#if resource.resourceType.project >
                <@makeLink2 namespace="resource" link="add?projectId=${persistable.id?c}" label="add item" disabled=disabled icon="add" />
	        </#if>
	        <#if ((billingAccounts![])?size > 0 || config.payPerIngestEnabled == false)>
                <@makeLink2 namespace="resource" link="duplicate/duplicate?id=${persistable.id?c}" label="duplicate" disabled=disabled icon="duplicate" />
			</#if>
	        <#if editable>
                <@makeLink2 namespace="resource" link="usage/${persistable.id?c}" label="usage" disabled=disabled icon="usage" />
			</#if>
			<#nested>
			<#if editor>
                <@makeLink2 namespace="resource" link="admin?id=${persistable.id?c}" label="admin" disabled=disabled icon="admin" />
			</#if>
         </@_toolbar>
    </#macro>


    <#macro _toolbar editor=false >
    <#if sessionData.authenticated)!false == false || ((editable?has_content == false) || editable == false) && (editor?has_content == false || editor == false)>
        <#return>
    </#if>
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
             <@makeLink2 namespace=resourceCollection.urlNamespace link="${persistable.id?c}/edit" label="edit" disabled=disabled icon="edit" />
            <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
             <@makeLink2 namespace=resourceCollection.urlNamespace link="delete?id=${persistable.id?c}" label="delete" disabled=_deleteable icon="delete" />
             <@makeLink2 namespace=resourceCollection.urlNamespace link="usage/${persistable.id?c}" label="usage" disabled=false icon="usage" />
            <#if editor && ((resourceCollection.managedResources![])?size > 0) >
                    <@makeLink2 namespace="resource" link="compare?collectionId=${persistable.id?c}" label="review" disabled=false icon="review" />
                    <@makeLink2 namespace="export" link="request?collectionId=${persistable.id?c}" label="export" disabled=false icon="export" />
			</#if>
             <#if administrator && whiteLabelCollection>
                    <@makeLink2 namespace=namespace link="admin/whitelabel/${persistable.id?c}/edit" label="private label settings" disabled=false icon="settings" />
             </#if>
        <#nested>
            <#if editor>
                <@makeLink2 namespace=namespace link="admin/${persistable.id?c}" label="admin" disabled=false icon="admin" />
            </#if>
       </@_toolbar>
    </#macro>


    <#macro billingToolbar namespace current="view">
         <@_toolbar>
            <@makeLink2 namespace=namespace label="edit" icon="edit" link="${persistable.id?c}/edit" disabled=disabled />
            <#local _deleteable = (persistable.status!"")?lower_case == "deleted">
            <@makeLink2 namespace=namespace label="delete" icon="delete" link="delete?id=${persistable.id}" disabled=_deleteable />
            <@makeLink2 namespace="export" link="request?accountId=${persistable.id}" label="export" icon="export"  />
        	<@makeLink2 namespace="cart" link="add?accountId=${persistable.id?c}" label="add invoice"icon="add" />
	    	<#if administrator>
		        <@makeLink2 namespace="billing" link="updateQuotas?id=${persistable.id?c}" label="Reset Totals" icon="add"/>
	        </#if>
            <#local edit = !(editable || administrator) />
		    <#if !edit>
                <#local _large = (persistable.resources?size &gt; 50000) />
                <@makeLink2 namespace=namespace link="usage/${persistable.id?c}" label="usage" icon="usage" disabled=_large />
	        </#if>
            <@makeLink2 namespace=namespace link="transfer/${persistable.id?c}" label="transfer" icon="transfer" disabled=edit />
         </@_toolbar>
    </#macro>

<#-- emit toolbar for use on a "creator" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires creator.creatorType:string either "institution" or "person"
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->
    <#macro creatorToolbar current>
         <@_toolbar>
            <#if (persistable.registered)!false>
                <#local creatorType = "user" />
            <#elseif creator??>
                <#local creatorType = creator.creatorType?lower_case />
            <#else>
                <#local creatorType = persistable.creatorType?lower_case />
            </#if>
            <@makeLink2 namespace="entity" link="${creatorType}/${persistable.id?c}/edit" label="edit" icon="edit" />
            <#if creatorType == 'user'>
                <@makeLink2 namespace="entity/user" link="rights/${persistable.id?c}" label="rights" icon="permissions" />
			</#if>
         </@_toolbar>
    </#macro>


<#-- emit toolbar for use on a "keyword" page
    @param current:string name of the current struts action (e.g. edit/view/save)
    @requires keywordType:string
    @requires sessionData:SessionData
    @requires authenticatedUser:Person
 -->

    <#macro keywordToolbar current>
        <#if editor>
         <@_toolbar editor>
            <@makeLink2 namespace="entity/keyword" link="edit?keywordType=${keywordType}&id=${persistable.id?c}" label="edit" icon="edit" />
         </@_toolbar>
        </#if>
    </#macro>

    <#macro makeLink2 namespace="" link="" label=label disabled=false icon="">

        <li class="list-inline-item">
        <#if disabled>
            <span class="disabled">
        <#else>
            <#local localAction="/" + link />
            <#if localAction == '/'>
                <#local localAction="" />
            </#if>
            <a href="<#compress><@s.url value="/${namespace}${localAction}"/></#compress>" class="toolbar-${icon}">
        </#if>
        <svg class="svgicon toolbaricon"><use xlink:href="/images/svg/symbol-defs.svg#svg-icons_${icon}"></use></svg>

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


