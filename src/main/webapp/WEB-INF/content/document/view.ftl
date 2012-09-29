<@s.set name="theme" value="'bootstrap'" scope="request" />
<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="document">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>
<@view.toolbar "${resource.urlNamespace}" "view" />


			<article class="span9">
<@view.projectAssociation resourceType="document" />

<@view.infoResourceBasicInformation />

<@view.sharedViewComponents document />
</article>
    <@view.sidebar />
</#escape>