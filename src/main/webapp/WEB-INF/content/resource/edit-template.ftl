<#escape _untrusted as _untrusted?html>
<#import "/WEB-INF/macros/resource/edit-macros.ftl" as edit>
<#import "/WEB-INF/macros/resource/common.ftl" as common>
<#import "/WEB-INF/content/${namespace}/edit.ftl" as local_ />
<#import "/${themeDir}/local-helptext.ftl" as  helptext>

<head>
<@edit.title />

<meta name="lastModifiedDate" content="$Date$"/>

<style>
// .deleteButton, .replaceButton {display:none;}
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
<@edit.subNavMenu>
	<#if local_.subNavMenu?? && local_.subNavMenu?is_macro>
		<@local_.subNavMenu />
	</#if>

</@edit.subNavMenu>

<#if  local_.customH1?? && local_.customH1?is_macro>
	<@local_.customH1 />
<#else>
	<#assign newTitle>New <#noescape>${resource.resourceType.label}</#noescape></#assign>
	<h1><#if resource.id == -1>Creating<#else>Editing</#if>:<span> <#if resource.title?has_content>${resource.title}<#else>${newTitle}</#if> </span></h1>
</#if>

<#assign fileReminder=true />
<#assign prefix="${resource.resourceType.label?lower_case}" />
<@s.form name='metadataForm' id='metadataForm'   cssClass="form-horizontal" method='post' enctype='multipart/form-data' action='save'>

	<#if local_.topSection?? && local_.topSection?is_macro>
		<@local_.topSection />
	</#if>

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
	<#if local_.beforeUpload?? && local_.beforeUpload?is_macro>
		<@local_.beforeUpload />
	</#if>


<#if multipleUpload??>
	<#if multipleUpload>
		<@edit.asyncFileUpload  uploadLabel="Attach ${resource.resourceType.label} Files" showMultiple=multipleUpload />
	<#else>
		<@edit.upload "${resource.resourceType.label} file" />
	</#if>
</#if>

	<#if local_.localSection?? && local_.localSection?is_macro>
		<@local_.localSection />
	</#if>


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


	<#if !(hideCreditSection??)>
	    <@edit.resourceCreators 'Individual and Institutional Roles' creditProxies 'credit'  />
    </#if>
	<@helptext.resourceCreator />

	<#if !(hideKeywordsAndIdentifiersSection??)>
	    <@edit.identifiers inheritanceEnabled />
	
	    <@edit.spatialContext inheritanceEnabled />
	    <@edit.temporalContext inheritanceEnabled />
	
	    <@edit.investigationTypes inheritanceEnabled />
	    
	    <@edit.materialTypes inheritanceEnabled />
	
	    <@edit.culturalTerms inheritanceEnabled />
	
	    <@edit.siteKeywords inheritanceEnabled />
	    
	    <@edit.generalKeywords inheritanceEnabled />
	</#if>

    <@edit.resourceNoteSection inheritanceEnabled />



    <#if !(hideRelatedCollections??)>
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

<#if local_.footer?? && local_.footer?is_macro>
	<@local_.footer />
</#if>
	
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