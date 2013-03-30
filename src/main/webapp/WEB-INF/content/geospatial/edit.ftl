<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#global validFileExtensions = ['aaa', 'bbb', 'ccc', 'jpg', 'jpeg']>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />

<@edit.resourceTitle />

<@s.form name='geospatialMetadataForm' id='geospatialMetadataForm'  cssClass="form-horizontal disableFormNavigate"  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation itemTypeLabel="geospatial" itemPrefix="geospatial">

</@edit.basicInformation>

<@edit.chooseProjectSection />

<@edit.asyncFileUpload  uploadLabel="Attach GIS Files" showMultiple=true inputFileCss="gis-ancillary-files" />

<@edit.submit fileReminder=false />

</@s.form>


<@edit.asyncUploadTemplates />
<@edit.resourceJavascript formSelector="#geospatialMetadataForm" selPrefix="#geospatial" includeAsync=true includeInheritance=true />

</body>
</#escape>