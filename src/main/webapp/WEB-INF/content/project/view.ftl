<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>
<@view.htmlHeader resourceType="project">
<meta name="lastModifiedDate" content="$Date$"/>
<@view.googleScholar />
</@view.htmlHeader>

<@view.toolbar "${resource.urlNamespace}" "view" />

<@view.basicInformation />
<@view.keywords showParentProjectKeywords=false />
<@view.spatialCoverage />
<@view.temporalCoverage />

<@view.indvidualInstitutionalCredit />

<#-- FIXME: add information resources, coding sheets, and ontologies ? -->
<#if project.informationResources.isEmpty() >
    No resources have been associated with this project.
<#else>
<div class='accordion'>
<h3 class='accordion head'>Resources within this Project</h3>
<div>
      <@rlist.informationResources iterable="project.documents" title="Documents" displayable=!project.documents.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.images" title="Images" displayable=!project.images.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.datasets" title="Datasets" displayable=!project.datasets.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.codingSheets" title="Coding sheets" displayable=!project.codingSheets.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.ontologies" title="Ontologies" displayable=!project.ontologies.empty editable=editable showProject=false/>
      <@rlist.informationResources iterable="project.sensoryDataDocuments" title="Sensory Data" displayable=!project.sensoryDataDocuments.empty editable=editable showProject=false/>
</div>
</div>
</#if>

<@view.accessRights />
