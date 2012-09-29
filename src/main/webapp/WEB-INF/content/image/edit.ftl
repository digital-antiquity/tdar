<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<#if image.id == -1>
<title>Register a New Image with tDAR</title>
<#else>
<title>Editing Image Metadata for ${image.title} (tDAR id: ${image.id?c})</title>
</#if>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>


<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='ImageMetadataForm' id='ImageMetadataForm'  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation>
<div tiplabel="Title" tooltipcontent="Enter the entire title, including sub-title, if appropriate.">
<@s.textfield labelposition='left' id='ImageTitle' label='Title' name='image.title' required=true cssClass="required tdartext descriptiveTitle longfield"
title="A title is required for all Images" />
</div>
<br/>
<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
<br/>
       <#assign dateVal = ""/>
       <#if image.dateCreated?? && image.dateCreated != -1>
         <#assign dateVal = image.dateCreated?c />
      </#if>
        <@s.textfield labelposition='left' id='dateCreated' label='Year' name='image.dateCreated' value="${dateVal}" cssClass="shortfield reasonableDate required" required=true 
         title="Please enter the year this image was created" />


<p id="t-abstract" class='new-group' tiplabel="Abstract / Description" tooltipcontent="Short description of the image. Often comes from the resource itself, but sometimes will include additional information from the contributor."> 

    <@s.textarea label='Abstract / Description' labelposition='top' id='ImageDescription' required=true name='image.description' rows="5"
       title="A basic description is required for all images"  cssClass='resizable required' />

    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the image, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='resource.copyLocation' cssClass="longfield"/>
    </p>

</p>

</@edit.basicInformation>


<@edit.asyncFileUpload "Image" true />

<@edit.resourceCreators 'Image Creators' authorshipProxies 'authorship' />

<@edit.sharedFormComponents />

</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formId="#ImageMetadataForm" selPrefix="#image" includeAsync=true includeInheritance=true />
</body>
