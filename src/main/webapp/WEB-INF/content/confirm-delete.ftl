<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#assign deleteable = true>
<#if persistable??>

<#if !persistable.status?? || persistable.status != 'DELETED'>

  
  <#if deleteIssues?? && !deleteIssues.isEmpty()>
    <#assign deleteable= false />
    <#list deleteIssues as issue>
      <#if issue.deleted?? && !issue.deleted> <#assign deleteable = false> </#if>
    </#list> 
  
  </#if>
<#if persistable.resourceType??>
<#assign whatamideleting = persistable.resourceType.label?lower_case />
<#else>
<#assign whatamideleting = persistableClass.simpleName />
</#if>
  <h2>Confirm deletion of ${whatamideleting}: ${persistable.name?html}</h2>
  
  <#if persistable.resourceType?? && persistable.resourceType.label?lower_case != "project">
    <@view.projectAssociation resourceType="${persistable.resourceType.label?lower_case}" />
  </#if>
  
  <#if !deleteable>
    <h4>This ${whatamideleting} cannot be deleted because it is still referenced by the following: </h4>
    <ul>
    <#if deleteIssues??>
      <#list deleteIssues as rsc>
        <#if !rsc.deleted?? || !rsc.deleted>  
          <li>${rsc.id?c} - ${rsc.name?html} </li> 
        </#if>
      </#list> 
    </#if>
  
  <#else>
    <@s.form name='deleteForm' id='deleteForm'  method='post' action='delete'>
      <h4>Are you sure you want to delete this <#if persistable.resourceType??>${persistable.resourceType.label?lower_case}</#if>?</h4>

      <@s.submit type="submit" name="delete" value="delete" />
      <@s.hidden name="id" />
    </@s.form>
  </#if>
 <#else>
 <h2>This resource has already been deleted <#if persistable.resourceType??>${persistable.resourceType.label?lower_case}</#if> ${persistable.name?html}</h2>

</#if>

 <#else>
 <h2>This resource has already been deleted</h2>

</#if>
