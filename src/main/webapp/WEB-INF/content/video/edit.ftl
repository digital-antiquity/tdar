<#escape _untrusted as _untrusted?html>
    <#global itemPrefix="video"/>
    <#global itemLabel="video"/>
    <#global inheritanceEnabled=true />
    <#global multipleUpload=true />


    <#macro basicInformation>
    <p id="t-located" data-tooltipcontent="Actual physical location of a copy of the video, e.g. an agency, repository,
        or library." data-tiplabel="Copy Location">
        <@s.textfield labelposition='left' id='copyLocation' label='Storage Loc.' name='video.copyLocation' cssClass="longfield"/>
    </p>
    </#macro>

</#escape>