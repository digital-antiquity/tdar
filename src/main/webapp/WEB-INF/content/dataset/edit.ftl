<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
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

<@edit.toolbar "${resource.urlNamespace}" "edit" />
<@s.form name='datasetMetadataForm' id='datasetMetadataForm'  method='post' enctype='multipart/form-data' action='save'>


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

<@edit.sidebar />

<@edit.resourceJavascript formId="#datasetMetadataForm" selPrefix="#dataset" includeInheritance=true />
</body>
</#escape>