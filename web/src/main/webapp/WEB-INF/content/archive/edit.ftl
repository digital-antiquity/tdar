<#escape _untrusted as _untrusted?html>
	<#import "/WEB-INF/macros/resource/common-resource.ftl" as common>
	<#import "/WEB-INF/macros/helptext.ftl" as  helptext>
	<#global multipleUpload=multipleFileUploadEnabled!false />
    <#global itemPrefix="archive"/>
    <#global inheritanceEnabled=true />
    <#macro afterUpload>
        <h2>Import Action</h2>
        <#if !resource.importDone>
            <@helptext.unpackArchiveTip />
            <div class="" id="unpackArchiveSection" data-tiplabel="Unpack the archive?" data-tooltipcontent="#divUnpackArchiveTip" >
                <@boolfield label='Unpack the uploaded archive (.bz2) into the repository?' name="resource.doImportContent" id="do_import_content" value=resource.doImportContent!false  />
            </div>
        <#else>
            <p>This archive <strong>has already been</strong> unpacked into the repository.
            </p>
         </#if>
    </#macro>
    
    <#macro boolfield name label id value labelPosition="left" type="checkbox" labelTrue="Yes" labelFalse="No" cssClass="">
        <#if value?? && value?string == 'true'>
            <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" 
                checked="checked"/>
        <#else>
            <@s.checkbox name="${name}" label="${label}" labelPosition="${labelPosition}" id="${id}"  value=value cssClass="${cssClass}" />
        </#if>
    
    </#macro>
</#escape>