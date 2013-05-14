<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/content/${namespace}/edit.ftl" as local_ />
<#import "/${themeDir}/local-helptext.ftl" as  helptext>

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
<#assign fileReminder=true />
<#assign prefix="${resource.resourceType.label?lower_case}" />
<@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>


<@edit.basicInformation itemPrefix=itemPrefix itemTypeLabel=itemLabel>
<#if local_.basicInformation?? && local_.basicInformation?is_macro>
	<@local_.basicInformation />
</#if>
</@edit.basicInformation>

<@edit.allCreators '${resource.resourceType.label} Creators' authorshipProxies 'authorship' />

<@edit.citationInfo itemPrefix>
	<#if local_.citationInformation?? && local_.citationInformation?is_macro>
		<@local_.citationInformation />
	</#if>
</@edit.citationInfo>

<@edit.upload "${resource.resourceType.label} file" />


    <@edit.organizeResourceSection />
    <#if !resource.resourceType.project>
      <@edit.resourceProvider inheritanceEnabled />
      <#if licensesEnabled?? && licensesEnabled >
          <@edit.license />
      </#if>
      <#if copyrightEnabled??>
          <@edit.copyrightHolders "Copyright Holders" copyrightHolderProxies />
      </#if>
    </#if>
    <@edit.resourceCreators 'Individual and Institutional Roles' creditProxies 'credit'  />
	<@helptext.resourceCreator />
    <@edit.identifiers inheritanceEnabled />

    <@edit.spatialContext inheritanceEnabled />
    <@edit.temporalContext inheritanceEnabled />

    <@edit.investigationTypes inheritanceEnabled />
    
    <@edit.materialTypes inheritanceEnabled />

    <@edit.culturalTerms inheritanceEnabled />

    <@edit.siteKeywords inheritanceEnabled />
    
    <@edit.generalKeywords inheritanceEnabled />

    <@edit.resourceNoteSection inheritanceEnabled />



    <#if !resource.resourceType.document>
      <@edit.relatedCollections inheritanceEnabled />
    </#if>
    
    <@edit.fullAccessRights />
    
    <#if !resource.resourceType.project>
      <@edit.submit fileReminder=((resource.id == -1) && fileReminder) />
    <#else>
      <@edit.submit fileReminder=false />
    </#if>

</@s.form>

<@edit.asyncUploadTemplates />

<@edit.resourceJavascript formSelector="#metadataForm" selPrefix="#${itemLabel}" includeInheritance=inheritanceEnabled >
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
    
    <#if local_.localJavascript?? && local_.localJavascript?is_macro>
	<@local_.localJavascript />
	</#if>
    });
    
</@edit.resourceJavascript>
</body>
</#escape>