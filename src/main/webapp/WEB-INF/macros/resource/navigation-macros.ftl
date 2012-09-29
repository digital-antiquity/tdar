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
    <@s.textfield spellcheck="false" id='loginUsername' label="Username" name="loginUsername" cssClass="required" /><br/>
    <@s.password id='loginPassword' label="Password" name="loginPassword" cssClass="required" /><br/>
<div class="field"></div>
 <label for="loginForm_userCookieSet">Remember me?</label><@s.checkbox name='userCookieSet' cssClass="overrideCheckbox"/><br/>
    <@s.submit value="Login"/>
    <#if Parameters.url??>
        <input type="hidden" name="url" value="${Parameters.url}"/>
</#if>
</@s.form>
<div id="error" style="border:1px solid red;display:none; margin:.1em;padding:.1em"></div>
<p style="margin-left: 9.2em;">
<a href='<@s.url value="/account/new"/>'>Register </a> |
<a href='<@s.url value="/account/recover"/>'>Reset Password</a>

</p>
</#macro>

<#macro toolbar namespace current="view">
  <#if resource??>
    <#-- FIXME: this is a bit of a hack, but we need a queuedFileTemplate table
    outside of the form for file uploads.  Consider renaming this to ..?-->
    <#if resource.resourceType != "PROJECT" && current == "edit">
    <table style="display:none;visibility:hidden" id="queuedFileTemplate">
        <@edit.fileProxyRow />
    </table>
    </#if>
    <#if resource.id == -1>
        <#return>
    </#if>
  </#if>
  <#if sessionData?? && sessionData.authenticated>
    <div id="toolbars" parse="true">
      <ul id="toolbar" class="fg-toolbar ui-toolbar ui-widget-header ui-corner-tl ui-corner-bl ui-corner-br  ui-corner-tr ui-helper-clearfix">
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
          <@makeLink "resource" "add?projectId=${resource.id?c}" "add new resource to project" "add" "" false>
            <@img "/images/database_add.png"/> 
          </@makeLink>
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
		            <@makeLink "dataset" "columns" "table metadata" "columns" current true disabled>
		            <@img "/images/database_table.png" />
		            </@makeLink>

		        <#elseif resource.resourceType=='CODING_SHEET'>
		        	<#assign disabled = true />
		        	<#if resource.defaultOntology?has_content >
			        	<#assign disabled = false />
			        </#if>
		            <@makeLink "coding-sheet" "mapping" "map ontology" "mapping"   current true disabled>
		                <@img "/images/database_key.png" />
		            </@makeLink>
		        </#if>
    
        </#if>
        </#if>
       <#elseif creator??>
        <@makeViewLink namespace current />
        <#if ableToEditAnything>
          <@makeEditLink namespace current />
        </#if>
       <#else>
        <@makeLink "workspace" "list" "bookmarked resources" "list" current false>
            <@img "/images/book_go.png"/>
        </@makeLink>
        <@makeLink "workspace" "select-tables" "integrate data tables in your workspace" "select-tables" current false>
            <@img "/images/table_multiple.png"/>
        </@makeLink>
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
    <@nav.makeLink "browse" "creators" "view" "view" current true>
        <@img "/images/view.png" />
    </@nav.makeLink>
    <#if "edit" != current>
    <@nav.makeLink "entity/${creatorType}" "edit" "edit" "edit" current true >
            <@img "/images/delete.png" />
    </@nav.makeLink>
    <#else>
    <@nav.makeLink "entity/${creatorType}" "edit" "edit" "edit" current true >
            <@img "/images/desaturated/delete.png" />
    </@nav.makeLink>
    </#if>
      </ul>
    </div>
  </#if>
  </#if>
</#macro>




<#macro makeLink namespace action label name current includeResourceId=true disabled=false>
	<#if disabled>
		<li class='disabled'>
		<span><#nested> ${label}</span>
	<#elseif current?string == name?string>
		<li class='highlight'>
		<span><#nested> ${label}</span>
		<#else>
			<li>
			<#if includeResourceId>
			    <#if persistable??>
			        <#local _id = persistable.id />
			    <#else>
			        <#local _id = creator.id />
			    </#if>
				<a href='<@s.url value="/${namespace}/${action}"><@s.param name="id" value="${_id?c}" /></@s.url>'><#nested> ${label}</a>
			<#else>
				<a href='<@s.url value="/${namespace}/${action}" />'><#nested> ${label}</a>
			</#if>
		</#if>
	</li>
</#macro>

<#macro makeUploadLink namespace current url="upload" label="upload">
</#macro>

<#macro makeEditLink namespace current url="edit" label="edit">
<@makeLink namespace url label "edit" current>
<@img "/images/edit.png" />
</@makeLink>
</#macro>

<#macro makeDeleteLink namespace current url="delete" label="delete">
<#if persistable.status?? && persistable.status.toString().toLowerCase().equals('deleted')>
  <@makeLink namespace url label "delete" current true true >
    <@img "/images/desaturated/delete.png" />
  </@makeLink>
<#else>
  <@makeLink namespace url label "delete" current true false >
    <@img "/images/delete.png" />
  </@makeLink>
</#if>
</#macro>

<#macro makeViewLink namespace current url="view" label="view">
<@makeLink namespace url label "view" current>
<@img "/images/view.png" />
</@makeLink>
</#macro>

<#macro img url alt="">
<img src='<@s.url value="${url}" />' <#t>
    <#if alt != ""> alt='${alt}'</#if> <#t>
 />
</#macro>


<#macro clearDeleteButton id="" disabled="false">
<#assign disabledText = ""/>
<#if disabled="true">
  <#assign disabledText>disabled="disabled"</#assign>
</#if>
    
    <#if !rowStatus??>
        <button ${disabledText} class="addAnother minus" type="button" tabindex="-1" onclick='deleteParentRow(this)'><img src="/images/minus.gif"></button>
    <#else>
        <button ${disabledText} class="addAnother minus" type="button" tabindex="-1" onclick='deleteParentRow(this)'><img src="/images/minus.gif" class="minus"></button>
    </#if>
</#macro>
</#escape>