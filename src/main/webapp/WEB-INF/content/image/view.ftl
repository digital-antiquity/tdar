<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="image">
<meta name="lastModifiedDate" content="$Date$"/>

<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />


<@view.projectAssociation resourceType="image" />
<@view.infoResourceBasicInformation>
</@view.infoResourceBasicInformation>
<@view.showcase />

<@view.sharedViewComponents image />
