<#escape _untrusted as _untrusted?html>
<#global itemPrefix="geospatial"/>
<#global inheritanceEnabled=true />
<#global multipleUpload=true />
<#global hideRelatedCollections=true/>
<#global hideKeywordsAndIdentifiersSection=true/>


<#-- global validFileExtensions = ['aaa', 'bbb', 'ccc', 'jpg', 'jpeg', 'tif', 'tiff'] -->


<#macro localJavascript>
/*
        var fv = new  FileuploadValidator("geospatialMetadataForm");
        fv.addMethod("must-have-foo", function(file, files) {
            return file.filename.indexOf("foo") > -1;
        }, "This file does not contain the word 'foo'");
        fv.addRule("must-have-foo");
         
        //expose global for debug;
        window.fv = fv;

*/</#macro>

<#macro footer>

</#macro>
</#escape>