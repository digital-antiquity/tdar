<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<#if dataset.id == -1>
<title>Register a New Dataset with tDAR</title>
<#else>
<title>Editing Dataset Metadata for ${dataset.title} (tDAR id: ${dataset.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Date$"/>

<@edit.resourceJavascript formId="#datasetMetadataForm" selPrefix="#dataset" includeInheritance=true>
    $('#datasetDescription').rules("add", {
        required: true,
        messages: {
            required: "Please enter a description.",
        }
    });
</@edit.resourceJavascript>
<style>
.proxyConfidentialDiv {display:none;}
.deleteButton, .replaceButton {display:none;}
</style>

</head>
<body>

<@edit.toolbar "${resource.urlNamespace}" "edit" />
<@s.form name='datasetMetadataForm' id='datasetMetadataForm'  method='post' enctype='multipart/form-data' action='save'>


<@edit.basicInformation>
<div tiplabel="Title" tooltipcontent="Enter the entire title, including sub-title, if appropriate.">
<@s.textfield labelposition='left' id='datasetTitle' label='Title' name='dataset.title' cssClass="required tdartext longfield" 
    title="A title is required for all datasets" required=true />
</div>
<br/>
<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
<br/>
<@s.textfield labelposition='left' id='dateCreated' label='Year Created' name='dataset.dateCreated' cssClass="reasonableDate" />
<p id="t-abstract" class='new-group' tiplabel="Abstract / Description" tooltipcontent="Short description of the resource. Often comes from the resource itself, but sometimes will include additional information from the contributor."> 

    <@s.textarea label='Abstract / Description' labelposition='top' id='datasetDescription' name='dataset.description' rows="5" 
        title="A basic description is required for all datasets" cssClass='resizable' required=true />
</p>

</@edit.basicInformation>
<!-- foobar -->
<@edit.upload "Dataset file">
</@edit.upload>

<@edit.resourceCreators 'Dataset Creators' authorshipProxies 'authorship' />

<@edit.sharedFormComponents/>

</@s.form>
<div id="sidebar" parse="true">
    <div id="notice">
    <h3>Introduction</h3>
    This is the page editing form for a project.
    </div>
</div>

</body>
