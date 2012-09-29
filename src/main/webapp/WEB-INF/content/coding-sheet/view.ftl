<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="coding sheet">
    <meta name="lastModifiedDate" content="$Date$"/>
    <@view.googleScholar />
</@view.htmlHeader>

<@view.toolbar "${resource.urlNamespace}" "view" />


<@view.projectAssociation resourceType="coding sheet" />

<@view.infoResourceBasicInformation />

<@view.codingRules>
<@view.categoryVariables />
</@view.codingRules>

<@view.relatedResourceSection label="Coding Sheet"/>

<@view.resourceNotes />
<@view.unapiLink codingSheet />
<@view.infoResourceAccessRights />


