<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.subNavMenu />


<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='ImageMetadataForm' id='ImageMetadataForm'  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation "image" "image">

    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the image, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='image.copyLocation' cssClass="longfield"/>
    </p>


</@edit.basicInformation>
<@edit.allCreators 'Image Creators' authorshipProxies 'authorship' />

<@edit.citationInfo "image" />

<@edit.asyncFileUpload "Image" true />


<@edit.sharedFormComponents />

</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formSelector="#ImageMetadataForm" selPrefix="#image" includeAsync=true includeInheritance=true />
</body>
</#escape>