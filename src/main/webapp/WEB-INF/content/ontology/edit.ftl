<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit />
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view />
<head>
    <script type='text/javascript' src='<@s.url value="/includes/jquery.tabby.min.js"/>'></script> 

<#if resource.id == -1>
<title>Register a new ontology with tDAR</title>
<#else>
<title>Editing Ontology Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Id$"/>
</head>
<body>
<@edit.toolbar "${resource.urlNamespace}" "edit" />
<div>

<@s.form id='resourceMetadataForm' method='post' action='save' enctype='multipart/form-data'>

<@edit.basicInformation>
<@s.textfield labelposition='left' id='resourceTitle' label='Title' title="A title is required for all ontologies" name='ontology.title' required='true' cssClass="longfield descriptiveTitle  required" maxlength="512" />
<br/>
<span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005.">
       <#assign dateVal = ""/>
       <#if ontology.dateCreated?? && ontology.dateCreated != -1>
         <#assign dateVal = ontology.dateCreated?c />
      </#if>
        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='ontology.dateCreated' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true 
         title="Please enter the year this ontology was created" />
</span>

<p id="t-abstract" class="new-group">
    <@s.textarea id='resourceDescription' label='Abstract / Description' labelposition='top'rows='5' name='ontology.description'
        title="A basic description is required for all ontologies" cssClass='resizable required' required=true />
</p>

</@edit.basicInformation>
<#--
 <@edit.fullAccessRights />
-->

<#if (resource.getLatestVersions().size()>0)>
<div class="glide">
    <@view.ontology />
</div>
</#if>

<@edit.manualTextInput typeLabel="Ontology" type="ontology" />

<@edit.resourceCreators 'Ontology Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.submit  fileReminder=false />
</@s.form>
</div>

<@edit.sidebar />

<@edit.resourceJavascript>
    setupSupportingResourceForm(${resource.getTotalNumberOfFiles()?c}, "ontology");
    $('#fileInputTextArea').tabby();
</@edit.resourceJavascript>


</body>
