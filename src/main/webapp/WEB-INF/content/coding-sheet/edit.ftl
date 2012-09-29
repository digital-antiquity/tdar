<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/view-macros.ftl" as view>
<head>
<#if resource.id == -1>
<title>Register a New Coding Sheet With tDAR</title>
<#else>
<title>Editing Coding Sheet Metadata for ${resource.title} (tDAR id: ${resource.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Id$"/>

</head>
<body>

<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='resourceRegistrationForm' id='resourceRegistrationForm' method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation>
<span
    tiplabel="Title"
    tooltipcontent="Enter the entire title, including sub-title, if appropriate."
>
<@s.textfield labelposition='left' id="resourceRegistrationTitle" label='Title' title="A title is required for all coding sheets" name='codingSheet.title' cssClass="required descriptiveTitle longfield" required=true maxlength="512"/>
</span>
<br/>
<span tiplabel="Year" tooltipcontent="Four digit year, e.g. 1966 or 2005.">
       <#assign dateVal = ""/>
       <#if codingSheet.dateCreated?? && codingSheet.dateCreated != -1>
         <#assign dateVal = codingSheet.dateCreated?c />
      </#if>
        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='codingSheet.dateCreated' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true
          title="Please enter the year this coding sheet was created" />

</span>
<p id="t-abstract" class="new-group"
    tiplabel="Abstract / Description"
    tooltipcontent="Short description of the resource. Often comes from the resource itself, but sometimes will include additional information from the contributor.">
    <@s.textarea label='Abstract / Description' labelposition='top' id="resourceRegistrationDescription" rows='5' 
        title="A basic description is required for all coding sheets" name='codingSheet.description' cssClass="resizable required" required=true />
</p>
</@edit.basicInformation>

<#-- <@edit.fullAccessRights /> -->
<div class="glide">
<@view.codingRules />
</div>
<@edit.manualTextInput typeLabel="Coding Sheet" type="coding" />

<@edit.resourceCreators 'Coding Sheet Creators' authorshipProxies 'authorship' />

<@edit.resourceNoteSection />

<@edit.submit  fileReminder=false  />
</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formId="#resourceRegistrationForm" selPrefix="#resourceRegistration">
    setupSupportingResourceForm(${codingSheet.getTotalNumberOfFiles()?c}, "coding sheet");
</@edit.resourceJavascript>

</body>
