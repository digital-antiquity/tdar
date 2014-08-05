<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
    <#import "/WEB-INF/macros/resource/common.ftl" as common>

    <#macro afterBasicInfo>
        <#if resource.projection??>
            <@view.kvp key="Map Source" val=resource.mapSource />
        </#if>

        <#if resource.scale??>
            <@view.kvp key="Scale" val=resource.scale />
        </#if>

        <#if resource.spatialReferenceSystem??>
            <@view.kvp key="Spatial Reference System & Projection" val=resource.spatialReferenceSystem />
        </#if>

        <#if resource.currentnessUpdateNotes??>
            <@view.kvp key="Currentness & Update Notes" val=resource.currentnessUpdateNotes />
        </#if>

    </#macro>

</#escape>