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
<@s.form id='resourceMetadataForm' method='post' enctype='multipart/form-data' action='save'  cssClass="form-horizontal">
<@edit.spatialContext />
</@s.form>

<@edit.resourceJavascript />

<div class="well">
    <h3>Experimental map </h3>
    <h4>${resource.title!"new resource"} (id:${id?c})</h4>
    <div id="editmapv3" class="tdar-map-large tdar-map-edit googlemap"></div>
</div>

<script>
    $(function() {
        //fixme: implicitly init when necessary
        TDAR.maps.initMapApi();
        var mapdiv = $('#editmapv3')[0];
        TDAR.maps.setupMap(mapdiv);
        TDAR.maps.setupEditMap(mapdiv);
    });
</script>

</body>
</#escape>