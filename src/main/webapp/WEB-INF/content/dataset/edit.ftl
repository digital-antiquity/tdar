<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<#if dataset.id == -1>
<title>Register a New Dataset with tDAR</title>
<#else>
<title>Editing Dataset Metadata for ${dataset.title} (tDAR id: ${dataset.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Date$"/>

<style>
.deleteButton, .replaceButton {display:none;}
</style>

<script type="text/javascript">

$(function(){
    $("#fileUploadField").change(function(){
            if ($("#fileUploadField").val().length > 0) {
                $("#reminder").hide();
            }        
    });
});
</script>


</head>
<body>

<@edit.toolbar "${resource.urlNamespace}" "edit" />
<@s.form name='datasetMetadataForm' id='datasetMetadataForm'  method='post' enctype='multipart/form-data' action='save'>


<@edit.basicInformation>
<div tiplabel="Title" tooltipcontent="Enter the entire title, including sub-title, if appropriate.">
<@s.textfield labelposition='left' id='datasetTitle' label='Title' name='dataset.title' cssClass="required descriptiveTitle tdartext longfield" 
    title="A title is required for all datasets" required=true />
</div>
<br/>
<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
<br/>
       <#assign dateVal = ""/>
       <#if dataset.date?? && dataset.date!= -1>
         <#assign dateVal = dataset.date?c />
      </#if>
        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='dataset.date' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true 
         title="Please enter the year this dataset was created" />

<p id="t-abstract" class='new-group' tiplabel="Abstract / Description" tooltipcontent="Short description of the resource. Often comes from the resource itself, but sometimes will include additional information from the contributor."> 

    <@s.textarea label='Abstract / Description' labelposition='top' id='datasetDescription' name='dataset.description' rows="5" 
        title="A basic description is required for all datasets" cssClass='resizable required' required=true />
</p>

</@edit.basicInformation>

<@edit.upload "Dataset file" />

<@edit.resourceCreators 'Dataset Creators' authorshipProxies 'authorship' />

<@edit.sharedFormComponents/>

</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formId="#datasetMetadataForm" selPrefix="#dataset" includeInheritance=true />
</body>
</#escape>