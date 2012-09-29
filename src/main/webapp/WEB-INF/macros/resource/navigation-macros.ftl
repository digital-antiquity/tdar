<#-- 
$Id$ 
navigation freemarker macros
-->
<#import "list-macros.ftl" as list>
<#import "edit-macros.ftl" as edit>

<#macro showControllerErrors>
<#if (actionErrors?? && actionErrors.size() > 0)>
   <div class="action-errors ui-corner-all">
     <b>The following errors were found with your submission</b> <br />
     <ul>
     <@s.iterator var='err' value='actionErrors'>
      <li class="action-error">${err!"unknown error"}</li>
     </@s.iterator>
     </ul>
     <#if (stackTraces?? && stackTraces.size() > 0)>
     <a href="#" id="pToggleStackTrace" onclick="$('#stackTraceList').toggle();return false;">Show/Hide Detailed Error Information</a>
     <div id="stackTraceList" class="hidden">
        <#list stackTraces as _stacktrace>
        <p>
        <em>Stacktrace ${_stacktrace_index + 1} of ${stackTraces.size()}</em><br />
        ${_stacktrace?html}
        </p>
        </#list>
     </div>
     </#if>
   </div>
</#if>
</#macro>

<#macro loginForm>
<script type="text/javascript">
$(document).ready(function() {
  $('#loginForm').validate({
    errorLabelContainer: $("#error"),
    messages: {
      loginEmail: {
        email: "Please enter a valid email address.",
        required: "Please enter a valid email address."
      },
      loginPassword: {
        required: "Please enter your password."
      }
    }
  });
  $('#loginEmail').focus();
});
</script>
<style type='text/css'>
.overrideCheckbox {margin-left: 9.3em !important; }
</style>

<@s.form id='loginForm' method="post" action="%{#request.contextPath}/login/process">
    <@s.textfield id='loginEmail' label="Email" name="loginEmail" cssClass="required email" /><br/>
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
       <#if resource??>
        <@makeViewLink namespace current />
        <#if editable>
          <@makeEditLink namespace current />
          <@makeDeleteLink namespace current />
        </#if>
        <li><@list.bookmark resource /></li>
        <#if resource.resourceType == "PROJECT">                
          <@makeLink "resource" "add?projectId=${resource.id?c}" "add new resource to project" "add" "" false>
            <@img "/images/database_add.png"/> 
          </@makeLink>
        </#if>
        <#if editable && resource.resourceType == 'DATASET'>
          <@makeLink "dataset" "citations" "manage citations" "citations" current>
            <@img "/images/book_edit.png" />
          </@makeLink>
        <#if ! resource.dataTables.isEmpty() >
            <@makeLink "dataset" "columns" "map columns" "columns" current>
            <@img "/images/database_table.png" />
            </@makeLink>
        </#if>
    
        <#if ontologyLinked>
            <@makeLink "dataset" "column-ontology" "link ontology" "column-ontology" current>
                <@img "/images/database_key.png" />
            </@makeLink>
        </#if>
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
<a href='<@s.url value="/${namespace}/${action}" resourceId="${resource.id?c}" />'><#nested> ${label}</a>
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
<#if resource.status.toString().toLowerCase().equals('deleted')>
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

<#-- macro to lessen typing when constructing an <img> tag 
FIXME: add alt text?
-->

<#macro img url>
<img src='<@s.url value="${url}" />' />
</#macro>


<#macro clearDeleteInputButton id="">
    <#if !rowStatus??>
        <input type='button' value='Clear / Remove' onclick='clearRow("${"#" + id + "_0"}");' />
    <#elseif rowStatus.index == 0>
        <input type='button' value='Clear / Remove' title='Remove the information stored in this field' onclick='clearRow("${"#" + id + "_" + rowStatus.index}");' />
    <#else>
        <input type='button' value='Delete' onclick='deleteRow("${"#" + id + "_" + rowStatus.index}");'/>
    </#if>
</#macro>

<#macro clearDeleteButton id="">
    <#if !rowStatus??>
        <button class="addAnother minus" type="button" tabindex="-1" onclick='deleteParentRow(this)'><img src="/images/minus.gif"></button>
    <#else>
        <button class="addAnother minus" type="button" tabindex="-1" onclick='deleteParentRow(this)'><img src="/images/minus.gif" class="minus"></button>
    </#if>
</#macro>

