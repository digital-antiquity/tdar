<#-- 
$Id$ 
navigation freemarker macros
-->
<#escape _untrusted as _untrusted?html>
<#import "list-macros.ftl" as list>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#-- emit a partial login form -->
<#macro loginForm cssClass="">
<script type="text/javascript">
$(document).ready(function() {
  $('#loginForm').validate({
    messages: {
      loginUsername: {
        required: "Please enter your username."
      },
      loginPassword: {
        required: "Please enter your password."
      }
    },
    errorClass:'help-inline',
  highlight: function(label) {
    $(label).closest('.control-group').addClass('error');
  },
  success: function($label) {
    $label.closest('.control-group').removeClass('error').addClass('success');
  }
  
    });
  $('#loginUsername').focus();
  $('#loginUsername').bind("focusout",function() {
    var fld = $('#loginUsername');
    fld.val($.trim(fld.val()))});
});
</script>
<#local formAction><@getFormUrl absolutePath="/login/process"/></#local>
<@s.form id='loginForm' method="post" action="${formAction}" cssClass="${cssClass}">
    <input type="hidden" name="url" value="${Parameters.url!''}"/>
    <@s.textfield spellcheck="false" id='loginUsername' name="loginUsername" label="Username" cssClass="required" />
    <@s.password id='loginPassword' name="loginPassword" label="Password" cssClass="required" />
    <@s.checkbox name="userCookieSet" label="Stay logged-in the next time I visit this page" />
    
    <div class="form-actions">
        <button type="submit" class="button btn btn-primary input-small submitButton" name="_tdar.Login" id="btnLogin">Login</button>
        <div class="pull-right">
            <div class="btn-group">
                <a class="btn " href='<@s.url value="/account/new"/>' rel="nofollow">Register </a> 
                <a class="btn " href='<@s.url value="/account/recover"/>' rel="nofollow">Reset Password</a>
            </div>
        </div>
    </div>
</@s.form>
<div id="error"></div>
</#macro>

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
      <ul >
       <#if persistable??>
        <@makeViewLink namespace current />
        <#if editable>
          <@makeEditLink namespace current />
        </#if>
        <#if editable>
          <@makeDeleteLink namespace current />
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
        <@makeViewLink namespace current />
        <#if ableToEditAnything>
          <@makeEditLink namespace current />
        </#if>
       <#else>
        <@makeLink "workspace" "list" "bookmarked resources" "list" current false />
        <@makeLink "workspace" "select-tables" "integrate data tables in your workspace" "select-tables" current false />
       </#if>
      </ul>
    </div>
  </#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
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
      <ul >
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

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#macro makeEditLink namespace current url="edit" label="edit">
    <@makeLink namespace url label "edit" current />
</#macro>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#macro makeDeleteLink namespace current url="delete" label="delete">
    <#if persistable.status?? && persistable.status.toString().toLowerCase().equals('deleted')>
      <@makeLink namespace url label "delete" current true true />
    <#else>
      <@makeLink namespace url label "delete" current true false />
    </#if>
</#macro>

<#-- FIXME: FTLREFACTOR remove: rarely used -->
<#macro makeViewLink namespace current url="view" label="view">
    <@makeLink namespace url label "view" current />
</#macro>


<#-- emit "delete" button for use with repeatable form field rows -->
<#macro clearDeleteButton id="" disabled=false title="delete this item from the list">
    <button class="btn  btn-mini repeat-row-delete" type="button" tabindex="-1" title="${title}" <#if disabled> disabled="disabled"</#if>><i class="icon-trash"></i></button>
</#macro>

<#-- emit the URL associated with the current form. The URL always includes the scheme & host,  if the application uses a nonstandard
 port for the current scheme  (e.g. the https port is not 443),  the URL include scheme, host, and port -->
<#macro getFormUrl absolutePath="/login/process">
<#compress>
<#-- NOTE: as Jim says, this can be done insetad with an @s.url scheme="https|http", but with tDAR running on so-many ports 
    in testing, I'm not sure if the right way is the best way for us  -->
<#assign actionMethod>${absolutePath}</#assign>
<#if httpsEnabled>
    <#assign appPort = ""/>
    <#if httpsPort != 443>
        <#assign appPort= ":" + httpsPort?c/>
    </#if>
    <#assign actionMethod>https://${hostName}${appPort}${absolutePath}</#assign>
</#if>
${actionMethod}
</#compress>
</#macro>


</#escape>
