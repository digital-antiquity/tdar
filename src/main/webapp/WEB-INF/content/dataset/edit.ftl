<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

<style>
.deleteButton, .replaceButton {display:none;}
</style>

<script type="text/javascript">

$(function(){
    $("#fileUploadField").change(function(){
            if ($("#fileUploadField").val().length > 0) {
                $("#reminder").hide();
            }        
    });
});
</script>


</head>
<body>
<@edit.sidebar />
<@edit.subNavMenu />

<@edit.resourceTitle />
<@s.form name='datasetMetadataForm' id='datasetMetadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>


<@edit.basicInformation 'dataset' 'dataset' >
<br/>
<@s.select labelposition='left' label='Language'  name='resourceLanguage'  emptyOption='false' listValue='label' list='%{languages}'/>
<br/>

</@edit.basicInformation>
<@edit.allCreators 'Dataset Creators' authorshipProxies 'authorship' />
<@edit.citationInfo "dataset" />

<@edit.upload "Dataset file" />

<@edit.sharedFormComponents/>

</@s.form>


<@edit.resourceJavascript formSelector="#datasetMetadataForm" selPrefix="#dataset" includeInheritance=true>
    $(function() {

    <#if validFileExtensions??>
    var validate = $('.validateFileType');
    if ($(validate).length > 0) {
        $(validate).rules("add", {
            accept: "<@edit.join sequence=validFileExtensions delimiter="|"/>",
            messages: {
                accept: "Please enter a valid file (<@edit.join sequence=validFileExtensions delimiter=", "/>)"
            }
        });
    }
    </#if>
    });
    
</@edit.resourceJavascript>
</body>
</#escape>