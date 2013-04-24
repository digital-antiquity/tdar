<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/navigation-macros.ftl" as nav>
<#global validFileExtensions = ['aaa', 'bbb', 'ccc', 'jpg', 'jpeg', 'tif', 'tiff']>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />

<@edit.resourceTitle />

<@s.form name='geospatialMetadataForm' id='geospatialMetadataForm'  cssClass="form-horizontal disableFormNavigate"  method='post' enctype='multipart/form-data' action='save'>

<@edit.basicInformation "geospatial" "geospatial">

</@edit.basicInformation>

<@edit.citationInfo "geospatial" />
<@edit.chooseProjectSection />

<@edit.asyncFileUpload  uploadLabel="Attach GIS Files" showMultiple=true inputFileCss="gis-ancillary-files" />

<@edit.submit fileReminder=false />


</@s.form>


<@edit.asyncUploadTemplates />
<@edit.resourceJavascript formSelector="#geospatialMetadataForm" selPrefix="#geospatial" includeAsync=true includeInheritance=true />

<script type="text/javascript">
    $(function() {
        var fv = new  FileuploadValidator("geospatialMetadataForm");
        fv.addMethod("must-have-foo", function(file, files) {
            return file.filename.indexOf("foo") > -1;
        }, "This file does not contain the word 'foo'");
        
        fv.addRule("must-have-foo");
        $(fv.fileupload).bind("fileuploadcompleted", function() {
            fv.validate();
        });
        //expose global for debug;
        window.fv = fv;
    });
</script>


</body>
</#escape>