<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="dataset">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.projectAssociation resourceType="dataset" />
<@view.infoResourceBasicInformation />
<@view.sharedViewComponents resource />
