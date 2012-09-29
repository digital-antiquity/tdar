<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<@view.htmlHeader resourceType="image">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>

<@view.toolbar "${resource.urlNamespace}" "view" />


<@view.projectAssociation resourceType="image" />
<@view.infoResourceBasicInformation>
</@view.infoResourceBasicInformation>
<@view.uploadedFileInfo />
<@view.keywords />
<@view.spatialCoverage />
<@view.temporalCoverage />
<@view.resourceProvider />
<@view.indvidualInstitutionalCredit />
<@view.sourceCitations />
<@view.sourceCollections />
<@view.relatedCitations />
<@view.relatedComparativeCollections />
<#-- display linked data <-> ontology nodes -->
<@view.unapiLink image />

<@view.infoResourceAccessRights />
