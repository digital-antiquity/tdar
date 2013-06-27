<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/common.ftl" as common>

<#macro afterBasicInfo>
  <#if resource.projection??>
      <@view.kvp key="Projection" val=resource.projection />
  </#if>

  <#if resource.scale??>
      <@view.kvp key="Scale" val=resource.scale />
  </#if>
	
  <#if resource.spatialReferenceSystem??>
      <@view.kvp key="Spatial Reference System" val=resource.spatialReferenceSystem />
  </#if>

  <#if resource.currentnessUpdateNotes??>
	  <p><strong>Currentness &amp; Update Notes</strong></p>
  <p>${resource.currentnessUpdateNotes}</p>
  </#if>
	
</#macro>

</#escape>