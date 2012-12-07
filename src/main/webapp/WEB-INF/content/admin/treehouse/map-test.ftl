<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<head>
<style type="text/css">
.tdar-map-large {
    height:450px;
}
</style>
<title>Welcome to the treehouse</title>
<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<h4>${resource.title!"new resource"} (id:${id?c})</h4>
<@s.form id='resourceMetadataForm' method='post' enctype='multipart/form-data' action='save'  cssClass="form-horizontal">
<@edit.spatialContext />
</@s.form>

<@edit.resourceJavascript />


<script>
$(function() {


});
</script>

<div id="overlay"></div>
<p id="popup">Loading...</p>
</body>
</#escape>