<#-- 
$Id$ 
navigation freemarker macros
-->
<#escape _untrusted as _untrusted?html>
<#import "list-macros.ftl" as list>
<#import "edit-macros.ftl" as edit>


<#macro loginForm>
<script type="text/javascript">
$(document).ready(function() {
  $('#loginForm').validate({
    errorLabelContainer: $("#error"),
    messages: {
      loginUsername: {
        email: "Please enter a valid username or email address.",
        required: "Please enter a valid email address."
      },
      loginPassword: {
        required: "Please enter your password."
      }
    }
  });
  $('#loginUsername').focus();
  $('#loginUsername').bind("focusout",function() {
    var fld = $('#loginUsername');
    fld.val($.trim(fld.val()))});
});
</script>
<style type='text/css'>
.overrideCheckbox {margin-left: 9.3em !important; }
</style>

<@s.form id='loginForm' method="post" action="%{#request.contextPath}/login/process">
    <@s.textfield spellcheck="false" id='loginUsername' label="Username" name="loginUsername" cssClass="required" />
    <@s.password id='loginPassword' label="Password" name="loginPassword" cssClass="required" />

    <@s.checkbox name='userCookieSet'  label="Remember Me"/>
    <@s.submit value="Login"/>
    <#if Parameters.url??>
        <input type="hidden" name="url" value="${Parameters.url}"/>
</#if>
</@s.form>
<div id="error" style="border:1px solid red;display:none; margin:.1em;padding:.1em"></div>
<p>
<a href='<@s.url value="/account/new"/>'>Register </a> |
<a href='<@s.url value="/account/recover"/>'>Reset Password</a>
</p>
</#macro>

<#macro toolbar namespace current="view">
  <#if resource??>
    <#if resource.id == -1>
        <#return>
	</#if>
  </#if>
  <#if sessionData?? && sessionData.authenticated>
	<div class="span12 resource-nav" id="toolbars" parse="true">
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
        <li><@list.bookmark resource /></li>
        <#if resource.resourceType == "PROJECT">                
          <@makeLink "resource" "add?projectId=${resource.id?c}" "add new resource to project" "add" "" false />
        </#if>
        <#if editable>
            <#if resource.resourceType == 'DATASET'>
                <#--
                  <@makeLink "dataset" "citations" "manage citations" "citations" current>
                    <@img "/images/book_edit.png" />
                  </@makeLink>
                 -->
                    <#assign disabled = true />
                    <#if ! resource.dataTables.isEmpty() >
                        <#assign disabled = false />
                    </#if>
                    <@makeLink "dataset" "columns" "table metadata" "columns" current true disabled />

                <#elseif resource.resourceType=='CODING_SHEET'>
                    <#assign disabled = true />
                    <#if resource.defaultOntology?has_content >
                        <#assign disabled = false />
                    </#if>
                    <@makeLink "coding-sheet" "mapping" "map ontology" "mapping"   current true disabled />
                </#if>
    
        </#if>
        </#if>
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

<#macro creatorToolbar current>

    <#if editor || authenticatedUser?? && id == authenticatedUser.id>
        <#if creator??>
        <#local creatorType = creator.creatorType.toString().toLowerCase() />
        <#else>
        <#local creatorType = persistable.creatorType.toString().toLowerCase() />
        </#if>
    
  <#if sessionData?? && sessionData.authenticated>
    <div id="toolbars" parse="true">
      <ul id="toolbar" class="fg-toolbar ui-toolbar ui-widget-header ui-corner-tl ui-corner-bl ui-corner-br  ui-corner-tr ui-helper-clearfix">
    <@nav.makeLink "browse" "creators" "view" "view" current true />

    <#if "edit" != current>
	    <@nav.makeLink "entity/${creatorType}" "edit" "edit" "edit" current true  />
    <#else>
	    <@nav.makeLink "entity/${creatorType}" "edit" "edit" "edit" current true />
    </#if>
      </ul>
    </div>
  </#if>
  </#if>
</#macro>




<#macro makeLink namespace action label name current includeResourceId=true disabled=false>
	<#assign state = "" />
    <#if disabled>
    	<#assign state="disabled" />
    <#elseif current?string == name?string>
		<#assign state="active" />
	</#if>
	<li class="${state} ${action}">
                <a href='<@s.url value="/${namespace}/${action}">
	            <#if includeResourceId>
	                <#if persistable??>
	                    <#local _id = persistable.id />
	                <#else>
	                    <#local _id = creator.id />
	                </#if>
	                <@s.param name="id" value="${_id?c}" />
	            </#if>
				</@s.url>'>
                <#nested> ${label}</a>
    </li>
</#macro>

<#macro makeEditLink namespace current url="edit" label="edit">
	<@makeLink namespace url label "edit" current />
</#macro>

<#macro makeDeleteLink namespace current url="delete" label="delete">
	<#if persistable.status?? && persistable.status.toString().toLowerCase().equals('deleted')>
	  <@makeLink namespace url label "delete" current true true />
	<#else>
	  <@makeLink namespace url label "delete" current true false />
	</#if>
</#macro>

<#macro makeViewLink namespace current url="view" label="view">
	<@makeLink namespace url label "view" current />
</#macro>

<#macro img url alt="">
<img src='<@s.url value="${url}" />' <#t>
    <#if alt != ""> alt='${alt}'</#if> <#t>
 />
</#macro>


<#macro clearDeleteButton id="" disabled="false" title="delete this item from the list">
<#if disabled="true">
<button class="btn  btn-mini" type="button" tabindex="-1" onclick="deleteParentRow(this)" title="${title}" disabled="disabled"><i class="icon-trash"></i></button>
<#else>
<button class="btn  btn-mini" type="button" tabindex="-1" onclick="deleteParentRow(this)" title="${title}"><i class="icon-trash"></i></button>
</#if>
</#macro>


</#escape>
