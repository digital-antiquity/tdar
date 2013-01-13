<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="video">
<meta name="lastModifiedDate" content="$Date$"/>

<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />


<@view.projectAssociation resourceType="video" />
<@view.infoResourceBasicInformation>
</@view.infoResourceBasicInformation>
<@view.showcase />

<@view.sharedViewComponents video />
