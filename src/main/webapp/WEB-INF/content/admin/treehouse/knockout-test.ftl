<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>


<@s.form name='ImageMetadataForm' id='ImageMetadataForm'  method='post' cssClass="form-horizontal" action='save'>
    <@s.radio label="" name="frob" list="#@java.util.LinkedHashMap@{true:'Frob it',false:'Leave unfrobbed'}" />

</@s.form>


</body>
</#escape>