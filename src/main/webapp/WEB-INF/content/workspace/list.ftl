<#import "/WEB-INF/macros/resource/list-macros.ftl" as rlist>

<head>
<title>${authenticatedUser.properName}'s Workspace</title>
<meta name="lastModifiedDate" content="$Date$"/>
</head>

<h2>Your Bookmarked Resources</h2>
<@rlist.toolbar "workspace"  />

<p>
	<a href="#datasets">datasets (${bookmarkedDatasets.size()?c})</a> | 
	<a href="#documents">documents (${bookmarkedDocuments.size()?c})</a> | 
	<a href="#ontologies">ontologies (${bookmarkedOntologies.size()?c})</a> | 
    <a href="#codingSheets">coding sheets (${bookmarkedCodingSheets.size()?c})</a> | 
    <a href="#images">images (${bookmarkedImages.size()?c})</a> |
    <a href="#projects">projects (${bookmarkedProjects.size()?c})</a> |
    <a href="#sensoryData">sensory data (${bookmarkedSensoryData.size()?c})</a>
</p>

<a name="datasets"></a>
<@rlist.informationResources iterable="bookmarkedDatasets" editable=false bookmarkable=true title="Datasets" displayable=!bookmarkedDatasets.empty />

<a name="documents"></a>
<@rlist.informationResources iterable="bookmarkedDocuments" editable=false bookmarkable=true title="Documents" displayable=!bookmarkedDocuments.empty />

<a name="images"></a>
<@rlist.informationResources iterable="bookmarkedImages" editable=false bookmarkable=true title="Images" displayable=!bookmarkedImages.empty />

<a name="sensoryData"></a>
<@rlist.informationResources iterable="bookmarkedSensoryData" editable=false bookmarkable=true title="Sensory Data" displayable=!bookmarkedSensoryData.empty />

<a name="projects"></a>
<@rlist.informationResources iterable="bookmarkedProjects" editable=false bookmarkable=true title="Projects" displayable=!bookmarkedProjects.empty />

<a name="ontologies"></a>
<@rlist.informationResources iterable="bookmarkedOntologies" editable=false bookmarkable=true title="Ontologies" displayable=!bookmarkedOntologies.empty />

<a name="codingSheets"></a>
<@rlist.informationResources iterable="bookmarkedCodingSheets" editable=false bookmarkable=true title="Coding Sheets" displayable=!bookmarkedCodingSheets.empty />