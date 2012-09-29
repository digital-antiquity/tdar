<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>


<@edit.toolbar "${resource.urlNamespace}" "edit" />

<@s.form name='VideoMetadataForm' id='VideoMetadataForm'  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation "video" "video">

    <p id="t-located"  tooltipcontent="Actual physical location of a copy of the video, e.g. an agency, repository, 
        or library." tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='video.copyLocation' cssClass="longfield"/>
    </p>


</@edit.basicInformation>
<@edit.citationInfo "video" />

<@edit.asyncFileUpload "Video" true />

<@edit.allCreators 'Video Creators' authorshipProxies 'authorship' />

<@edit.sharedFormComponents />

</@s.form>

<@edit.sidebar />

<@edit.resourceJavascript formId="#VideoMetadataForm" selPrefix="#video" includeAsync=true includeInheritance=true />
</body>
</#escape>