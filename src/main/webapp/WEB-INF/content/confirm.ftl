<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>

<#assign deleteable = true>
<#if resource.status != 'DELETED'>

  <#if relatedResources??>
    <#list relatedResources as rsc>
      <#if !rsc.isDeleted()> <#assign deleteable = false> </#if>
    </#list> 
  </#if>
  <#if resource.informationResources??>
    <#list resource.informationResources as rsc>
      <#if !rsc.isDeleted()> <#assign deleteable = false> </#if>
    </#list> 
  </#if>
  
  <h2>Confirm deletion of ${resource.resourceType.label?lower_case} ${resource.title}</h2>
  
  <#if resource.resourceType.label?lower_case != "project">
    <@view.projectAssociation resourceType="${resource.resourceType.label?lower_case}" />
  </#if>
  
  <#if !deleteable>
    <h4>This resource cannot be deleted because it is still used by the following resources</h4>
    <ul>
    <#if relatedResources??>
      <#list relatedResources as rsc>
        <#if !rsc.isDeleted()>  
          <li>${rsc.id?c} - ${rsc.title} </li> 
        </#if>
      </#list> 
    </#if>
    <#if resource.informationResources??>
      <#list resource.informationResources as rsc>
        <#if !rsc.isDeleted()>
         <li>${rsc.id?c} - ${rsc.title} </li>
       </#if>
      </#list> 
    </#if>
  
  <#else>
    <@s.form name='deleteForm' id='deleteForm'  method='post' action='delete'>
      <h4>Are you sure you want to delete this ${resource.resourceType.label?lower_case}?</h4>
    
      <@s.submit type="submit" name="delete" value="delete" />
      <@s.hidden name="resourceId" />
    </@s.form>
  </#if>
 <#else>
 <h2>This resource has already been deleted ${resource.resourceType.label?lower_case} ${resource.title}</h2>

</#if>
