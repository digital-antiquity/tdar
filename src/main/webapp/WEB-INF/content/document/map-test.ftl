<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@s.form id='resourceMetadataForm' method='post' enctype='multipart/form-data' action='save'  cssClass="form-horizontal">
<@edit.spatialContext />
</@s.form>

<@edit.resourceJavascript />
</body>
</#escape>