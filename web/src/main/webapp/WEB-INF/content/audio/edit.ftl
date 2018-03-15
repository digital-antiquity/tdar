<#escape _untrusted as _untrusted?html>
    <#import "/WEB-INF/macros/resource/common-resource.ftl" as common />
    <#import "/${config.themeDir}/local-helptext.ftl" as helptext />
    <#global multipleUpload=multipleFileUploadEnabled!false />
    <#global itemPrefix="audio"/>
    <#global inheritanceEnabled=true />
    <#macro beforeUpload>
        <@helptext.audioSoftwareTip />
    <div class="" id="audioSoftwareSection" data-tiplabel="Application" data-tooltipcontent="#divAudioSoftwareTip">
        <h2>Application</h2>
        <@s.textfield label="Software" id="resource_software"
        title="Provide the name of the application that produced the audio file" name='audio.software'
        cssClass="required descriptiveTitle input-xxlarge" required=true maxlength="255"/>
    </div>
    </#macro>
</#escape>